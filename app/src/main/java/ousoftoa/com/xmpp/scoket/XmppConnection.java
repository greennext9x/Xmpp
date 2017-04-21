package ousoftoa.com.xmpp.scoket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.Room;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.PinyinComparator;
import rx.Observable;
import rx.Subscriber;

public class XmppConnection {
    private static XMPPTCPConnection connection;
    private static XmppConnection xmppConnection;
    private static Chat newchat;
    private static MultiUserChat mulChat;
    private static List<Friend> friendList = new ArrayList<>();
    private XmppConnecionListener connectionListener;
    private XmppMessageInterceptor xmppMessageInterceptor;
    private XmppMessageListener messageListener;
    public static List<Room> myRooms = new ArrayList<>();
    public static List<Room> leaveRooms = new ArrayList<>();

    /**
     * 单例模式
     *
     * @return
     */
    public static XmppConnection getInstance() {
        if (xmppConnection == null) {
            synchronized (XmppConnection.class) {
                if (xmppConnection == null) {
                    xmppConnection = new XmppConnection();
                }
            }
        }
        return xmppConnection;
    }

    public void setNull() {
        connection = null;
    }

    /**
     * 创建连接
     */
    public XMPPTCPConnection getConnection() {
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    /**
     * 打开连接
     */
    public boolean openConnection() {
        try {
            if (null == connection || !connection.isAuthenticated()) {
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setHost( Constants.SERVER_HOST )
                        .setServiceName( Constants.SERVER_NAME )
                        .setPort( Constants.SERVER_PORT )
                        .setSecurityMode( XMPPTCPConnectionConfiguration.SecurityMode.disabled )
                        .setDebuggerEnabled( true )
                        .setCompressionEnabled( false )
                        .setSendPresence( true )// 状态设为离线，目的为了取离线消息
                        .build();
                connection = new XMPPTCPConnection( config );
                connection.connect();// 连接到服务器
                // 添加連接監聽
                connectionListener = new XmppConnecionListener();
                connection.addConnectionListener( connectionListener );
                xmppMessageInterceptor = new XmppMessageInterceptor();
                messageListener = new XmppMessageListener();
                connection.addPacketInterceptor( xmppMessageInterceptor, new PacketTypeFilter( Message.class ) );
                connection.addSyncStanzaListener( messageListener, new PacketTypeFilter( Message.class ) );
                connection.addSyncStanzaListener( new XmppPresenceListener(), new PacketTypeFilter( Presence.class ) );
                connection.addPacketInterceptor( new XmppPresenceInterceptor(), new PacketTypeFilter( Presence.class ) );
                ProviderManager.addIQProvider( "muc", "MZH", new MUCPacketExtensionProvider() );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public void closeConnection() {
        if (connection != null) {
            connection.removeConnectionListener( connectionListener );
            ProviderManager.removeIQProvider( "muc", "MZH" );
            try {
                connection.disconnect();
            } catch (Exception e) {
                if (Constants.IS_DEBUG)
                    e.printStackTrace();
            } finally {
                connection = null;
                xmppConnection = null;
            }
        }
    }

    public void reconnect() {
        new Handler().postDelayed( () -> {
            closeConnection();
            login( Constants.USER_NAME, Constants.PWD );
            loadFriendAndJoinRoom();
        }, 1000 );
    }

    public Observable loadFriendAndJoinRoom() {
        return Observable.from( getMyRoom() )
                .map( room -> joinMultiUserChat( Constants.USER_NAME, room.name, false ) );
    }

    /**
     * 登录
     *
     * @param account  登录帐号
     * @param password 登录密码
     * @return
     */
    public Observable<List<Friend>> login(String account, String password) {
        return Observable.create( new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    if (getConnection() == null) {
                        subscriber.onError( new Throwable( "连接失败" ) );
                    }
                    if (!getConnection().isAuthenticated() && getConnection().isConnected()) {
                        getConnection().login( account, password );
                        // 更改在綫狀態
                        Presence presence = new Presence( Presence.Type.available );
                        presence.setMode( Presence.Mode.available );
                        Constants.MODE = presence.getMode().toString();
                        getConnection().sendStanza( presence );
                        subscriber.onNext( true );
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError( new Throwable( "重复连接" ) );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError( new Throwable( e ) );
                }
            }
        } ).map( aBoolean -> {
            VCard vCard = getUserInfo( null );
            if (vCard != null){
                Friend mfriend = new Friend();
                String username = account;
                String nikename = vCard.getField("nickName");
                String userheard = vCard.getField( "avatar" );
                if (nikename == null){
                    nikename = username;
                }
                mfriend.setUsername( username );
                mfriend.setUserHead( userheard );
                mfriend.setNickname( nikename );
                DataHelper.saveDeviceData( MyApplication.getInstance(), Constants.USERINFO, mfriend );
                return vCard;
            } else{
                return new Throwable( "获取用户信息失败" );
            }
        } ).flatMap( vCard -> getFriend());
    }

    /**
     * 注册
     *
     * @param account  注册帐号
     * @param password 注册密码
     * @return 1、注册成功 0、服务器没有返回结果2、这个账号已经存在3、注册失败
     */
    public boolean regist(String account, String password) {
        if (getConnection() == null) {
            return false;
        } else {
            try {
                AccountManager.getInstance( getConnection() ).createAccount( account, password );
                return true;
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException
                    | SmackException.NotConnectedException e) {
                return false;
            }
        }
    }

    /**
     * 修改密码
     *
     * @param pwd
     * @return
     */
    public boolean changPwd(String pwd) {
        try {
            AccountManager.getInstance( getConnection() ).changePassword( pwd );
            return true;
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException e) {
            return false;
        }
    }

    public void setRecevier(String chatName, int chatType) {
        if (getConnection() == null)
            return;
        if (chatType == ChatItem.CHAT) {
            // 创建回话
            ChatManager cm = ChatManager.getInstanceFor( getConnection() );
            // 发送消息给pc服务器的好友（获取自己的服务器，和好友）
            newchat = cm.createChat( getFullUsername( chatName ), null );
        } else if (chatType == ChatItem.GROUP_CHAT) {
            mulChat = MultiUserChatManager.getInstanceFor( getConnection() )
                    .getMultiUserChat( chatName + "@conference." + getConnection().getServiceName() );
        }
    }

    //发送文本消息 String chatname
    @SuppressLint("NewApi")
    public void sendMsg(String chatname, String msg, int chatType) throws Exception {
        if (getConnection() == null) {
            throw new Exception( "XmppException" );
        }
        if (!msg.isEmpty()) {
            Message message = new Message();
            message.setBody( msg );
            message.setFrom( getConnection().getUser() );
            message.setTo( chatname + "@" + Constants.SERVER_NAME );
            // 添加回执请求
            DeliveryReceiptManager.addDeliveryReceiptRequest( message );
            if (chatType == ChatItem.CHAT) {
                newchat.sendMessage( message );
            } else if (chatType == ChatItem.GROUP_CHAT) {
                mulChat.sendMessage( message );
            }
        }
    }

    //发送消息，附带参数
    public void sendMsgWithParms(String msg, String[] parms, Object[] datas, int chatType) throws Exception {
        if (getConnection() == null) {
            throw new Exception( "XmppException" );
        }
        Message message = new Message();
        for (int i = 0; i < datas.length; i++) {
            message.addSubject( parms[i], (String) datas[i] );
        }
        message.setBody( msg );
        if (chatType == ChatItem.CHAT) {
            newchat.sendMessage( message );
        } else if (chatType == ChatItem.GROUP_CHAT) {
            mulChat.sendMessage( msg + ":::" + datas[0] );
        }
    }

    /**
     * 搜索好友
     *
     * @param key
     * @return
     */
    public Observable<List<String>> searchUser(String key) {
        return Observable.create( subscriber -> {
            List<String> userList = new ArrayList<>();
            try {
                UserSearchManager search = new UserSearchManager( getConnection() );
                Form searchForm = search.getSearchForm( "search." + Constants.SERVER_NAME );
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer( "Username", true );
                answerForm.setAnswer( "search", key );
                ReportedData data = search.getSearchResults( answerForm, "search." + Constants.SERVER_NAME );
                Iterator<Row> it = data.getRows().iterator();
                Row row = null;
                while (it.hasNext()) {
                    row = it.next();
                    String username = row.getValues( "Username" ).toString();
                    userList.add( username.substring( 1, username.length() - 1 ) );
                }
                subscriber.onNext( userList );
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError( new Throwable( e.getMessage() ) );
            }
        } );
    }

    /**
     * 添加好友 无分组
     *
     * @param userName id
     * @param
     */
    public boolean addUser(String userName) {
        if (getConnection() == null)
            return false;
        try {
            Roster.getInstanceFor( getConnection() ).createEntry( getFullUsername( userName ), getFullUsername( userName ), null );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除好友
     *
     * @param userName
     * @return
     */
    public boolean removeUser(String userName) {
        if (getConnection() == null)
            return false;
        try {
            RosterEntry entry = null;
            if (userName.contains( "@" ))
                entry = Roster.getInstanceFor( getConnection() ).getEntry( userName );
            else
                entry = Roster.getInstanceFor( getConnection() ).getEntry( userName + "@" + getConnection().getServiceName() );
            if (entry == null)
                entry = Roster.getInstanceFor( getConnection() ).getEntry( userName );
            Roster.getInstanceFor( getConnection() ).removeEntry( entry );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改用户信息
     *
     * @param
     */
    public boolean changeVcard(VCard vcard) {
        if (getConnection() == null)
            return false;
        try {
            vcard.save( getConnection() );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 修改用户头像
     *
     * @param file
     */
    public Bitmap changeImage(File file) {
        Bitmap bitmap = null;
        if (getConnection() == null)
            return bitmap;
        try {
            VCard vcard = getUserInfo( null );
            byte[] bytes;
            bytes = getFileBytes( file );
            String encodedImage = StringUtils.encodeHex( bytes );
            vcard.setField( "avatar", encodedImage );
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            bitmap = BitmapFactory.decodeStream( bais );
            vcard.save( getConnection() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取用户信息
     *
     * @param user
     * @return
     */
    public VCard getUserInfo(String user) {  //null 时查自己
        try {
            VCard vcard = new VCard();
            if (user == null) {
                vcard.load( getConnection() );
            } else {
                vcard.load( getConnection(), user + "@" + Constants.SERVER_NAME );
            }
            if (vcard != null)
                return vcard;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取用户头像信息
     *
     * @param
     * @param user
     * @return
     */
    public Bitmap getUserImage(String user) {  //null 时查自己

        ByteArrayInputStream bais = null;
        try {
            VCard vcard = new VCard();
            if (user == null) {
                vcard.load( getConnection() );
            } else {
                vcard.load( getConnection(), user + "@" + Constants.SERVER_NAME );
            }
            if (vcard == null || vcard.getAvatar() == null)
                return null;
            bais = new ByteArrayInputStream( vcard.getAvatar() );

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bais == null)
            return null;
        return BitmapFactory.decodeStream( bais );
    }

    /**
     * 文件转字节
     *
     * @param file
     * @throws IOException
     * @returnq
     */
    private byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream( new FileInputStream( file ) );
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read( buffer );
            if (readBytes != buffer.length) {
                throw new IOException( "Entire file not read" );
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * 创建房间
     *
     * @param roomName 房间名称
     */
    public MultiUserChat createRoom(String roomName) {
        if (getConnection() == null) {
            return null;
        }
        MultiUserChat muc = null;
        try {
            // 创建一个MultiUserChat
            muc = MultiUserChatManager.getInstanceFor( getConnection() ).getMultiUserChat( roomName + "@conference."
                    + getConnection().getServiceName() );
            muc.create( roomName );
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            List<FormField> fields = form.getFields();
            for (int i = 0; fields != null && i < fields.size(); i++) {
                if (FormField.Type.hidden != fields.get( i ).getType() && fields.get( i ).getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer( fields.get( i ).getVariable() );
                }
                // 设置聊天室是持久聊天室，即将要被保存下来
                submitForm.setAnswer( "muc#roomconfig_persistentroom", true );
                // 房间仅对成员开放
                submitForm.setAnswer( "muc#roomconfig_membersonly", false );
                // 允许占有者邀请其他人
                submitForm.setAnswer( "muc#roomconfig_allowinvites", true );
                // 登录房间对话
                submitForm.setAnswer( "muc#roomconfig_enablelogging", true );
                // 仅允许注册的昵称登录
                submitForm.setAnswer( "x-muc#roomconfig_reservednick", true );
                // 允许使用者修改昵称
                submitForm.setAnswer( "x-muc#roomconfig_canchangenick", false );
                // 允许用户注册房间
                submitForm.setAnswer( "x-muc#roomconfig_registration", false );
                // 发送已完成的表单（有默认值）到服务器来配置聊天室
                muc.sendConfigurationForm( submitForm );
            }
        } catch (XMPPException | SmackException e) {
            e.printStackTrace();
            return null;
        }
        return muc;
    }

    public Observable<List<Friend>> getFriend() {
        friendList.clear();
        return Observable.just( Roster.getInstanceFor( getConnection() ).getEntries() )
                .flatMap( entries -> {
                    List<Friend> friendsTemp = new ArrayList<>();
                    for (RosterEntry entry : entries) {
                        Friend friend = new Friend();
                        String username = getUsername( entry.getUser() );
                        ItemType type = entry.getType();
                        String nickName = "";
                        String userHead = "";
                        VCard vCard = getUserInfo( username );
                        if (vCard != null) {
                            nickName = vCard.getField( "nickName" );
                            userHead = vCard.getField( "avatar" );
                            if (nickName == null) {
                                nickName = username;
                            }
                        }
                        friend.setUserHead( userHead );
                        friend.setUsername( username );
                        friend.setNickname( nickName );
                        friend.setType( type );
                        friendsTemp.add( friend );
                    }
                    return Observable.just( friendsTemp );
                } )
                .flatMap( list -> {
                    Friend[] usersArray = new Friend[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Friend friend = new Friend();
                        friend.setUsername( list.get( i ).getUsername() );
                        friend.setNickname( list.get( i ).getNickname() );
                        friend.setType( list.get( i ).getType() );
                        friend.setUserHead( list.get( i ).getUserHead() );
                        usersArray[i] = friend;
                    }
                    Arrays.sort( usersArray, new PinyinComparator() );
                    friendList = new ArrayList<>( Arrays.asList( usersArray ) );
                    return Observable.just( friendList );
                } );
    }

    public List<Friend> getFriendList() {
        return friendList;
    }

    public void changeFriend(Friend friend, ItemType type) {
        getFriendList().get( getFriendList().indexOf( friend ) ).setType( type );
    }


    public List<Room> getMyRoom() {
        return myRooms;
    }

    /**
     * 加入会议室
     *
     * @param user      昵称
     * @param roomsName 会议室名
     */
    public MultiUserChat joinMultiUserChat(String user, String roomsName, boolean restart) {
        if (getConnection() == null)
            return null;
        try {
            // 使用XMPPConnection创建一个MultiUserChat窗口
            MultiUserChat muc = MultiUserChatManager.getInstanceFor( getConnection() ).getMultiUserChat( roomsName
                    + "@conference." + getConnection().getServiceName() );
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            SimpleDateFormat sim = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences( "user", Context.MODE_PRIVATE );
            String str = preferences.getString( "time", "defaultname" );
            Date d = sim.parse( str );
            history.setSince( d );
            // 用户加入聊天室
            muc.join( user, null, history, SmackConfiguration.getDefaultPacketReplyTimeout() );
            return muc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (restart) {
                reconnect();
            }
        }
    }

    public void leaveMuc(String roomName) {
        // 使用XMPPConnection创建一个MultiUserChat窗口
        MultiUserChat muc = MultiUserChatManager.getInstanceFor( getConnection() ).getMultiUserChat( roomName
                + "@conference." + getConnection().getServiceName() );
        try {
            muc.leave();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过jid获得username
     *
     * @param fullUsername
     * @return
     */
    public static String getUsername(String fullUsername) {
        return fullUsername.split( "@" )[0];
    }

    /**
     * 通过username获得jid
     *
     * @param username
     * @return
     */
    public static String getFullUsername(String username) {
        return username + "@" + Constants.SERVER_NAME;
    }

    /**
     * 通过roomjid获取房间名
     *
     * @param fullRoomname
     * @return
     */
    public static String getRoomName(String fullRoomname) {
        return fullRoomname.split( "@" )[0];
    }

    /**
     * 通过roomjid获取发送者
     *
     * @param fullRoomname
     * @return
     */
    public static String getRoomUserName(String fullRoomname) {
        return fullRoomname.split( "/" )[1];
    }

    /**
     * 通过roomName获得roomji
     *
     * @param roomName
     * @return
     */
    public static String getFullRoomname(String roomName) {
        return roomName + "@conference." + Constants.SERVER_NAME;
    }

    class MUCPacketExtensionProvider extends IQProvider {
        @Override
        public Element parse(XmlPullParser parser, int i) throws XmlPullParserException, IOException, SmackException {
            int eventType = parser.getEventType();
            myRooms.clear();
            leaveRooms.clear();
            Room info = null;
            while (true) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("room".equals( parser.getName() )) {
                        String roomName = parser.getAttributeValue( "", "roomName" );
                        String roomJid = parser.getAttributeValue( "", "roomJid" );
                        info = new Room();
                        info.name = roomName;
                        info.roomid = roomJid;
                        myRooms.add( info );
                    }
                    if ("friend".equals( parser.getName() )) {
                        info.friendList.add( XmppConnection.getUsername( parser.nextText() ) );
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("muc".equals( parser.getName() )) {
                        break;
                    }
                }
                eventType = parser.next();
            }
            return null;
        }
    }
}
