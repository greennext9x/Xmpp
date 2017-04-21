package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.dao.MsgDbHelper;
import ousoftoa.com.xmpp.ui.view.MessageView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/14.
 */

public class MessagePresenter extends BasePresenter{
    private MessageView mView;

    public MessagePresenter(Context mContext,MessageView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getMessge(){
        Observable.just( MsgDbHelper.getInstance( mContext ).getLastMsg() )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( itemList -> {
                    mView.onNext( itemList );
                } );
    }
}
