package com.reliance.waveloadingview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunzhishuai on 17/3/2.
 * E-mail itzhishuaisun@sina.com
 */

public class WaveLoading extends View implements ValueAnimator.AnimatorUpdateListener {

    private Paint mWavePaint;
    private List<Point> mInitPoints = new ArrayList<>();
    private int baselineX = 0;

    private int waveBase = baselineX;
    private ValueAnimator valueAnimator;
    private Paint mTextPaint;
    private float percentage = 0;

    private Paint mBgpaint;
    private WaveViewAttrs mAttrs = null;

    public WaveLoading(Context context) {
        this(context, null);
    }

    public WaveLoading(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        mAttrs = new WaveViewAttrs(null, 40, 200, Color.argb(100, 0, 0, 0), 80, Color.RED, Shaper.normalShaper);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveLoading, defStyleAttr, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int index = typedArray.getIndex(i);
            switch (index) {
                case R.styleable.WaveLoading_wave_width:
                    int wavewidthDp = typedArray.getInt(R.styleable.WaveLoading_wave_width, 200);
                    mAttrs.waveWidth = Utils.convertDpToPixel(context, wavewidthDp);
                    break;
                case R.styleable.WaveLoading_wave_height:
                    int waveHeightDp = typedArray.getInt(R.styleable.WaveLoading_wave_height, 40);
                    mAttrs.waveHeight = Utils.convertDpToPixel(context, waveHeightDp);
                    break;
                case R.styleable.WaveLoading_wave_color:
                    int defaultwavecoler = Color.argb(100, 0, 0, 0);
                    mAttrs.waveColor = typedArray.getColor(R.styleable.WaveLoading_wave_color, defaultwavecoler);
                    break;
                case R.styleable.WaveLoading_wave_bg:
                    BitmapDrawable drawable = (BitmapDrawable) typedArray.getDrawable(index);
                    mAttrs.bg = drawable.getBitmap();
                    break;
                case R.styleable.WaveLoading_percentage_text_size:
                    int textDpSize = typedArray.getInt(R.styleable.WaveLoading_percentage_text_size, 80);
                    mAttrs.percentageTextSize = Utils.convertDpToPixel(context, textDpSize);
                    break;
                case R.styleable.WaveLoading_percentage_text_color:
                    mAttrs.percentageTextColor = typedArray.getColor(R.styleable.WaveLoading_wave_color, Color.RED);
                    break;
                case R.styleable.WaveLoading_loading_view_shape:
                    mAttrs.loading_view_shape = typedArray.getInt(R.styleable.WaveLoading_loading_view_shape, 100);
                    break;

            }
        }
    }

    private void init() {
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setColor(mAttrs.waveColor);
        valueAnimator = ValueAnimator.ofInt(0, mAttrs.waveWidth + 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(this);
        mTextPaint = new Paint();
        mTextPaint.setColor(mAttrs.percentageTextColor);
        mTextPaint.setTextSize(mAttrs.percentageTextSize);
        mBgpaint = new Paint();


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mAttrs.bg.getWidth();
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = mAttrs.bg.getHeight();
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBg(canvas);
        if (mInitPoints.size() == 0) {
            initPoints();
        }
        drawWave(canvas);
        drawPercentage(canvas);

    }

    private void drawBg(Canvas canvas) {
        if (mAttrs.bg == null)
            return;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mAttrs.bg, getMeasuredWidth(), getMeasuredHeight(), false);

        switch (mAttrs.loading_view_shape) {
            case Shaper.circleShaper:
                Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas1 = new Canvas(bitmap);
                canvas1.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2, mBgpaint);
                mBgpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas1.drawBitmap(scaledBitmap, 0, 0, mBgpaint);
                mBgpaint.setXfermode(null);
                canvas.drawBitmap(bitmap, 0, 0, mBgpaint);
                break;
            default:
                canvas.drawBitmap(scaledBitmap, 0, 0, mBgpaint);
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        valueAnimator.cancel();
        super.onDetachedFromWindow();
    }

    private void drawPercentage(Canvas canvas) {
        int v = (int) (percentage * 100);
        String drawPercentage = String.valueOf(v).concat("%");
        Rect rect = new Rect();
        mTextPaint.getTextBounds(drawPercentage, 0, drawPercentage.length(), rect);
        int centreY = getMeasuredHeight() / 2;
        int centreX = getMeasuredWidth() / 2;
        int height = rect.height();
        int width = rect.width();
        canvas.drawText(drawPercentage, centreX - width / 2, centreY + height / 2, mTextPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        valueAnimator.start();

    }

    private void drawWave(Canvas canvas) {

        Path path = new Path();
        for (int i = 0; i < mInitPoints.size(); i = i + 2) {
            Point point = mInitPoints.get(i);
            if (i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                Point prePoint = mInitPoints.get(i - 1);
                path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            }
        }
        int waveCounts = getWaveCounts(getMeasuredWidth());
        int boundWidth = (waveCounts - 1) * mAttrs.waveWidth;
        path.lineTo(boundWidth, getMeasuredHeight());
        path.lineTo(-mAttrs.waveWidth, getMeasuredHeight());
        path.lineTo(-mAttrs.waveWidth, baselineX);
        path.close();
        if(mAttrs.loading_view_shape== Shaper.circleShaper){
            Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas1 = new Canvas(bitmap);
            canvas1.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,Math.min(getMeasuredWidth(),getMeasuredHeight())/2,mWavePaint);
            mWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas1.drawPath(path,mWavePaint);
            mWavePaint.setXfermode(null);
            mWavePaint.setColor(mAttrs.waveColor);
            canvas.drawBitmap(bitmap,0,0,mWavePaint);
        }
        if(mAttrs.loading_view_shape== Shaper.normalShaper){
            canvas.drawPath(path,mWavePaint);
        }

    }

    private void initPoints() {
        mInitPoints.clear();
        int waveCounts = getWaveCounts(getMeasuredWidth());
        for (int j = 0; j < waveCounts * 4 + 1; j++) {
            switch (j % 4) {
                case 0:
                case 2:
                    waveBase = baselineX;
                    break;
                case 1:
                    waveBase = baselineX - mAttrs.waveHeight;
                    break;
                case 3:
                    waveBase = baselineX + mAttrs.waveHeight;
                    break;
            }
            Point point = new Point(-mAttrs.waveWidth + (mAttrs.waveWidth / 4) * j, waveBase);
            mInitPoints.add(point);
        }
    }

    public int getWaveCounts(int measureWidth) {
        return Math.round(measureWidth / mAttrs.waveWidth + 0.5f) + 1;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        Integer animatedValue = (Integer) animation.getAnimatedValue();
        moveWavePoint(animatedValue);
        invalidate();
    }

    private int prelength = 0;

    private void moveWavePoint(int wavelength) {
        int dx = wavelength - prelength;
        prelength = wavelength;
        float totalY = 0;
        for (int i = 0; i < mInitPoints.size(); i++) {
            if (wavelength != mAttrs.waveWidth) {
                mInitPoints.get(i).x = mInitPoints.get(i).x + dx;
            } else {
                mInitPoints.get(i).x = mInitPoints.get(i).x - wavelength;
                prelength = 0;
            }
            totalY = totalY + mInitPoints.get(i).y;
        }
        changePointsY(totalY / mInitPoints.size());
    }

    private int changebaseLine = 0;

    private void changePointsY(float pointBaseY) {
        for (int j = 0; j < mInitPoints.size(); j++) {
            mInitPoints.get(j).y = (int) (mInitPoints.get(j).y - pointBaseY + changebaseLine);
        }
    }

    public void setPercentage(float percentage) {
        if (percentage > 1 || percentage < 0)
            throw new IllegalArgumentException("percentage must less than one  and  more than zero");
        changebaseLine = (int) (getMeasuredHeight() * (1 - percentage));

        this.percentage = percentage;
    }

    private class Point {
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;
    }

    private class WaveViewAttrs {
        public WaveViewAttrs(Bitmap bg, int waveHeight, int waveWidth, int waveColor, int percentageTextSize, int percentageTextColor, int loading_view_shape) {
            this.bg = bg;
            this.waveHeight = waveHeight;
            this.waveWidth = waveWidth;
            this.waveColor = waveColor;
            this.percentageTextSize = percentageTextSize;
            this.percentageTextColor = percentageTextColor;
            this.loading_view_shape = loading_view_shape;
        }

        Bitmap bg;
        int waveHeight;
        int waveWidth;
        int waveColor;
        int percentageTextSize;
        int percentageTextColor;
        int loading_view_shape;
    }

    private interface Shaper {
        int normalShaper = 200;
        int circleShaper = 100;
    }
}
