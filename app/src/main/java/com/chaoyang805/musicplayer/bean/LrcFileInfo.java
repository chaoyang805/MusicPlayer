package com.chaoyang805.musicplayer.bean;

import java.util.TreeMap;

/**
 * Created by chaoyang805 on 2015/8/10.
 * 歌词文件的实体类
 */
public class LrcFileInfo {

    private String mTitle;
    private String mArtist;
    private String mAlbum;

    private TreeMap<Long, String> mTreeMap;

    public LrcFileInfo() {
        mTreeMap = new TreeMap<>();
    }

    public LrcFileInfo(String title,String artist){
        this();
        this.mTitle = title;
        this.mArtist = artist;
    }

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

    public void setLrcTreeMap(TreeMap<Long, String> lrcTreeMap) {
        this.mTreeMap = lrcTreeMap;
    }

    public TreeMap<Long, String> getLrcTreeMap() {
        return mTreeMap;
    }
}
