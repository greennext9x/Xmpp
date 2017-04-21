package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.List;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.view.SearchUserView;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

/**
 * Created by 韩莫熙 on 2017/4/12.
 */

public class SearchUserPersenter extends BasePresenter {
    private SearchUserView mView;

    public SearchUserPersenter(Context mContext, SearchUserView view) {
        super( mContext );
        this.mView = view;
    }

    public void getUser(String key) {
        XmppConnection.getInstance().searchUser( key )
                .compose( RxUtils.applySchedulers( mView ) )
                .subscribe( strings -> {
                    getFriendlist( strings );
                }, throwable -> mView.onError( throwable ) );
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
                .subscribe( list -> mView.onNext( list ) );
    }
}
