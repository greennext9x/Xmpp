package ousoftoa.com.xmpp.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.dao.NewMsgDbHelper;
import ousoftoa.com.xmpp.ui.activity.MainActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyAndroidUtil {
	private static NotificationCompat.Builder builder = new NotificationCompat.Builder( MyApplication.getInstance().getContext());

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showNoti(String notiMsg, String nickname){
		//android推送
		if(notiMsg.contains( Constants.SAVE_IMG_PATH)){
			builder.setContentText("[图片]");
		}
		else if(notiMsg.contains(Constants.SAVE_SOUND_PATH)){
			builder.setContentText("[语音]");
		}
		else if(notiMsg.contains("[/g0")){
			builder.setContentText("[动画表情]");
		}
		else if(notiMsg.contains("[/f0")) {  //适配表情
			builder.setContentText(ExpressionUtil.getText(MyApplication.getInstance(), StringUtil.Unicode2GBK(notiMsg)));
		}
		else if(notiMsg.contains("[/a0")) {
			builder.setContentText("[位置]");
		}
		else{
			builder.setContentText(notiMsg);
		}
		//5.0之后通知
		//设置点击跳转
		Intent hangIntent = new Intent();
		hangIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
		hangIntent.setClass(MyApplication.getInstance(), MainActivity.class);
		NotificationManager notificationManager =
				(NotificationManager) MyApplication.getInstance().getSystemService( Service.NOTIFICATION_SERVICE);
		// 通知产生的时间，会在通知信息里显示
		// 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
		builder.setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL
				| Notification.DEFAULT_SOUND );
		builder.setSmallIcon( R.mipmap.ic_launcher);
		// 设置该通知优先级，用来浮动显示通知
		builder.setPriority( Notification.PRIORITY_MAX );
		// 通知首次出现在通知栏，带上升动画效果的
		builder.setWhen( System.currentTimeMillis());
		builder.setLargeIcon( BitmapFactory.decodeResource( Resources.getSystem(), R.mipmap.ic_launcher));
		// 点击消失
		builder.setAutoCancel(true);
		builder.setContentTitle(nickname);
		builder.setNumber( NewMsgDbHelper.getInstance(MyApplication.getInstance()).getMsgCount());
		PendingIntent hangPendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 0,
				hangIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent( hangPendingIntent );
		notificationManager.notify(0, builder.build());
	}
}
