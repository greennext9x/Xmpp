package ousoftoa.com.xmpp.scoket;

import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.text.SimpleDateFormat;
import java.util.Date;

import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.bean.Room;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.MyAndroidUtil;


public class XmppMessageListener implements StanzaListener {
    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        final Message nowMessage = (Message) stanza;
        if (String.valueOf( nowMessage.toXML() ).contains( "<invite" )) {
            String noti = "你被邀请加入群组" + XmppConnection.getRoomName( nowMessage.getFrom() );
            String userName = XmppConnection.getRoomName( nowMessage.getFrom() );
            String nickName = "";
            String userHead = "";
            VCard vCard = XmppConnection.getInstance().getUserInfo( userName );
            if (vCard != null) {
                nickName = vCard.getField( "nickName" );
                userHead = vCard.getField( "avatar" );
                if (nickName == null) {
                    nickName = userName;
                }
            }
            ChatItem msg = new ChatItem( ChatItem.NOTI, "", userName, nickName, userName, userHead, noti, DateUtil.getNow(), 0 );
            MyAndroidUtil.showNoti( noti, nickName );
            NewMsgDbHelper.getInstance( MyApplication.getInstance() ).saveNewMsg( userName );
            MsgDbHelper.getInstance( MyApplication.getInstance() ).saveChatMsg( msg );
            EventBus.getDefault().post( new MessageEvent( "ChatNewMsg", "" ) );
            XmppConnection.getInstance().joinMultiUserChat( Constants.USER_NAME, XmppConnection.getRoomName( nowMessage.getFrom() ), true );
        }

        Message.Type type = nowMessage.getType();
        if ((type == Message.Type.groupchat || type == Message.Type.chat) && nowMessage.getBody() != null) {
            String chatName = "";
            String userName = "";
            String nickName = "";
            String userHead = "";
            int chatType = ChatItem.CHAT;
            if (type == Message.Type.groupchat) {
                chatName = XmppConnection.getRoomName( nowMessage.getFrom() );
                userName = XmppConnection.getRoomUserName( nowMessage.getFrom() );
                chatType = ChatItem.GROUP_CHAT;
                VCard vCard = XmppConnection.getInstance().getUserInfo( userName );
                if (vCard != null) {
                    nickName = vCard.getField( "nickName" );
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = chatName;
                    }
                }
            } else {
                chatName = userName = XmppConnection.getUsername( nowMessage.getFrom() );
                VCard vCard = XmppConnection.getInstance().getUserInfo( userName );
                if (vCard != null) {
                    nickName = vCard.getField( "nickName" );
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = userName;
                    }
                }
            }

            if (!userName.equals( Constants.USER_NAME )) {
                long dateString;
                DelayInformation inf = nowMessage.getExtension( "x", "jabber:x:delay" );
                if (inf == null)
                    dateString = DateUtil.getNow();
                else
                    dateString = inf.getStamp().getTime();
                //msg
                ChatItem msg = null;
                String msgBody = nowMessage.getBody();
                String subject = nowMessage.getSubject();
                if (type == Message.Type.groupchat && XmppConnection.leaveRooms.contains( new Room( chatName ) )) {
                    System.out.println( "我已经离开这个房间了" );
                } else if (nowMessage.getBody().contains( "[RoomChange" )) {
                    XmppConnection.getInstance().reconnect();
                } else {
                    msg = new ChatItem( chatType, subject, chatName, nickName, userName, userHead, msgBody, dateString, 0 );
                    NewMsgDbHelper.getInstance( MyApplication.getInstance().getContext() ).saveNewMsg( chatName );
                    MsgDbHelper.getInstance( MyApplication.getInstance() ).saveChatMsg( msg );
                    EventBus.getDefault().post( new MessageEvent( "ChatNewMsg", "" ) );
                    SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences( "user", Context.MODE_PRIVATE );
                    SharedPreferences.Editor editor = preferences.edit();
                    Date date = new Date();
                    String dateStr = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( date );
                    editor.putString( "time", dateStr );
                    editor.commit();
                    MyAndroidUtil.showNoti( msgBody, nickName );
                }
            }
        }
    }
}
