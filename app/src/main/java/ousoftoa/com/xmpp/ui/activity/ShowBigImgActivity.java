package ousoftoa.com.xmpp.ui.activity;

import com.bm.library.PhotoView;

import butterknife.Bind;
import ousoftoa.com.xmpp.R;
import ousoftoa.com.xmpp.base.BaseActivity;
import ousoftoa.com.xmpp.utils.ImageUtil;
import ousoftoa.com.xmpp.utils.RxUtils;
import ousoftoa.com.xmpp.wight.statusbar.StatusBarUtil;
import rx.Observable;

public class ShowBigImgActivity extends BaseActivity {
    @Bind(R.id.pv)
    PhotoView mPv;

    @Override
    protected void initView() {
        setContentView( R.layout.activity_show_big_img );
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void init() {
        StatusBarUtil.setTransparent(this);
        mPv.enable();
        Observable.just( ImageUtil.getBitmapFromBase64String( getIntent().getStringExtra( "showbigimg" ) ) )
                .compose( RxUtils.applySchedulers( this ) )
                .subscribe( bitmap -> {
                    if (bitmap != null)
                        mPv.setImageBitmap( bitmap );
                    else
                        mPv.setImageResource( R.mipmap.default_tp );
                } );
    }
}
