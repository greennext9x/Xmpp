package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.List;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.dao.NewFriendDbHelper;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.NewFriendView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/12.
 */

public class NewFriendPresenter extends BasePresenter{
    private NewFriendView mView;

    public NewFriendPresenter(Context mContext,NewFriendView mView) {
        super( mContext );
        this.mView = mView;
    }

    public void getNewFriend(){
        Observable.just( NewFriendDbHelper.getInstance(mContext).getNewFriend())
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( strings -> {
                    getFriendlist( strings );
                } );
    }

    public void getFriendlist(List<String> mlist) {
        Observable.from( mlist )
                .map( s -> {
                    VCard vCard = XmppConnection.getInstance().getUserInfo( s );
                    Friend friend = new Friend();
                    String nickName = "";
                    String userHead = "";
                    nickName = vCard.getField( "nickName" );
                    userHead = vCard.getField( "avatar" );
                    if (nickName == null) {
                        nickName = s;
                    }
                    friend.setUsername( s );
                    friend.setNickname( nickName );
                    friend.setUserHead( userHead );
                    return friend;
                } )
                .toList()
                .subscribe( list -> mView.onNext(list) );
    }


    public void addFriend(String username){
        Observable.create( (Observable.OnSubscribe<Boolean>) subscriber -> {
            boolean isSucces = XmppConnection.getInstance().addUser( username );
            if (isSucces){
                subscriber.onNext( true );
            }else {
                subscriber.onNext( false );
            }
            subscriber.onCompleted();
        } ).compose( RxUtils.applySchedulers( mView ) )
                .subscribe( aBoolean -> {
                    if (aBoolean){
                        mView.showTip( "添加成功" );
                        mView.onAddNext();
                    }else {
                        mView.showTip( "添加失败" );
                    }
                },throwable -> mView.onError(throwable));
    }
}
