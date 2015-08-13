package com.chaoyang805.musicplayer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.util.TreeMap;

/**
 * Created by chaoyang805 on 2015/8/11.
 * 播放歌词的异步任务
 */
public class LrcAsyncTask extends AsyncTask<TreeMap<Long, String>, String, Void> {

    private TextView mTextView;
    private TreeMap<Long, String> mLrcMap;
    private long mMusicPosition = 0;
    //判断是不是暂停后恢复播放时实例化的该类对象
    private boolean mIsResume;

    public LrcAsyncTask(TextView textView, long musicPosition) {
        mTextView = textView;
        Log.d("LrcAsyncTask", "MusicPosition:" + musicPosition);
        mMusicPosition = musicPosition;
        if (mMusicPosition > 0)
            mIsResume = true;
    }

    @Override
    protected Void doInBackground(TreeMap... params) {
        mLrcMap = params[0];
        playLrc();
        return null;
    }

    /**
     * 遍历TreeMap并通过publishProgress方法将歌词信息设置在TextView上
     */
    private void playLrc() {
        long lastStartTime = 0;
        for (Long currentStartTime : mLrcMap.keySet()) {
            if (!MusicPlayerActivity.mIsPlaying) {
                break;
            }
            //从暂停中恢复时循环跳过前几个已经播放过的歌词
            Log.d("LrcAsyncTask", "currentStartTime - mMusicPosition>>>>>" + (currentStartTime - mMusicPosition));
            if (currentStartTime < mMusicPosition) {
                Log.d("LrcAsyncTask",currentStartTime + "<<>>"+lastStartTime +"<<<>>>"+ mMusicPosition);
                lastStartTime = currentStartTime;
                continue;
            }
            try {
                //从暂停中恢复时先显示暂停时显示的那句歌词
                if (mIsResume) {
                    Log.d("LrcAsyncTask", "lastTime:" + ">>>>" + lastStartTime + mLrcMap.get(lastStartTime));
                    publishProgress(mLrcMap.get(lastStartTime));
                    lastStartTime = mMusicPosition;
                    mIsResume = false;
                }
                Thread.sleep(currentStartTime - lastStartTime);
                if (MusicPlayerActivity.mIsPlaying) {
                    publishProgress(mLrcMap.get(currentStartTime));
                }
                Log.d("LrcAsyncTask", currentStartTime + mLrcMap.get(currentStartTime));
                lastStartTime = currentStartTime;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        mTextView.setText(values[0]);
    }
}
