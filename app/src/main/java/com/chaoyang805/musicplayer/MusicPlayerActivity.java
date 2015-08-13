package com.chaoyang805.musicplayer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chaoyang805.musicplayer.bean.LrcFileInfo;
import com.chaoyang805.musicplayer.manager.MediaManager;
import com.chaoyang805.musicplayer.utils.FileUtils;
import com.chaoyang805.musicplayer.utils.LrcParser;

import java.util.TreeMap;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private static final int REQUEST_OPEN_MUSIC = 0x01;
    private static final int REQUEST_OPEN_LRC = 0x02;
    private ImageButton mIbToggle;
    private TextView mTvTitle, mTvArtist, mLrcView;
    private LrcParser mParser;
    private LrcFileInfo mLrcFileInfo;
    private Uri mMusicUri, mLrcUri;
    private LrcAsyncTask mTask;
    private long mCurPosition = 0;
    private boolean mMediaMgrWellPrepared = false;
    public static boolean mIsPlaying;
    NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        initViews();
        mParser = new LrcParser();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initViews() {
        mLrcView = (TextView) findViewById(R.id.lrc_view);
        mIbToggle = (ImageButton) findViewById(R.id.ib_toggle);
        mTvTitle = (TextView) findViewById(R.id.tv_media_info_title);
        mTvArtist = (TextView) findViewById(R.id.tv_media_info_artist);
        mIbToggle.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_open_music:
                if (mIsPlaying) {
                    mIsPlaying = false;
                    mMediaMgrWellPrepared = false;
                    MediaManager.pause();
                    mIbToggle.setImageResource(android.R.drawable.ic_media_play);
                }
                openMusicFile();
                break;
            case R.id.action_open_lrc:
                if (mIsPlaying) {
                    mIsPlaying = false;
                    mMediaMgrWellPrepared = false;
                    MediaManager.pause();
                    mIbToggle.setImageResource(android.R.drawable.ic_media_play);
                }
                openLrc();
                break;
        }
        return true;
    }

    private void openMusicFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_OPEN_MUSIC);
    }

    private void openLrc() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_lrc_file)), REQUEST_OPEN_LRC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OPEN_MUSIC) {
                loadMedia(data);

            } else if (requestCode == REQUEST_OPEN_LRC) {
                loadLrc(data);
            }
        }
    }

    /**
     * 从ActivityResult中读取歌词文件
     *
     * @param data
     */
    private void loadLrc(Intent data) {
        mLrcUri = data.getData();
        String path = FileUtils.getPath(this, mLrcUri);
        if (!isLrcFile(path)) {
            Toast.makeText(this, R.string.please_choose_a_lrc_file, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("MusicPlayerActivity", mLrcUri.toString() + "\n" + path);
        mLrcFileInfo = mParser.parserLrc(path);
        if (!TextUtils.isEmpty(mLrcFileInfo.getTitle())) {
            mTvTitle.setText(mLrcFileInfo.getTitle());
        }
        if (!TextUtils.isEmpty(mLrcFileInfo.getArtist())) {
            mTvArtist.setText(mLrcFileInfo.getArtist());
        }
    }

    /**
     * 从ActivityResult中读取MP3文件
     *
     * @param data
     */
    private void loadMedia(Intent data) {
        mMusicUri = data.getData();
        if (!isMediaFile()) {
            Toast.makeText(this, R.string.please_choose_a_mp3_file, Toast.LENGTH_SHORT).show();
            return;
        }
        MediaManager.prepare(this, mMusicUri, this);
        mMediaMgrWellPrepared = true;
        Log.e("MusicPlayerActivity", mMusicUri.toString());
    }

    /**
     * 根据文件Uri判断文件是否为mp3格式
     *
     * @return
     */
    private boolean isMediaFile() {
        return mMusicUri.toString().endsWith(".mp3");

    }

    /**
     * 根据文件路径判断文件是否为lrc格式
     *
     * @param path
     * @return
     */
    private boolean isLrcFile(String path) {
        return path.endsWith(".lrc");
    }

    /**
     * 按钮点击的listener
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_toggle) {
            playOrPauseMusic();
            Log.d("MusicPlayerActivity", "playOrPauseMusic");
        }
    }

    /**
     * 切换音乐的播放状态
     */
    private void playOrPauseMusic() {
        if (!mMediaMgrWellPrepared) {
            Toast.makeText(this, R.string.please_choose_music_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!MediaManager.isPlaying()) {

            MediaManager.play();
            mIsPlaying = true;
            mIbToggle.setImageResource(android.R.drawable.ic_media_pause);
            if (mLrcFileInfo != null) {
                TreeMap<Long, String> lrcTreeMap = mLrcFileInfo.getLrcTreeMap();
                mTask = new LrcAsyncTask(mLrcView, mCurPosition);
                mTask.execute(lrcTreeMap);
            }
        } else if (MediaManager.isPlaying()) {
            MediaManager.pause();
            mIsPlaying = false;
            mIbToggle.setImageResource(android.R.drawable.ic_media_play);
            mCurPosition = MediaManager.getCurrentPosition();
            Log.d("MusicPlayer", "CurrentPosition:" + mCurPosition);
        }
    }

    /**
     * 音乐播放完毕的回调
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mLrcView.setText(R.string.thanks_for_listening);
        mIbToggle.setImageResource(android.R.drawable.ic_media_play);
        MediaManager.release();
        mIsPlaying = false;
        mMediaMgrWellPrepared = false;
    }

    long firstPress = -1;

    /**
     * 按下返回键时提示用户退出将停止播放音乐
     */
    @Override
    public void onBackPressed() {
        if (firstPress < 0) {
            Toast.makeText(this, R.string.exit_will_stop_music, Toast.LENGTH_SHORT).show();
            firstPress = System.currentTimeMillis();
            return;
        }
        long secondPress = System.currentTimeMillis();
        if (secondPress - firstPress < 3000) {
            finish();
        } else {
            Toast.makeText(this, R.string.exit_will_stop_music, Toast.LENGTH_SHORT).show();
            firstPress = secondPress;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsPlaying) {
            showNotification();
        }
    }


    private void showNotification() {
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.music_is_nowplaying))
                        .setContentText(getString(R.string.music_is_nowplaying));
        mNotificationManager.notify(0x11, mBuilder.build());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancelAll();
        MediaManager.release();
        mIsPlaying = false;
        mMediaMgrWellPrepared = false;
    }
}
