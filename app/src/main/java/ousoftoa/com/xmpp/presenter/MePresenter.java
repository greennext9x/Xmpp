package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.base.MyApplication;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.MeView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/18.
 */

public class MePresenter extends BasePresenter {
    private MeView mView;

    public MePresenter(Context mContext, MeView meView) {
        super( mContext );
        this.mView = meView;
    }

    public void getUserInfo() {
        Friend userfriend = DataHelper.getDeviceData( mContext, Constants.USERINFO );
        if (userfriend != null)
            mView.onNext( userfriend );
        else
            mView.onError( new Throwable( "获取个人信息失败" ) );
    }

    public void getNowUserInfo() {
        Observable.create( (Observable.OnSubscribe<Friend>) subscriber -> {
            VCard vCard = XmppConnection.getInstance().getUserInfo( null );
            if (vCard != null) {
                Friend mfriend = new Friend();
                String username = Constants.USER_NAME;
                String nikename = vCard.getField( "nickName" );
                String userheard = vCard.getField( "avatar" );
                if (nikename == null) {
                    nikename = username;
                }
                mfriend.setUsername( username );
                mfriend.setUserHead( userheard );
                mfriend.setNickname( nikename );
                DataHelper.saveDeviceData( MyApplication.getInstance(), Constants.USERINFO, mfriend );
                subscriber.onNext( mfriend );
                subscriber.onCompleted();
            } else
                subscriber.onError( new Throwable( "获取信息失败" ) );
        } ).compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( friend -> mView.onNext( friend ), throwable -> mView.onError( throwable ) );
    }
}
