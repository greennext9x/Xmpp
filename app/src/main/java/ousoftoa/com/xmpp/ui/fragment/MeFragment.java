package ousoftoa.com.xmpp.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseFragment;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.presenter.MePresenter;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.ui.activity.LoginActivity;
import ousoftoa.com.xmpp.ui.activity.UserInfoActivity;
import ousoftoa.com.xmpp.ui.view.MeView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.ImageUtil;

/**
 * Created by 韩莫熙 on 2017/4/6.
 */

public class MeFragment extends BaseFragment<MePresenter> implements MeView {
    @Bind(R.id.ivHeader)
    ImageView mIvHeader;
    @Bind(R.id.tvName)
    TextView mTvName;
    @Bind(R.id.tvAccount)
    TextView mTvAccount;
    @Bind(R.id.llMyInfo)
    LinearLayout mLlMyInfo;

    @Override
    public boolean isEventBus() {
        return true;
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate( R.layout.fragment_me, null );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new MePresenter( mContext, this );
    }

    @Override
    protected void init() {
        initListener();
        mPresenter.getUserInfo();
    }

    private void initListener() {
        RxView.clicks( mLlMyInfo )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> startActivity( new Intent( mContext,UserInfoActivity.class) ) );
    }

    @OnClick(R.id.oivout)
    public void onClick() {
        DataHelper.SetBooleanSF( mContext, Constants.LOGIN_CHECK,false );
        XmppConnection.getInstance().closeConnection();
        startActivity( new Intent( mContext, LoginActivity.class ) );
        getActivity().finish();
    }

    @Override
    public void onNext(Friend userfriend) {
        String userhead = userfriend.getUserHead();
        Bitmap headimg = ImageUtil.getBitmapFromBase64String( userhead );
        String nickName = userfriend.getNickname();
        String username = userfriend.getUsername();
        if (headimg != null)
            mIvHeader.setImageBitmap( headimg );
        mTvName.setText( nickName );
        mTvAccount.setText( "坑聊号:" + username );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changefriend(MessageEvent event){
        if (event.getTag().equals( "changeVcard" ))
            mPresenter.getNowUserInfo();
    }
}
