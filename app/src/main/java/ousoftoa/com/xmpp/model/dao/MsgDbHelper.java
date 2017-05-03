package ousoftoa.com.xmpp.model.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.bean.Constants;

public class MsgDbHelper {
	private static MsgDbHelper instance = null;
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "chat";
	
	private SqlLiteHelper helper;
	private SQLiteDatabase db;  // 我的最新聊天信息
	private final int SHOW_MSG_COUNT = 10;
	private final int MORE_MSG_COUNT = 10 ;	
	
	public MsgDbHelper(Context context) {
		helper = new SqlLiteHelper(context);
		db = helper.getWritableDatabase();
	}

	public void closeDb(){
		db.close();
		helper.close();
	}
	public static MsgDbHelper getInstance(Context context) {
		if (instance == null) {
			instance = new MsgDbHelper(context);
		}
		return instance;
	}
	
	private class SqlLiteHelper extends SQLiteOpenHelper {


		public SqlLiteHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE  IF NOT EXISTS " + DB_NAME
						+ "( id INTEGER PRIMARY KEY AUTOINCREMENT,chatType INTEGER,subject text,chatName text,"+
						"nickName text,username text , head text ,msg text,sendDate text,inOrOut INTEGER," +
						"whos text,i_filed INTEGER,t_field text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			dropTable(db);
			onCreate(db);
		}

		private void dropTable(SQLiteDatabase db) {
			String sql = "DROP TABLE IF EXISTS "+DB_NAME;
			db.execSQL(sql);
		}

	}

	public void saveChatMsg(ChatItem msg){
		ContentValues values = new ContentValues();
		values.put("chatType", msg.chatType);
		values.put("subject", msg.subject);
		values.put("chatName", msg.chatName);
		values.put("nickName", msg.nickName);
		values.put("username", msg.username);
		values.put("head", msg.head);
		values.put("msg", msg.msg);
		values.put("sendDate",msg.sendDate);
		values.put("inOrOut", msg.inOrOut);
		values.put("whos", Constants.USER_NAME);
		db.insert(DB_NAME, "id", values);
	}

	/**
	 * 取当前会话窗口的聊天记录，限量count
	 */
	public List<ChatItem> getChatMsg(String chatName){
		List<ChatItem> chatItems = new ArrayList<ChatItem>();
		ChatItem msg;
		String sql = "select chatType,subject,chatName,nickName,username,head,msg,sendDate,inOrOut " +
				" from(select * from "+DB_NAME +
				" where chatName = ? and whos = ? order by id desc LIMIT " +SHOW_MSG_COUNT+")order by id";
		Cursor cursor = db.rawQuery(sql, new String[]{chatName,Constants.USER_NAME});
		while(cursor.moveToNext()){
			msg = new ChatItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6)
					, cursor.getLong(7), cursor.getInt(8));
			chatItems.add(msg);
		}
		cursor.close();
		return chatItems;
	}
	
	/**
	 * 获取更多好友聊天记录,显示多10条
	 */
	public List<ChatItem> getChatMsgMore(int startIndex, String chatName){
		List<ChatItem> chatItems = new ArrayList<>();
		ChatItem msg;
		String sql ="select chatType,subject,chatName,nickName,username,head,msg,sendDate,inOrOut " +
				" from(select * from "+DB_NAME +
				" where chatName = ? and whos = ? order by id desc LIMIT " +MORE_MSG_COUNT+" offset "+startIndex+")order by id";
		Cursor cursor = db.rawQuery(sql, new String[]{chatName,Constants.USER_NAME});
		while(cursor.moveToNext()){
			msg = new ChatItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6)
					, cursor.getLong(7), cursor.getInt(8));
			chatItems.add(msg);
		}
		cursor.close();
		return chatItems;
	}

	public int getChatMsgsize(String chatName){
		int size = 0;
		String sql ="select chatType,subject,chatName,nickName,username,head,msg,sendDate,inOrOut " +
				" from(select * from "+DB_NAME +
				" where chatName = ? and whos = ?"+")order by id";
		Cursor cursor = db.rawQuery(sql, new String[]{chatName,Constants.USER_NAME});
		size = cursor.getCount();
		return size;
	}

	/**
	 * 取得我的的最新消息，显示在好友表
	 */
	@SuppressWarnings("unused")
	public List<ChatItem> getLastMsg(){
		List<ChatItem> chatItems = new ArrayList<>();
		ChatItem msg;
		String sql ="select chatType,subject,chatName,nickName,username,head,msg,sendDate,inOrOut from  "+DB_NAME +
				" where whos = ? "+
				 " GROUP BY chatName "+
					"order by id desc";
		Cursor cursor = db.rawQuery(sql, new String[]{Constants.USER_NAME});
		while (cursor.moveToNext()) {
			msg = new ChatItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6)
					, cursor.getLong(7), cursor.getInt(8));
			chatItems.add(msg);
		}
		cursor.close();
		return chatItems;
	}
	
	/**
	 * 取得我的的最新消息，模糊搜索,显示在好友表
	 */
	@SuppressWarnings("unused")
	public List<ChatItem> getLastMsg(String keywords){
		List<ChatItem> chatItems = new ArrayList<>();
		ChatItem msg;
		String sql ="select chatType,subject,chatName,nickName,username,head,msg,sendDate,inOrOut from  "+DB_NAME +
			 	" where username like ? and whos = ? "+
				 " GROUP BY chatName "+
					" order by id desc";
		final Cursor cursor = db.rawQuery(sql, new String[]{"%"+keywords+"%",Constants.USER_NAME});
		while (cursor.moveToNext()) {
			msg = new ChatItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6)
					, cursor.getLong(7), cursor.getInt(8));
			chatItems.add(msg);
		}
		cursor.close();
		return chatItems;
	}
	
	public void delChatMsg(String msgId){
		db.delete(DB_NAME, "chatName=? and whos=?", new String[]{msgId,Constants.USER_NAME});
	}

	public void clear(){
		db.delete(DB_NAME, "id>?", new String[]{"0"});
	}
}
