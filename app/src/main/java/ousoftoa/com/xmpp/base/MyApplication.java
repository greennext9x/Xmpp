package ousoftoa.com.xmpp.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;

import com.aitangba.swipeback.ActivityLifecycleHelper;
import com.bumptech.glide.Glide;
import com.lqr.emoji.LQREmotionKit;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.loader.ImageLoader;
import com.lqr.imagepicker.view.CropImageView;

/**
 * Created by 韩莫熙 on 2017/4/1.
 */

public class MyApplication extends Application{
    private static MyApplication mInstance = null;
    private static Context mContext;
    private static Handler mHandler;//主线程Handler


    public static MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = this.getApplicationContext();
        mHandler = new Handler();
        // 必须在 Application 的 onCreate 方法中执行 registerActivityLifecycleCallbacks 来初始化滑动返回
        registerActivityLifecycleCallbacks( ActivityLifecycleHelper.build());
        LQREmotionKit.init( this );
        //初始化仿微信控件ImagePicker
        initImagePicker();
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(getContext()).load( Uri.parse("file://" + path).toString()).centerCrop().into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle( CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }


    public static Handler getMainHandler() {
        return mHandler;
    }


    public Context getContext(){
        return this.mContext;
    }
}
