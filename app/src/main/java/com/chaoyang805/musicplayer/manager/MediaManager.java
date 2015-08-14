package com.chaoyang805.musicplayer.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * Created by chaoyang805 on 2015/8/1.
 * 使用MediaManager来管理MediaPlayer对象.
 */
public class MediaManager {

    private static MediaPlayer mMediaPlayer;

    /**
     * 准备MediaPlayer的静态方法
     * @param context
     * @param uri 音乐文件的uri
     * @param onCompletionListener 音乐播放完毕的回调
     */
    public static void prepare(Context context,Uri uri,
                                 MediaPlayer.OnCompletionListener onCompletionListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        try {
            mMediaPlayer.setDataSource(context,uri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("MediaManager", "prepared");
    }

    /**
     * 播放音乐
     */
    public static void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    /**
     * 暂停音乐
     */
    public static void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 恢复播放
     */
    public static void resume() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            Log.d("MediaManager", "MediaManager.start()");
            mMediaPlayer.start();
        }
    }

    /**
     * 释放MediaPlayer
     */
    public static void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 获取当前音乐播放的进度
     * @return
     */
    public static int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }


}
