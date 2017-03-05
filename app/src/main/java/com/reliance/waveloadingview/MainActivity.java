package com.reliance.waveloadingview;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;

public class MainActivity extends AppCompatActivity {
    WaveLoading mWaveView,mWaveView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWaveView = (WaveLoading) findViewById(R.id.wl_view);
        mWaveView2 = (WaveLoading) findViewById(R.id.wl_view2);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setDuration(15000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaveView.setPercentage((Float) animation.getAnimatedValue());
                mWaveView2.setPercentage((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }
}
