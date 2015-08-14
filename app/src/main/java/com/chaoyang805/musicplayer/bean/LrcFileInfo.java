package com.chaoyang805.musicplayer.bean;

import android.util.Log;

import java.util.TreeMap;

/**
 * Created by chaoyang805 on 2015/8/10.
 * 歌词文件的实体类，存储歌曲歌词的相关信息
 */
public class LrcFileInfo {
    /**
     * 歌曲的标题
     */
    private String mTitle;
    /**
     * 歌曲的歌手信息
     */
    private String mArtist;
    /**
     * 歌曲的专辑信息
     */
    private String mAlbum;
    /**
     * 每句歌词和时间对应的TreeMap
     */
    private TreeMap<Integer, String> mTreeMap;

    /**
     * 构造方法中实例化TreeMap
     */
    public LrcFileInfo() {
        mTreeMap = new TreeMap<>();
    }

    //成员变量的setter和getter方法

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public void setAlbum(String album) {
        this.mAlbum = album;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setLrcTreeMap(TreeMap<Integer, String> lrcTreeMap) {
        this.mTreeMap = lrcTreeMap;
    }

    public TreeMap<Integer, String> getLrcTreeMap() {
        return mTreeMap;
    }

    /**
     * 根据时间判断是否在该时间存在歌词需要显示
     * @param time
     * @return
     */
    public boolean contains(int time) {
        return mTreeMap.containsKey(time);
    }

    /**
     * 根据时间来获取相应的歌词
     * @param time
     * @return
     */
    public String getLrcByTime(int time) {
        String lrc = mTreeMap.get(time);
        Log.d("LrcFileInfo", lrc);
        return lrc;
    }
}
