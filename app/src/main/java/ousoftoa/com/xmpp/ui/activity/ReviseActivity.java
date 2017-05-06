package ousoftoa.com.xmpp.ui.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.model.bean.MessageEvent;
import ousoftoa.com.xmpp.model.bean.UserInfoData;
import ousoftoa.com.xmpp.scoket.XmppConnection;
import ousoftoa.com.xmpp.utils.RxUtils;
import rx.Observable;

public class ReviseActivity extends BaseActivity {

    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @Bind(R.id.etName)
    EditText mEtName;
    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.tvNote)
    TextView mTvnote;

    private UserInfoData mInfodata;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_revise );
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void init() {
        mInfodata = (UserInfoData) getIntent().getSerializableExtra( "userinfo" );
        initData();
        initListener();
    }

    private void initListener() {
        RxTextView.textChangeEvents( mEtName )
                .subscribe( event -> {
                    if (!TextUtils.isEmpty( event.text().toString().trim() )) {
                        mBtnToolbarSend.setEnabled( true );
                        mBtnToolbarSend.setTextColor( getResources().getColor( R.color.white ) );
                    } else {
                        mBtnToolbarSend.setEnabled( false );
                        mBtnToolbarSend.setTextColor( getResources().getColor( R.color.enabledText ) );
                    }
                } );
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
        RxView.clicks( mBtnToolbarSend )
                .filter( aVoid -> !TextUtils.isEmpty( mEtName.getText().toString().trim() ) )
                .subscribe( aVoid -> reviseUsername( mEtName.getText().toString().trim() ) );
    }

    private void initData() {
        mTvTitle.setText( mInfodata.getTitle() );
        if (!mInfodata.getType().equals( "nickName" )){
            mTvnote.setVisibility( View.GONE );
        }
    }

    private void reviseUsername(String msg) {
        Observable.create( (Observable.OnSubscribe<VCard>) subscriber -> {
            VCard mvcard = XmppConnection.getInstance().getUserInfo( null );
            mvcard.setField( mInfodata.getType(), msg );
            subscriber.onNext( mvcard );
            subscriber.onCompleted();
        } ).flatMap( vCard -> XmppConnection.getInstance().changeVcard( vCard ) )
                .compose( RxUtils.applySchedulers( this ) )
                .subscribe( aBoolean -> {
                    EventBus.getDefault().post( new MessageEvent( "changeVcard", "" ) );
                    finish();
                }, throwable -> onError( throwable ) );
    }
}
