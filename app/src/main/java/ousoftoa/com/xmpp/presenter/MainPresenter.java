package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.MainView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.RxUtils;

/**
 * Created by 韩莫熙 on 2017/4/6.
 */

public class MainPresenter extends BasePresenter {
    private MainView mView;

    public MainPresenter(Context mContext, MainView view) {
        super( mContext );
        this.mView = view;
    }

    public void toLogin() {
        String name = DataHelper.getStringSF( mContext, Constants.LOGIN_ACCOUNT );
        String password = DataHelper.getStringSF( mContext, Constants.LOGIN_PWD );
        XmppConnection.getInstance().login( name, password )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( list -> {
                    DataHelper.saveDeviceData( mContext, name, list );
                }, throwable -> mView.showTip( throwable.getMessage() ) );
    }
}
