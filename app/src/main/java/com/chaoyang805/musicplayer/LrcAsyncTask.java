package com.chaoyang805.musicplayer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.chaoyang805.musicplayer.bean.LrcFileInfo;
import com.chaoyang805.musicplayer.manager.MediaManager;

/**
 * Created by chaoyang805 on 2015/8/11.
 * 播放歌词的异步任务
 */
public class LrcAsyncTask extends AsyncTask<LrcFileInfo, Void, Void> {

    private TextView mTextView;
    //判断是不是暂停后恢复播放时实例化的该类对象
    private LrcFileInfo mLrcFileInfo;

    public LrcAsyncTask(TextView textView) {
        mTextView = textView;
    }

    @Override
    protected Void doInBackground(LrcFileInfo... params) {
        mLrcFileInfo = params[0];
        while (MusicPlayerActivity.mIsPlaying) {
            publishProgress();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        int currentPosition = MediaManager.getCurrentPosition() / 1000;
        Log.d("LrcAsyncTask", "已经播放" + (currentPosition) + "秒");
        if (mLrcFileInfo.contains(currentPosition)) {
            Log.d("LrcAsyncTask", "contains:" + currentPosition);
            mTextView.setText(mLrcFileInfo.getLrcByTime(currentPosition));

        }
    }
}
