package ousoftoa.com.xmpp.scoket;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemType;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.model.dao.NewFriendDbHelper;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.MyAndroidUtil;


public class XmppPresenceListener implements PacketListener {

    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        Presence presence = (Presence) stanza;
        String jid = presence.getFrom();
        String to = presence.getTo();
        //Presence.Type有7中状态
        if (presence.getType().equals( Presence.Type.subscribe)) {
            if (!XmppConnection.getInstance().getFriendList().contains(XmppConnection.getUsername(jid))) {
                Friend friend = new Friend();
                friend.setUsername( XmppConnection.getUsername(jid) );
                friend.setType( ItemType.from );
                XmppConnection.getInstance().getFriendList().add(friend);
            }
            for (Friend friend : XmppConnection.getInstance().getFriendList()) {
                if (friend.getUsername().equals(XmppConnection.getUsername(jid)) && friend.getType() == ItemType.to) {
                    String userName = XmppConnection.getUsername(jid);
                    String nickName = "";
                    String userHead = "";
                    VCard vCard = XmppConnection.getInstance().getUserInfo(userName);
                    if (vCard != null) {
                        nickName = vCard.getField("nickName");
                        userHead = vCard.getField( "avatar" );
                        if (nickName == null) {
                            nickName = userName;
                        }
                    }
                    MyAndroidUtil.showNoti(nickName + "同意添加好友", nickName);
                    ChatItem msg = new ChatItem(ChatItem.CHAT, userName, nickName, userName, userHead, userName + "同意添加好友", DateUtil.getNow(), 0);
                    NewMsgDbHelper.getInstance( MyApplication.getInstance()).saveNewMsg(userName);
                    MsgDbHelper.getInstance(MyApplication.getInstance()).saveChatMsg(msg);
                    XmppConnection.getInstance().changeFriend(friend, ItemType.both);
                } else if (friend.getUsername().equals(XmppConnection.getUsername(jid))) {
                    String nickName = "";
                    VCard vCard = XmppConnection.getInstance().getUserInfo(friend.getUsername());
                    if (vCard != null) {
                        nickName = vCard.getField("nickName");
                        if (nickName == null) {
                            nickName = friend.getUsername();
                        }
                    }
                    XmppConnection.getInstance().changeFriend(friend, ItemType.from);
                    MyAndroidUtil.showNoti(nickName + "申请好友", nickName);
                    NewFriendDbHelper.getInstance(MyApplication.getInstance()).saveNewFriend(XmppConnection.getUsername(jid));
                }
            }
            EventBus.getDefault().post( new MessageEvent( "ChatNewMsg","" ) );
        } else if (presence.getType().equals( Presence.Type.unsubscribe) || presence.getType().equals( Presence.Type.unsubscribed)) {
            for (Friend friend : XmppConnection.getInstance().getFriendList()) {
                if (friend.getUsername().equals(XmppConnection.getUsername(jid))) {
                    XmppConnection.getInstance().changeFriend(friend, ItemType.remove);
                }
            }
            EventBus.getDefault().post( new MessageEvent( "friendChange","" ) );
        }
    }
}
