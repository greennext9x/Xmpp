package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.UserInfoView;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/18.
 */

public class UserInfoPresenter extends BasePresenter {
    private UserInfoView mView;

    public UserInfoPresenter(Context mContext, UserInfoView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getUserInfo() {
        Observable.create( (Observable.OnSubscribe<VCard>) subscriber -> {
            VCard vcard = XmppConnection.getInstance().getUserInfo( null );
            if (vcard != null) {
                subscriber.onNext( vcard );
                subscriber.onCompleted();
            } else {
                subscriber.onError( new Throwable( "获取失败" ) );
            }
        } ).compose( RxUtils.applySchedulers( mView ) )
                .subscribe( vCard -> mView.onNext( vCard )
                        , throwable -> mView.onError( throwable ) );
    }

    public void setPortrait(String path) {
        Observable.just( ImageUtil.getBase64StringFromFile( path ) )
                .map( s -> {
                    VCard mvcar = new VCard();
                    mvcar.setField( "avatar", s );
                    return mvcar;
                } )
                .flatMap( vCard -> XmppConnection.getInstance().changeVcard( vCard ) )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( bitmap -> {
                    EventBus.getDefault().post( new MessageEvent( "changeVcard", "" ) );
                    getUserInfo();
                }, throwable -> mView.onError( throwable ) );
    }

}
