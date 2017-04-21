package ousoftoa.com.xmpp.base;

import android.content.Context;

/**
 * Created by Administrator on 2017/1/4/0004.
 */

public abstract class BasePresenter {
    protected Context mContext;
    protected int mState=0;

    public BasePresenter(Context mContext) {
        this.mContext = mContext;
    }
}
