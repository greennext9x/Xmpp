package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.ui.view.ChatView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/20.
 */

public class ChatPresenter extends BasePresenter{
    private ChatView mView;

    public ChatPresenter(Context mContext,ChatView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getChat(String chatName){
        Observable.just( MsgDbHelper.getInstance(mContext).getChatMsg(chatName) )
                .compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( itemList -> mView.onNext( itemList ) );
    }
}
