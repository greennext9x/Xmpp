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
	public String subject;
	public String chatName; //群聊的话跟username不一样
	public String nickName;
	public String username;  //对方的昵称
	public String head;
	public String msg;
	public long sendDate;
	public int inOrOut; //0代表in 1代表out

	public ChatItem(){}

	public ChatItem(int chatType, String subject, String chatName, String nickName, String username, String head, String msg, long sendDate,
					int inOrOut) {
		this.chatType = chatType;
		this.subject = subject;
		this.chatName = chatName;
		this.nickName = nickName;
		this.username = username;
		this.head = head;
		this.msg = msg;
		this.sendDate = sendDate;
		this.inOrOut = inOrOut;
	}

	public String getChatName() {
		return chatName;
	}

	public void setChatName(String chatName) {
		this.chatName = chatName;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int getItemType() {
		return inOrOut;
	}

	@Override
	public String toString() {
		return "ChatItem{" +
				"chatType=" + chatType +
				", subject='" + subject + '\'' +
				", chatName='" + chatName + '\'' +
				", nickName='" + nickName + '\'' +
				", username='" + username + '\'' +
				", head='" + head + '\'' +
				", msg='" + msg + '\'' +
				", sendDate=" + sendDate +
				", inOrOut=" + inOrOut +
				'}';
	}
}
