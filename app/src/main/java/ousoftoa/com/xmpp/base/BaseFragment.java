package ousoftoa.com.xmpp.base;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.trello.rxlifecycle.components.support.RxFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import ousoftoa.com.xmpp.R;

public abstract class BaseFragment<P extends BasePresenter> extends RxFragment implements IBaseView {
    protected P mPresenter;
    protected ProgressDialog dialog ;
    protected View mRootView;
    protected Context mContext;
    private static final String TAG = "BaseFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = initView(inflater,container);
        ButterKnife.bind(this, mRootView);
        if (isEventBus()){
            EventBus.getDefault().register( this );
        }
        initPresenter();
        init();
        return mRootView;
    }

    protected abstract View initView(LayoutInflater inflater, ViewGroup container);

    protected abstract void initPresenter();

    protected abstract void init();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mContext = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dialog != null){
            dialog = null;
        }
        ButterKnife.unbind(this);
        if (isEventBus()){
            EventBus.getDefault().unregister( this );
        }
        this.mPresenter = null;
        this.mRootView = null;
    }

    public boolean isEventBus(){
        return false;
    }

    @Override
    public void showTip(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
            dialog  = new ProgressDialog(mContext);
            dialog.setMessage( getString( R.string.loading ));
            dialog.show();
        }
        if (!getActivity().isFinishing() && !dialog.isShowing()) {
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
    public void onError(Throwable throwable) {
        showTip( "加载错误,请稍后重试" );
    }
}
