package ousoftoa.com.xmpp.presenter;

import android.content.Context;

import ousoftoa.com.xmpp.base.BasePresenter;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.ui.view.MeView;
import ousoftoa.com.xmpp.utils.DataHelper;

/**
 * Created by 韩莫熙 on 2017/4/18.
 */

public class MePresenter extends BasePresenter{
    private MeView mView;

    public MePresenter(Context mContext,MeView meView) {
        super( mContext );
        this.mView = meView;
    }

    public void getUserInfo(){
        Friend userfriend = DataHelper.getDeviceData( mContext, Constants.USERINFO );
        if (userfriend != null)
            mView.onNext( userfriend );
        else
            mView.onError( new Throwable(  ) );
    }
}
