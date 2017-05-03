package ousoftoa.com.xmpp.presenter;

import android.content.Context;
import android.util.Log;

import java.util.List;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.ChatItem;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.ChatView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/20.
 */

public class ChatPresenter extends BasePresenter {
    private ChatView mView;
    private int chatType;
    private String chatName;

    public ChatPresenter(Context mContext, ChatView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getChat(String chatName, int chatType) {
        this.chatType = chatType;
        this.chatName = chatName;
        Observable.create( (Observable.OnSubscribe<List<ChatItem>>) subscriber -> {
            List<ChatItem> mData = MsgDbHelper.getInstance( mContext ).getChatMsg( chatName );
            subscriber.onNext( mData );
            subscriber.onCompleted();
        } ).compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( itemList -> mView.onNext( itemList ) );
    }

    public void sendMsg(String suject, String msg) {
        XmppConnection.getInstance().sendMsg( chatName, suject, msg, chatType )
                .compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( o -> Log.i( "debug", "sendSound: " + msg ), throwable -> mView.onSendFail() );
    }

    public void loadMore(int count) {
        Observable.create( (Observable.OnSubscribe<List<ChatItem>>) subscriber -> {
            List<ChatItem> mData = MsgDbHelper.getInstance( mContext ).getChatMsgMore( count,chatName );
            subscriber.onNext( mData );
            subscriber.onCompleted();
        } ).compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( itemList -> mView.onRefreshNext( itemList ) );
    }
}
