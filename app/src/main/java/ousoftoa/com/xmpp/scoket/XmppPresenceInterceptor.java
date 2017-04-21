package ousoftoa.com.xmpp.scoket;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.packet.RosterPacket;

import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;

public class XmppPresenceInterceptor implements StanzaListener {

    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        Presence presence = (Presence) stanza;
        String from = presence.getFrom();// 发送方
        String to = presence.getTo();// 接收方
        // Presence.Type有7中状态
        if (presence.getType().equals( Presence.Type.subscribe)) {
            for (Friend friend : XmppConnection.getInstance().getFriendList()) {
                if ((friend.getUsername().equals(XmppConnection.getUsername(to)) && friend.getType() == RosterPacket.ItemType.from)) {
                    XmppConnection.getInstance().changeFriend(friend, RosterPacket.ItemType.both);
                } else if (friend.getUsername().equals(XmppConnection.getUsername(to))) {
                    XmppConnection.getInstance().changeFriend(friend, RosterPacket.ItemType.to);
                }
            }

            if (!XmppConnection.getInstance().getFriendList().contains(XmppConnection.getUsername(to))) {
                Friend friend = new Friend();
                friend.setUsername( XmppConnection.getUsername(to) );
                friend.setType( RosterPacket.ItemType.to );
                XmppConnection.getInstance().getFriendList().add(friend);
            }
            EventBus.getDefault().post( new MessageEvent( "friendChange","" ) );
        } else if (presence.getType().equals( Presence.Type.unsubscribe) || presence.getType().equals( Presence.Type.unsubscribed)) {
            for (Friend friend : XmppConnection.getInstance().getFriendList()) {
                if (friend.getUsername().equals(XmppConnection.getUsername(to))) {
                    XmppConnection.getInstance().changeFriend(friend, RosterPacket.ItemType.remove);
                }
            }
            EventBus.getDefault().post( new MessageEvent( "friendChange","" ) );
        }
    }
}
