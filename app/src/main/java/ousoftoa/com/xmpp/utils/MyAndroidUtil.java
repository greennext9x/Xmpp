package ousoftoa.com.xmpp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.ui.activity.MainActivity;

public class MyAndroidUtil {
    private static NotificationCompat.Builder builder = new NotificationCompat.Builder( MyApplication.getInstance().getContext() );

    public static void showNoti(String subject, String notiMsg, String nickname, Bitmap bitmap) {
        if (subject.equals( Constants.SEND_IMG ))
            builder.setContentText( "[图片]" );
        else if (subject.equals( Constants.SEND_SOUND ))
            builder.setContentText( "[语音]" );
        else if (subject.equals( Constants.SEND_LOCATION ))   //适配表情
            builder.setContentText( "[位置]" );
        else
            builder.setContentText( notiMsg );

        //设置点击跳转
        Intent intent = new Intent();
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        intent.setClass( MyApplication.getInstance(), MainActivity.class );
        NotificationManager notificationManager =
                (NotificationManager) MyApplication.getInstance().getSystemService( Service.NOTIFICATION_SERVICE );
        PendingIntent pendingIntent = PendingIntent.getActivity( MyApplication.getInstance(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT );
        if (bitmap != null)
            builder.setLargeIcon( bitmap );
        builder.setSmallIcon( R.mipmap.icon_kengchat )
                .setContentTitle( nickname )
                .setAutoCancel( true )//点击消失
                .setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL
                        | Notification.DEFAULT_SOUND )//向通知添加声音、闪灯和振动
                .setPriority( Notification.PRIORITY_MAX )//设置该通知优先级，用来浮动显示通知
                .setWhen( System.currentTimeMillis() )//通知首次出现在通知栏，带上升动画效果的
                .setContentIntent( pendingIntent );
        notificationManager.notify( 0, builder.build() );
    }
}
