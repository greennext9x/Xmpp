package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import java.util.List;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.ContactsView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/7.
 */

public class ContactsPresenter extends BasePresenter {
    private ContactsView mView;

    public ContactsPresenter(Context mContext, ContactsView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getFriend() {
        String name = DataHelper.getStringSF( mContext, Constants.LOGIN_ACCOUNT );
        Observable.just( DataHelper.<List<Friend>>getDeviceData( mContext, name ) )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( list -> mView.onNext( list )
                        , throwable -> mView.onError( throwable ) );
    }

    public void changeFriend() {
        String name = DataHelper.getStringSF( mContext, Constants.LOGIN_ACCOUNT );
        XmppConnection.getInstance().getFriend()
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( list -> {
                    DataHelper.saveDeviceData( mContext, name, list );
                    mView.onNext( list );
                }, throwable ->  mView.onError( throwable ) );
    }
}
