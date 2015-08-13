package com.chaoyang805.musicplayer.utils;

import android.util.Log;

import com.chaoyang805.musicplayer.bean.LrcFileInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 歌词解析类，解析到的歌词信息会保存在LrcFileInfo对象中
 * Created by chaoyang805 on 2015/8/7.
 */
public class LrcParser {

    /**
     * 用来保存歌词信息的TreeMap
     */
    private TreeMap<Long, String> mTreeMap;
    /**
     * 正则表达式
     */
    private String mRegex = "\\[(\\d{2}:\\d{2}\\.?\\d{0,2})\\]";

    private LrcFileInfo mLrcFileInfo = null;
    private Pattern mPattern;

    /**
     * 构造方法
     * 初始化全局变量
     */
    public LrcParser() {
        mPattern = Pattern.compile(mRegex);
        mLrcFileInfo = new LrcFileInfo();
        mTreeMap = new TreeMap<>();
    }

    /**
     * 解析lrc文件的方法，解析完后会将解析的内容放入到lrcFileInfo对象中去
     */
    public LrcFileInfo parserLrc(String filePath) {
        try {
            String charset = FileUtils.getFileCharset(filePath);
            Log.d("LrcParser", "charset:" + charset);
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, charset);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                Log.d("LrcParser", line);
                //提取歌词文件中的标题 专辑 和歌手信息
                if (line.startsWith("[ti:")) {
                    Log.d("LrcParser", "Title:" + line);
                    mLrcFileInfo.setTitle(subLrcInfo(line));
                } else if (line.startsWith("[ar:")) {
                    mLrcFileInfo.setArtist(subLrcInfo(line));
                } else if (line.startsWith("[al:")) {
                    mLrcFileInfo.setAlbum(subLrcInfo(line));
                    //用正则表达式提取歌词和时间信息
                } else {
                    Matcher lrcMatcher = mPattern.matcher(line);
                    String[] content = mPattern.split(line);  //将时间和歌词文字内容分开
                    String lyricsContent;               //保存歌词的文字信息
                    if (content.length >= 1) {
                        lyricsContent = content[content.length - 1];
                    } else {
                        lyricsContent = " ";     //如果length = 0 说明是只有时间没歌词的空行
                    }
                    while (lrcMatcher.find()) {        //同一句歌词可能对应多个出现时间
                        String timeStr = lrcMatcher.group();
                        long lyricsStartTime = formatTime(timeStr); //歌词开始出现的时间
                        mTreeMap.put(lyricsStartTime, lyricsContent);
                    }
                }
            }
            //将获取好的TreeMap添加到lecFileInfo中去
            mLrcFileInfo.setLrcTreeMap(mTreeMap);
            //依次关闭IO流
            br.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mLrcFileInfo;
    }

    /**
     * 提取歌词文件中的标题 歌手 专辑信息
     *
     * @param line
     * @return
     */
    private String subLrcInfo(String line) {
        return line.substring(4, line.length() - 1);
    }

    /**
     * 解析时间字符串，
     *
     * @param timeStr 包含歌词时间信息的字符串
     * @return 返回对应歌词开始出现的时间
     * [01:21.23]
     */
    private long formatTime(String timeStr) {
        String minStr = timeStr.substring(1, 3);
        String secStr = timeStr.substring(4, 6);
        String millisec = "0";
        if (timeStr.length() >= 10) {
            millisec = timeStr.substring(7, 9);
        }
        int minutes = Integer.parseInt(minStr);
        int second = Integer.parseInt(secStr);
        int millis = Integer.parseInt(millisec);
        return minutes * 60 * 1000 + second * 1000 + millis * 10;
    }

}
