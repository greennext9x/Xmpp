package ousoftoa.com.xmpp.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import immortalz.me.library.TransitionsHeleper;
import immortalz.me.library.bean.InfoBean;
import immortalz.me.library.method.InflateShowMethod;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.Friend;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.presenter.FriendPresenter;
import ousoftoa.com.xmpp.ui.view.FriendView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.ImageUtil;
import rx.Observable;

public class FriendInfoActivity extends BaseActivity<FriendPresenter> implements FriendView {

    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.ivHeader)
    ImageView mIvHeader;
    @Bind(R.id.tvAlias)
    TextView mTvAlias;
    @Bind(R.id.ivGender)
    ImageView mIvGender;
    @Bind(R.id.tvAccount)
    TextView mTvAccount;
    @Bind(R.id.tvArea)
    TextView mTvArea;
    @Bind(R.id.tvSignature)
    TextView mTvSignature;
    @Bind(R.id.rlMenu)
    RelativeLayout mRlMenu;
    @Bind(R.id.btnAddFriend)
    Button mBtnAddFriend;
    @Bind(R.id.btnDeleteFriend)
    Button mBtnDeleteFriend;
    private Friend mFriend;

    private boolean isShowbtndelete = false;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_friend_info );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new FriendPresenter( this, this );
    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        mFriend = (Friend) intent.getSerializableExtra( "friendinfo" );
        initToolbar();
        initTransition();
        mPresenter.getFriend( mFriend.getUsername() );
    }

    private void initTransition() {
        TransitionsHeleper.getInstance()
                .setShowMethod(new InflateShowMethod(this, R.layout.activity_friend_transition) {
                    @Override
                    public void loadCopyView(InfoBean bean, ImageView copyView) {
                        Bitmap headimg = ImageUtil.getBitmapFromBase64String(mFriend.getUserHead());
                        if (headimg != null){
                            copyView.setImageBitmap(headimg);
                        }else {
                            copyView.setImageResource(R.mipmap.default_tp);
                        }
                    }

                    @Override
                    public void loadTargetView(InfoBean bean, ImageView targetView) {
                        Bitmap headimg = ImageUtil.getBitmapFromBase64String(mFriend.getUserHead());
                        if (headimg != null){
                            targetView.setImageBitmap(headimg);
                        }else {
                            targetView.setImageResource(R.mipmap.default_tp);
                        }
                    }
                })
                .show(this, mIvHeader);
    }


    private void initToolbar() {
        mImBack.setOnClickListener( view -> finish() );
    }


    @OnClick({R.id.btnAddFriend, R.id.btnDeleteFriend, R.id.btn_cancel, R.id.btn_confim})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddFriend:
                mPresenter.addFriend( mFriend.getUsername() );
                break;
            case R.id.btnDeleteFriend:
                mRlMenu.setVisibility( View.VISIBLE );
                break;
            case R.id.btn_cancel:
                mRlMenu.setVisibility( View.GONE );
                break;
            case R.id.btn_confim:
                mPresenter.deleteFriend( mFriend.getUsername() );
                mRlMenu.setVisibility( View.GONE );
                break;
        }
    }

    @Override
    public void onNext(VCard vCard) {
        mTvAccount.setText( "坑聊号：" + mFriend.getUsername() );
        String nickname = vCard.getField( "nickName" );
        if (nickname != null) {
            mTvAlias.setText( nickname );
        } else {
            mTvAlias.setText( mFriend.getNickname() );
        }
        String sex = vCard.getField( "sex" );
        if (sex != null && sex.equals( "男" ))
            mIvGender.setImageResource( R.mipmap.ic_gender_male );
        else {
            mIvGender.setImageResource( R.mipmap.ic_gender_female );
        }
        if (vCard.getField( "area" ) != null)
            mTvArea.setText( vCard.getField( "area" ) );
        if (vCard.getField( "signature" ) != null)
            mTvSignature.setText( vCard.getField( "signature" ) );
        String name = DataHelper.getStringSF( this, Constants.LOGIN_ACCOUNT );
        Observable.from( DataHelper.<List<Friend>>getDeviceData( this, name ) )
                .map( friend -> {
                    if (friend.getUsername().equals( mFriend.getUsername() )) {
                        isShowbtndelete = true;
                    }
                    return friend;
                } )
                .toList()
                .subscribe( friend -> {
                    if (isShowbtndelete) {
                        mBtnDeleteFriend.setVisibility( View.VISIBLE );
                    } else {
                        mBtnAddFriend.setVisibility( View.VISIBLE );
                    }
                } );
    }

    @Override
    public void onAddNext() {
        EventBus.getDefault().post( new MessageEvent( "friendchange","" ) );
        finish();
    }

    @Override
    public void onDeleteNext() {
        EventBus.getDefault().post( new MessageEvent( "friendchange","" ) );
        finish();
    }
}
