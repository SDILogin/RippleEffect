package com.andexert.rippleeffect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.andexert.library.WaterWaveView;

public class MainActivity extends AppCompatActivity {

    private View mMainImage;
    private WaterWaveView mWaterWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainImage = findViewById(R.id.main_image);

        mWaterWaveView = (WaterWaveView) findViewById(R.id.water_wave_view);
        mWaterWaveView.setTargetView(mMainImage);

        mMainImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mWaterWaveView.createWaveAtPosition(event);
                        break;

                    default:
                        break;
                }

                return false;
            }
        });
    }
}
