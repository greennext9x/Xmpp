package ousoftoa.com.xmpp.utils;

import com.trello.rxlifecycle.LifecycleTransformer;

import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.base.BaseFragment;
import ousoftoa.com.xmpp.base.IBaseView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by jess on 11/10/2016 16:39
 * Contact with jess.yan.effort@gmail.com
 */

public class RxUtils {

    public static <T> Observable.Transformer<T, T> applySchedulers(final IBaseView view) {
        return observable -> observable.subscribeOn( Schedulers.io())
                .doOnSubscribe( () -> {//显示进度条
                    view.showLoadingDialog();
                } )
                .subscribeOn( AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate( () -> {
                    view.closeLoadingDialog();//隐藏进度条
                } ).compose(RxUtils.<T>bindToLifecycle(view));
    }

    public static <T> LifecycleTransformer<T> bindToLifecycle(IBaseView view) {
        if (view instanceof BaseActivity) {
            return ((BaseActivity) view).<T>bindToLifecycle();
        } else if (view instanceof BaseFragment) {
            return ((BaseFragment) view).<T>bindToLifecycle();
        } else {
            throw new IllegalArgumentException("view isn't activity or fragment");
        }
    }

    public static <T> Observable.Transformer<T, T>  bindToSchedulers(IBaseView view) {
        return observable -> observable.subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( bindToLifecycle( view ) );
    }

}
