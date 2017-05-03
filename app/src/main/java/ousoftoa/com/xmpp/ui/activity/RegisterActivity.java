package ousoftoa.com.xmpp.ui.activity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.presenter.RegisterPresenter;
import ousoftoa.com.xmpp.ui.view.RegisterView;

public class RegisterActivity extends BaseActivity<RegisterPresenter> implements RegisterView {

    @Bind(R.id.im_back)
    ImageView mImBack;
    @Bind(R.id.etName)
    EditText mEtName;
    @Bind(R.id.etPwd)
    EditText mEtPwd;
    @Bind(R.id.btRegister)
    Button mBtRegister;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_register );
    }

    @Override
    protected void initPresenter() {
        mPresenter = new RegisterPresenter( this, this );
    }

    @Override
    protected void init() {
        initToolbar();
        initListener();
    }

    private void initListener() {
        RxView.clicks( mBtRegister )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> {
                    if (mEtName.getText().toString().isEmpty()) {
                        showTip( "账号不能为空" );
                        return;
                    }
                    if (mEtPwd.getText().toString().isEmpty()) {
                        showTip( "密码不能为空" );
                        return;
                    }
                    mPresenter.toRegister( mEtName.getText().toString(), mEtPwd.getText().toString() );
                } );
    }

    private void initToolbar() {
        RxView.clicks( mImBack )
                .throttleFirst( 1, TimeUnit.SECONDS )
                .subscribe( aVoid -> finish() );
    }

    @Override
    public void onNext() {
        finish();
    }
}
