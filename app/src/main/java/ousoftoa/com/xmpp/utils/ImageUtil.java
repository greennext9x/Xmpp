package ousoftoa.com.xmpp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressLint("NewApi")
public class ImageUtil {

    public static Bitmap getBitmapFromBase64String(String imageString) {
        // 对字节数组字符串进行Base64解码并生成图燿
        if (imageString == null) // 图像数据为空
            return null;
        byte[] data = Base64.decode( imageString, Base64.DEFAULT );
        return BitmapFactory.decodeByteArray( data, 0, data.length );
    }

    public static String getBase64StringFromFile(String imageFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream( imageFile );
            data = new byte[in.available()];
            in.read( data );
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        return Base64.encodeToString( data, Base64.DEFAULT );// 返回Base64编码过的字节数组字符
    }

    /**
     * drawable -> Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565 );
        Canvas canvas = new Canvas( bitmap );
        //canvas.setBitmap(bitmap);  
        drawable.setBounds( 0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight() );
        drawable.draw( canvas );
        return bitmap;
    }

    /**
     * resource - > Bitmap
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap resourceToBitmap(Context context, int resId) {
        Resources res = context.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource( res, resId );
        return bitmap;
    }

    /**
     * Bitmap   - > Bytes
     *
     * @param bm
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress( Bitmap.CompressFormat.PNG, 100, baos );
        return baos.toByteArray();
    }

    /**
     * Bytes  - > Bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray( b, 0, b.length );
        } else {
            return null;
        }
    }
}