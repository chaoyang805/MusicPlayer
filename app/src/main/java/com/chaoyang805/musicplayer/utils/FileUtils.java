package com.chaoyang805.musicplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

/**
 * Created by chaoyang805 on 2015/8/10.
 * 文件操作的工具类
 */
public class FileUtils {
    /**
     * 获取文件的路径信息
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 通过第三方库获取文件的编码方式，防止乱码
     * @param filePath
     * @return
     */
    public static String getFileCharset(String filePath) {
        File file = new File(filePath);

        Charset charset = null;
        try {
            // 获取原始文件编码
            CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
            detector.add(JChardetFacade.getInstance());
            charset = detector.detectCodepage(file.toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return charset.name();
    }
}
