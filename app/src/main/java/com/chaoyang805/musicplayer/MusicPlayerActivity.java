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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chaoyang805.musicplayer.bean.LrcFileInfo;
import com.chaoyang805.musicplayer.manager.MediaManager;
import com.chaoyang805.musicplayer.utils.FileUtils;
import com.chaoyang805.musicplayer.utils.LrcParser;
import com.chaoyang805.musicplayer.view.LrcView;

/**
 * 播放音乐和歌词同步显示的主Activity
 */
public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    /**
     * 选取音乐的requestCode
     */
    private static final int REQUEST_OPEN_MUSIC = 0x01;
    /**
     * 选取歌词的requestCode
     */
    private static final int REQUEST_OPEN_LRC = 0x02;
    /**
     * 控制播放暂停的Button
     */
    private ImageButton mIbToggle;
    /**
     * 选择音乐文件的Button
     */
    private Button mBtnOpenMusic;
    /**
     * 选择歌词文件的Button
     */
    private Button mBtnOpenLrc;
    /**
     *
     */
    private TextView mTvTitle, mTvArtist;
    private LrcView mLrcView;
    /**
     * 歌词解析类的对象,用来解析歌词文件.
     */
    private LrcParser mParser;
    /**
     * 解析到的歌词信息存储在LrcFileInfo中.
     */
    private LrcFileInfo mLrcFileInfo;
    /**
     * 选取音乐和歌词后返回的Uri
     */
    private Uri mMusicUri, mLrcUri;
    /**
     * 异步任务对象，用来同步播放歌词
     */
    private LrcAsyncTask mTask;
    /**
     * 记录音乐播放状态的标志位,声明成公共的同时可供外部类访问
     */
    public static boolean mIsPlaying;
    /**
     * MediaManager 准备是否完成的标志位
     */
    private boolean mMediaMgrWellPrepared = false;
    /**
     * 返回键按下时的标志位，用来判断在onPause时是否要显示notification
     */
    private boolean mIsBackPressed = false;
    /**
     * 用来在后台播放时显示通知
     */
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        initViews();
        mParser = new LrcParser();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 初始化View控件
     * 并设置事件侦听器
     */
    private void initViews() {
        mLrcView = (LrcView) findViewById(R.id.lrc_view);
        mIbToggle = (ImageButton) findViewById(R.id.ib_toggle);
        mTvTitle = (TextView) findViewById(R.id.tv_media_info_title);
        mTvArtist = (TextView) findViewById(R.id.tv_media_info_artist);
        mBtnOpenMusic = (Button) findViewById(R.id.btn_open_music);
        mBtnOpenLrc = (Button) findViewById(R.id.btn_open_lrc);
        mBtnOpenMusic.setOnClickListener(this);
        mBtnOpenLrc.setOnClickListener(this);
        mIbToggle.setOnClickListener(this);
    }

    /**
     * 通过文件管理器选择一个音乐文件
     */
    private void openMusicFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_OPEN_MUSIC);
    }

    /**
     * 通过文件管理器选择一个歌词文件
     */
    private void openLrc() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_lrc_file)), REQUEST_OPEN_LRC);
    }

    /**
     * 选择完文件后的回调,在这里进行歌词解析和MediaManager的准备
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
     * @param data
     */
    private void loadLrc(Intent data) {
        //从Intent中获取文件的Uri
        mLrcUri = data.getData();
        //通过FileUtils类的方法获得文件路径
        String path = FileUtils.getPath(this, mLrcUri);
        //判断是否为lrc文件
        if (!isLrcFile(path)) {
            Toast.makeText(this, R.string.please_choose_a_lrc_file, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("MusicPlayerActivity", mLrcUri.toString() + "\n" + path);
        //解析歌词并返回LrcFileInfo对象
        mLrcFileInfo = mParser.parserLrc(path);
        //为歌曲设置标题和歌手
        if (!TextUtils.isEmpty(mLrcFileInfo.getTitle())) {
            mTvTitle.setText(mLrcFileInfo.getTitle());
        }
        if (!TextUtils.isEmpty(mLrcFileInfo.getArtist())) {
            mTvArtist.setText(mLrcFileInfo.getArtist());
        }
    }

    /**
     * 从ActivityResult中读取MP3文件
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
     * @return
     */
    private boolean isMediaFile() {
        String path = FileUtils.getPath(this, mMusicUri);
        return path.endsWith(".mp3");

    }

    /**
     * 根据文件路径判断文件是否为lrc格式
     * @param path
     * @return
     */
    private boolean isLrcFile(String path) {
        return path.endsWith(".lrc");
    }

    /**
     * onClick方法，处理按钮点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_toggle) {

        }
        switch (v.getId()) {
            case R.id.ib_toggle:
                //播放或者暂停音乐
                playOrPauseMusic();
                Log.d("MusicPlayerActivity", "playOrPauseMusic");
                break;
            case R.id.btn_open_music:
                //如果音乐正在播放，先停止播放再进行音乐选择
                if (mIsPlaying) {
                    mIsPlaying = false;
                    mMediaMgrWellPrepared = false;
                    MediaManager.pause();
                    mIbToggle.setImageResource(android.R.drawable.ic_media_play);
                }
                openMusicFile();
                break;
            case R.id.btn_open_lrc:
                //如果音乐正在播放，先停止播放再进行歌词选择
                if (mIsPlaying) {
                    mIsPlaying = false;
                    mMediaMgrWellPrepared = false;
                    MediaManager.pause();
                    mIbToggle.setImageResource(android.R.drawable.ic_media_play);
                }
                openLrc();
                break;
        }
    }

    /**
     * 切换音乐的播放状态
     */
    private void playOrPauseMusic() {
        //MediaManager没准备好说明还没选择音乐
        if (!mMediaMgrWellPrepared) {
            Toast.makeText(this, R.string.please_choose_music_first, Toast.LENGTH_SHORT).show();
            return;
        }
        //播放音乐
        if (!mIsPlaying) {

            MediaManager.play();
            mIsPlaying = true;
            mIbToggle.setImageResource(android.R.drawable.ic_media_pause);
            if (mLrcFileInfo != null) {
                mTask = new LrcAsyncTask(mLrcView,mLrcFileInfo);
                mTask.execute();
            }
            //暂停音乐
        } else if (mIsPlaying) {
            MediaManager.pause();
            mIsPlaying = false;
            mIbToggle.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    /**
     * 音乐播放完毕的回调,恢复按钮的状态，重置标志位
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsPlaying = false;
        mLrcView.setText(R.string.thanks_for_listening);
        mIbToggle.setImageResource(android.R.drawable.ic_media_play);
        MediaManager.release();
        mMediaMgrWellPrepared = false;
    }

    /**
     * 记录返回键第一次点下时的时间
     */
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
        //三秒内再次点击则退出程序并停止播放音乐
        if (secondPress - firstPress < 3000) {
            mIsBackPressed = true;
            finish();
        } else {
            Toast.makeText(this, R.string.exit_will_stop_music, Toast.LENGTH_SHORT).show();
            firstPress = secondPress;
        }
    }

    /**
     * Activity处于后台时如果音乐正在播放则在通知栏进行通知。
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mIsPlaying && !mIsBackPressed) {
            showNotification();
        }
    }

    /**
     * onResume时如果正在播放音乐，则清楚通知栏的通知
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mIsPlaying) {
            mNotificationManager.cancelAll();
        }
    }

    /**
     * 在通知栏显示通知提示音乐正在播放的方法
     */
    private void showNotification() {
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.music_is_nowplaying))
                        .setContentText(getString(R.string.music_is_nowplaying));
        mNotificationManager.notify(0x11, mBuilder.build());

    }

    /**
     * Activity销毁时清楚通知并释放MediaManager，重置标志位等
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancelAll();
        MediaManager.release();
        mIsPlaying = false;
        mMediaMgrWellPrepared = false;
    }
}
