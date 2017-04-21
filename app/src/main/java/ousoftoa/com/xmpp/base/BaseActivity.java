package ousoftoa.com.xmpp.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.aitangba.swipeback.SwipeBackHelper;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.wight.statusbar.StatusBarUtil;

/**
 * Created by Administrator on 2017/1/4/0004.
 */

/**
 * Created by 36483 on 2016/12/19.
 */

public abstract class BaseActivity<P extends BasePresenter> extends RxAppCompatActivity implements IBaseView,SwipeBackHelper.SlideBackManager{
    protected P mPresenter;

    protected ProgressDialog dialog ;

    protected Context mContext;
    private static final String TAG = "SwipeBackActivity";
    private SwipeBackHelper mSwipeBackHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        initView();
        ButterKnife.bind(this);
        if (isEventBus()){
            EventBus.getDefault().register( this );
        }
        initPresenter();
        StatusBarUtil.setColor(this,getResources().getColor( R.color.themebackgroup ),10);
        init();
    }

    protected abstract void initView();

    protected abstract void initPresenter();

    protected abstract void init();

    public boolean isEventBus(){
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mSwipeBackHelper == null) {
            mSwipeBackHelper = new SwipeBackHelper(this);
        }
        return mSwipeBackHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public Activity getSlideActivity() {
        return this;
    }

    @Override
    public boolean supportSlideBack() {
        return true;
    }

    @Override
    public boolean canBeSlideBack() {
        return true;
    }

    @Override
    public void finish() {
        if(mSwipeBackHelper != null) {
            mSwipeBackHelper.finishSwipeImmediately();
            mSwipeBackHelper = null;
        }
        super.finish();
    }

    @Override
    public void showTip(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadFailure(String errorMsg) {

    }

    @Override
    public void loadSuccess(Object object) {

    }

    @Override
    public void showLoadingDialog() {
        if (dialog  == null) {
            dialog  = new ProgressDialog(this);
            dialog.setMessage( getString( R.string.loading ));
            dialog.show();
        }
        if (!isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void closeLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if (isEventBus()){
            EventBus.getDefault().unregister( this );
        }
    }

    @Override
    public void onError(Throwable throwable) {
        showTip( "加载错误,请稍后重试" );
    }
}
