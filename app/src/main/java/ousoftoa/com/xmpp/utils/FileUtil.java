package ousoftoa.com.xmpp.utils;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 韩莫熙 on 2017/5/3.
 */

@SuppressLint("NewApi")
public class FileUtil {

    /**
     * @param filePath  保存路径,包括名字
     * @return
     */
    public static boolean saveFileByBase64(String fileString, String filePath) {
        // 对字节数组字符串进行Base64解码并生成图燿
        if (fileString == null) // 图像数据为空
            return false;
        byte[] data = Base64.decode(fileString, Base64.DEFAULT);
        saveFileByBytes(data, filePath);
        return true;
    }

    /**
     * @param filePath  保存路径,包括名字
     * @return
     */
    public static boolean saveFileByBytes(byte[] data,String filePath) {
        // 对字节数组字符串进行Base64解码并生成图燿
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                File file2 = new File(filePath.substring(0, filePath.lastIndexOf("/")+1));
                file2.mkdirs();
            }
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }
}
