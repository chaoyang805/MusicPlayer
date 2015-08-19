package com.chaoyang805.musicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class LrcView extends TextView {

    private static final String TAG = "LrcView";
    //显示歌词的行数,为保证对称，取奇数.
    private int mColumns = 9;
    //保存歌词信息的map
    private TreeMap<Integer, String> mMap;
    //歌词字体大小
    private float mTextSize = 20;
    //正在播放的歌词的颜色
    private int mShowingLrcColor = Color.GREEN;
    //歌词间的行间距
    private int mColumnMargin = 20;
    //画笔
    private Paint mPaint;
    //歌词播放的当前时间(以秒记)
    private int mCurrentPosition = 0;
    //正在显示的歌词
    private String mShowingLrc;
    //歌词文字的高度
    private int mTextHeight;
    private int mUnShowLrcColor = Color.BLACK;

    private boolean isLrcMapSet = false;

    public void setLrcMap(TreeMap<Integer, String> map) {
        this.mMap = map;
        if (mMap != null) {
            isLrcMapSet = true;
        }
    }

    //保存控件的宽高
    private int mWidth, mHeight;
    //保存控件的中心点坐标
    private int mCenterX, mCenterY;


    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public LrcView(Context context, AttributeSet attr, int defStyleRes) {
        super(context, attr, defStyleRes);
        mTextSize = getTextSize();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        //获取中心点坐标
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        //根据控件的高度计算出显示几行歌词
        String text = "歌词";
        Rect textBounds = new Rect();
        mPaint.getTextBounds(text, 0, text.length() - 1, textBounds);
        mTextHeight = textBounds.bottom - textBounds.top;
        mColumns = mHeight / (mColumnMargin + mTextHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isLrcMapSet) {
            drawLrcs(canvas);
        }
    }

    private void drawLrcs(Canvas canvas) {
        //draw正在显示的歌词
        mPaint.setColor(mShowingLrcColor);
        int length = (int) mPaint.measureText(mShowingLrc);
        int x = (mWidth - length) / 2;
        int y = mCenterY + mTextHeight / 2;
        canvas.drawText(mShowingLrc, x, y, mPaint);
        //draw已经唱过的歌词
        mPaint.setColor(mUnShowLrcColor);
        int currentPos = mCurrentPosition - 1;
        for (int i = 0; i < mColumns / 2; i++) {
            while (currentPos > 0) {
                currentPos--;
                if (mMap.containsKey(currentPos)) {
                    String text = mMap.get(currentPos);
                    x = (int) ((mWidth - mPaint.measureText(text)) / 2);
                    y = mCenterY - mColumnMargin - mTextHeight / 2 - i * (mColumnMargin + mTextHeight);
                    canvas.drawText(text, x, y, mPaint);
                    break;
                }
            }
        }
        //draw还未唱的歌词
        currentPos = mCurrentPosition;
        for (int i = 0; i < mColumns / 2; i++) {
            while (currentPos < mMap.lastKey() + 1) {
                currentPos++;
                if (mMap.containsKey(currentPos)) {
                    String text = mMap.get(currentPos);
                    x = (int) ((mWidth - mPaint.measureText(text)) / 2);
                    y = mCenterY + mTextHeight / 2 + (i + 1) * (mColumnMargin + mTextHeight);
                    canvas.drawText(text, x, y, mPaint);
                    break;
                }
            }
        }
    }


    /**
     * 更新歌曲播放的进度
     */
    public void updateLrc(int currentPosition) {
        if (mMap.containsKey(currentPosition)) {
            mCurrentPosition = currentPosition;
            mShowingLrc = mMap.get(mCurrentPosition);
        }
        postInvalidate();
    }

}
