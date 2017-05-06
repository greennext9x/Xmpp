package ousoftoa.com.xmpp.wight.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.base.BaseDialog;
import com.jakewharton.rxbinding.view.RxView;

import ousoftoa.com.xmpp.R;

/**
 * Created by 韩莫熙 on 2017/5/5.
 */

public class CheckDialog extends BaseDialog<CheckDialog> {
    private CheckBox mCbBoy;
    private LinearLayout mLlBoy;
    private CheckBox mCbGirl;
    private LinearLayout mLlGirl;

    private String sex;
    private CheckListener mCheckListener;

    public CheckDialog(Context context) {
        super( context );
    }

    public CheckDialog setData(String sex) {
        this.sex = sex;
        return this;
    }

    public CheckDialog setCheckListener(CheckListener checkListener) {
        this.mCheckListener = checkListener;
        return this;
    }

    @Override
    public View onCreateView() {
        widthScale( 0.85f );
        View inflate = View.inflate( mContext, R.layout.dialog_custom_base, null );
        mCbBoy = (CheckBox) inflate.findViewById( R.id.cb_boy );
        mCbGirl = (CheckBox) inflate.findViewById( R.id.cb_girl );
        mLlBoy = (LinearLayout) inflate.findViewById( R.id.ll_boy );
        mLlGirl = (LinearLayout) inflate.findViewById( R.id.ll_girl );
        if (sex != null && sex.equals( "男" ))
            mCbBoy.setChecked( true );
        else if (sex != null && sex.equals( "女" ))
            mCbGirl.setChecked( true );
        else {
            mCbBoy.setChecked( false );
            mCbGirl.setChecked( false );
        }
        inflate.setBackgroundDrawable(
                CornerUtils.cornerDrawable( Color.parseColor( "#ffffff" ), dp2px( 5 ) ) );
        return inflate;
    }

    @Override
    public void setUiBeforShow() {
        RxView.clicks( mLlBoy )
                .subscribe( aVoid -> {
                    mCbBoy.setChecked( true );
                    mCbGirl.setChecked( false );
                    dismiss();
                    mCheckListener.setcheck( "男" );
                } );
        RxView.clicks( mLlGirl )
                .subscribe( aVoid -> {
                    mCbBoy.setChecked( false );
                    mCbGirl.setChecked( true );
                    dismiss();
                    mCheckListener.setcheck( "女" );
                } );
    }

    public interface CheckListener {
        void setcheck(String sex);
    }
}
