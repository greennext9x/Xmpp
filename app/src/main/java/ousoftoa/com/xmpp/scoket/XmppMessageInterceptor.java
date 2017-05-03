package ousoftoa.com.xmpp.scoket;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.utils.DateUtil;

public class XmppMessageInterceptor implements StanzaListener {
    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        Message nowMessage = (Message) stanza;
        if (nowMessage.getType() == Message.Type.groupchat || nowMessage.getType() == Message.Type.chat) {
            String chatName;
            String userName;
            String nickName = "";
            String userHead = "";
            int chatType = ChatItem.CHAT;
            if (nowMessage.getType() == Message.Type.groupchat) {
                chatName = XmppConnection.getRoomName( nowMessage.getTo() );
                userName = nowMessage.getTo();
                VCard vCard = XmppConnection.getInstance().getUserInfo( userName );
                if (vCard != null) {
                    nickName = vCard.getField( "nickName" );
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = chatName;
                    }
                }
                chatType = ChatItem.GROUP_CHAT;
            } else {
                chatName = userName = XmppConnection.getUsername( nowMessage.getTo() );
                VCard vCard = XmppConnection.getInstance().getUserInfo( userName );
                if (vCard != null) {
                    nickName = vCard.getField( "nickName" );
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = userName;
                    }
                }
            }
            // 记录我们发出去的消息
            String msgBody = nowMessage.getBody();
            String subject = nowMessage.getSubject();
            if (nowMessage.getBody().contains( "[RoomChange" )) {
                System.out.println( "房间要发生改变了" );
            } else {
                ChatItem msg = new ChatItem( chatType, subject, chatName, nickName, userName, userHead, msgBody, DateUtil.getNow(), 1 );
                MsgDbHelper.getInstance( MyApplication.getInstance() ).saveChatMsg( msg );
                EventBus.getDefault().post( new MessageEvent( "ChatNewMsg", "" ) );
            }
        }
    }
}
