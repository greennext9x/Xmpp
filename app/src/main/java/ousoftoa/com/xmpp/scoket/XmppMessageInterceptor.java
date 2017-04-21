package ousoftoa.com.xmpp.scoket;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.utils.DateUtil;
import ousoftoa.com.xmpp.utils.FileUtil;

public class XmppMessageInterceptor implements StanzaListener {
    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        Message nowMessage = (Message) stanza;
        if (nowMessage.getType() == Message.Type.groupchat || nowMessage.getType() == Message.Type.chat) {
            String chatName = "";
            String userName = "";
            String nickName = "";
            String userHead = "";
            int chatType = ChatItem.CHAT;
            if (nowMessage.getType() == Message.Type.groupchat) {
                chatName = XmppConnection.getRoomName(nowMessage.getTo());
                userName = nowMessage.getTo();
                VCard vCard = XmppConnection.getInstance().getUserInfo(userName);
                if (vCard != null) {
                    nickName = vCard.getField("nickName");
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = chatName;
                    }
                }
                chatType = ChatItem.GROUP_CHAT;
            } else {
                chatName = userName = XmppConnection.getUsername(nowMessage.getTo());
                VCard vCard = XmppConnection.getInstance().getUserInfo(userName);
                if (vCard != null) {
                    nickName = vCard.getField("nickName");
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = userName;
                    }
                }
            }
            // 记录我们发出去的消息
            String msgBody;
            if (nowMessage.getSubject("imgData") != null) {
                if (FileUtil.getType(nowMessage.getBody()) == FileUtil.SOUND)
                    msgBody = Constants.SAVE_SOUND_PATH + "/" + nowMessage.getBody();
                else
                    msgBody = Constants.SAVE_IMG_PATH + "/" + nowMessage.getBody();
            } else if (nowMessage.getType() == Message.Type.groupchat & nowMessage.getBody().contains(":::")) { //被迫的
                String[] msgAndData = nowMessage.getBody().split(":::");
                if (FileUtil.getType(msgAndData[0]) == FileUtil.SOUND)
                    msgBody = Constants.SAVE_SOUND_PATH + "/" + msgAndData[0];
                else
                    msgBody = Constants.SAVE_IMG_PATH + "/" + msgAndData[0];
            } else
                msgBody = nowMessage.getBody();

            if (nowMessage.getBody().contains("[RoomChange")) {
                System.out.println("房间要发生改变了");
            } else {
                ChatItem msg = new ChatItem(chatType, chatName, nickName, userName, userHead, msgBody, DateUtil.getNow(), 1);
                MsgDbHelper.getInstance(MyApplication.getInstance()).saveChatMsg(msg);
                EventBus.getDefault().post( new MessageEvent( "ChatNewMsg","" ) );
            }
        }
    }
}
