package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.FriendView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by 韩莫熙 on 2017/4/11.
 */

public class FriendPresenter extends BasePresenter {
    private FriendView mView;

    public FriendPresenter(Context mContext, FriendView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getFriend(String username) {
        Observable.create( new Observable.OnSubscribe<VCard>() {
            @Override
            public void call(Subscriber<? super VCard> subscriber) {
                VCard vCard = XmppConnection.getInstance().getUserInfo( username );
                if (vCard != null) {
                    subscriber.onNext( vCard );
                    subscriber.onCompleted();
                } else {
                    subscriber.onError( new Throwable( "获取好友信息失败" ) );
                }
            }
        } ).compose( RxUtils.bindToSchedulers( mView ) )
                .subscribe( vCard -> mView.onNext( vCard )
                        , throwable -> mView.onError( throwable ) );
    }

    public void addFriend(String username) {
        XmppConnection.getInstance().addUser( username )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( aBoolean -> {
                    mView.showTip( "添加成功" );
                    mView.onAddNext();
                }, throwable -> mView.showTip( throwable.getMessage() ) );
    }

    public void deleteFriend(String username) {
        XmppConnection.getInstance().removeUser( username )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( aBoolean -> {
                    mView.showTip( "删除成功" );
                    mView.onDeleteNext();
                }, throwable -> {
                    mView.showTip( "删除失败" );
                } );
    }
}
