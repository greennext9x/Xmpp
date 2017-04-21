package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.LoginView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.RxUtils;

/**
 * Created by 韩莫熙 on 2017/4/5.
 */

public class LoginPresenter extends BasePresenter {
    private LoginView mView;

    public LoginPresenter(Context mContext, LoginView view) {
        super( mContext );
        this.mView = view;
    }

    public void toLogin(String name, String password) {
        XmppConnection.getInstance().login( name, password )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( list -> {
                            DataHelper.saveDeviceData( mContext, name, list );
                            DataHelper.SetBooleanSF( mContext, Constants.LOGIN_CHECK, true );
                            DataHelper.SetStringSF( mContext, Constants.LOGIN_ACCOUNT, name );
                            DataHelper.SetStringSF( mContext, Constants.LOGIN_PWD, password );
                            mView.onNext();
                        }, throwable ->
                                mView.showTip( throwable.getMessage() )
                );
    }
}
