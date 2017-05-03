package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.RegisterView;
import ousoftoa.com.xmpp.utils.RxUtils;

/**
 * Created by 韩莫熙 on 2017/4/26.
 */

public class RegisterPresenter extends BasePresenter{
    private RegisterView mView;

    public RegisterPresenter(Context mContext,RegisterView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void toRegister(String name, String password){
        XmppConnection.getInstance().regist( name, password )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( aBoolean -> mView.onNext(),throwable -> mView.onError( throwable ) );
    }
}
