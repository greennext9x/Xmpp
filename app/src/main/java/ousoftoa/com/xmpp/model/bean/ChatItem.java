package ousoftoa.com.xmpp.model.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;


public class ChatItem implements Serializable,MultiItemEntity {
	public static final int CHAT = 0; 
	public static final int GROUP_CHAT = 1;
	public static final int NOTI = 2;
	public static final int CHAT_IN = 0;
	public static final int CHAT_OUT = 1;

	public int chatType;   // 0 chat  1 groupChat 2 noti
	public String chatName; //群聊的话跟username不一样
	public String nickName;
	public String username;  //对方的昵称
	public String head;
	public String msg;
	public long sendDate;
	public int inOrOut; //0代表in 1代表out

	public ChatItem(int chatType, String chatName, String nickName, String username, String head, String msg, long sendDate,
					int inOrOut) {
		this.chatType = chatType;
		this.chatName = chatName;
		this.nickName = nickName;
		this.username = username;
		this.head = head;
		this.msg = msg;
		this.sendDate = sendDate;
		this.inOrOut = inOrOut;
	}

	@Override
	public int getItemType() {
		return inOrOut;
	}
}
