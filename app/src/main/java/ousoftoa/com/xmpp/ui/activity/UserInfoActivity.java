package ousoftoa.com.xmpp.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImageGridActivity;
import com.lqr.optionitemview.OptionItemView;
import com.nanchen.compresshelper.CompressHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.Constants;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.bean.UserInfoData;
import ousoftoa.com.xmpp.presenter.UserInfoPresenter;
import ousoftoa.com.xmpp.ui.view.UserInfoView;
import ousoftoa.com.xmpp.utils.DataHelper;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.wight.dialog.CheckDialog;

public class UserInfoActivity extends BaseActivity<UserInfoPresenter> implements UserInfoView {
    @Bind(R.id.llHeader)
    LinearLayout mLlHeader;
    @Bind(R.id.ivHeader)
    ImageView mIvHeader;
    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.oivName)
    OptionItemView mOivName;
    @Bind(R.id.oivAccount)
    OptionItemView mOivAccount;
    @Bind(R.id.oivQRCodeCard)
    OptionItemView mOivQRCodeCard;
    @Bind(R.id.oivGender)
    OptionItemView mOivGender;
    @Bind(R.id.oivSignature)
    OptionItemView mOivSignature;
    @Bind(R.id.oivArea)
    OptionItemView mOivArea;

    public static final int REQUEST_IMAGE_PICKER = 1000;
    private VCard mVCard;
    private CheckDialog mCheckDialog;

    @Override
    public boolean isEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        setContentView( R.layout.activity_user_info );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new UserInfoPresenter( this, this );
    }

    private void initListener() {
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
        RxView.clicks( mLlHeader )
                .throttleFirst( 1,TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    Intent intent = new Intent(this, ImageGridActivity.class);
                    startActivityForResult(intent, REQUEST_IMAGE_PICKER);
                } );
        RxView.clicks( mOivName )
                .throttleFirst( 1,TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    UserInfoData infoData = new UserInfoData();
                    infoData.setType( "nickName" );
                    infoData.setTitle( "修改昵称" );
                    Intent intent = new Intent( UserInfoActivity.this,ReviseActivity.class );
                    intent.putExtra( "userinfo", infoData );
                    startActivity( intent );
                } );
        RxView.clicks( mOivSignature )
                .throttleFirst( 1,TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    UserInfoData infoData = new UserInfoData();
                    infoData.setType( "signature" );
                    infoData.setTitle( "修改签名" );
                    Intent intent = new Intent( UserInfoActivity.this,ReviseActivity.class );
                    intent.putExtra( "userinfo", infoData );
                    startActivity( intent );
                } );
        RxView.clicks( mOivGender )
                .throttleFirst( 1,TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    mCheckDialog.setData( mVCard.getField( "sex" ) )
                    .setCheckListener( sex -> {
                        mPresenter.setPortrait( mVCard,"sex",sex );
                    } ).show();
                } );
    }

    @Override
    protected void init() {
        mCheckDialog = new CheckDialog( this );
        ImagePicker.getInstance().setMultiMode( false );
        initListener();
        mPresenter.getUserInfo();
    }

    @Override
    public void onNext(VCard vCard) {
        mVCard = vCard;
        String userheard = vCard.getField( "avatar" );
        String username = DataHelper.getStringSF( this, Constants.LOGIN_ACCOUNT );
        String nikename = vCard.getField( "nickName" );
        String sex = vCard.getField( "sex" );
        String area = vCard.getField( "area" );
        String signature = vCard.getField( "signature" );
        Bitmap headimg = ImageUtil.getBitmapFromBase64String( userheard );
        if (nikename == null)
            nikename = username;
        mOivName.setRightText( nikename );
        mOivAccount.setRightText( username );
        if (headimg != null)
            mIvHeader.setImageBitmap( headimg );
        if (sex != null)
            mOivGender.setRightText( sex );
        if (area != null)
            mOivArea.setRightText( area );
        if (signature != null)
            mOivSignature.setRightText( signature );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null) {
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        if (images != null && images.size() > 0) {
                            ImageItem imageItem = images.get(0);
                            File file = new File( imageItem.path );
                            File newFile = CompressHelper.getDefault(this).compressToFile(file);
                            String msg = ImageUtil.getBase64StringFromFile( newFile.getPath() );
                            mPresenter.setPortrait(mVCard,"avatar",msg);
                        }
                    }
                }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changefriend(MessageEvent event){
        if (event.getTag().equals( "changeVcard" ))
            mPresenter.getUserInfo();
    }
}
