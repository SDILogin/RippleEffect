/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Robin Chutaux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.andexert.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class WaterWaveView extends FrameLayout {
    private final static int DEFAULT_INNER_CIRCLE_RADIUS = 7;
    private final static int DEFAULT_MIDDLE_CIRCLE_WIDTH = 15;
    private final static int DEFAULT_OUTER_CIRCLE_WIDTH = 10;
    private final static int WAVE_ALPHA = 75;
    private final static int WAVE_COLOR = 0x016cc2;
    private final static int FRAME_RATE = 30;
    private final static int RIPPLE_DURATION = 2000;

    private int mInnerCircleRadius = DEFAULT_INNER_CIRCLE_RADIUS;
    private int mMiddleCircleStrokeWidth = DEFAULT_MIDDLE_CIRCLE_WIDTH;
    private int mOuterCircleStrokeWidth = DEFAULT_OUTER_CIRCLE_WIDTH;

    private float mRadiusMax = 0;

    private Paint mInnerCirclePaint;
    private Paint mMiddleCirclePaint;
    private Paint mOuterCirclePaint;

    private List<Wave> mListOfWaves = new ArrayList<>();

    private TouchListener mTouchDelegate = new TouchListener();

    public WaterWaveView(Context context) {
        this(context, null);
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setAntiAlias(true);
        mInnerCirclePaint.setStrokeWidth(mInnerCircleRadius);
        mInnerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mInnerCirclePaint.setColor(WAVE_COLOR);
        mInnerCirclePaint.setAlpha(WAVE_ALPHA / 2);

        mMiddleCirclePaint = new Paint(mInnerCirclePaint);
        mMiddleCirclePaint.setStrokeWidth(mMiddleCircleStrokeWidth);
        mMiddleCirclePaint.setStyle(Paint.Style.STROKE);
        mMiddleCirclePaint.setAlpha(WAVE_ALPHA);

        mOuterCirclePaint = new Paint(mInnerCirclePaint);
        mOuterCirclePaint.setStrokeWidth(mOuterCircleStrokeWidth);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setAlpha(WAVE_ALPHA / 2);

        this.setWillNotDraw(false);
        this.setDrawingCacheEnabled(true);
        this.setClickable(true);

        mTouchDelegate.setTargetView(null);
        setOnTouchListener(mTouchDelegate);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (!mListOfWaves.isEmpty()) {
            drawWaves(canvas);
            clearList();

            postInvalidateDelayed(FRAME_RATE);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRadiusMax = Math.max(w / 3, h / 3);
    }

    /**
     * Delegate touch events to specific view
     *
     * @param v view, that obtain touch events
     */
    public void setTargetView(View v) {
        mTouchDelegate.setTargetView(v);
    }

    /**
     * Launch wave animation for the current view with a MotionEvent
     *
     * @param event MotionEvent
     */
    public void createWaveAtPosition(MotionEvent event) {
        createWave(event.getX(), event.getY());
    }

    /**
     * Launch wave animation for the current view centered at mX and mY position
     *
     * @param x position of the wave center
     * @param y position of the wave center
     */
    public void createWaveAtPosition(final float x, final float y) {
        createWave(x, y);
    }

    /**
     * Create wave with center at (x, y)
     *
     * @param x position of the wave center
     * @param y position of the wave center
     */
    private void createWave(final float x, final float y) {
        if (mListOfWaves.isEmpty()) {
            mListOfWaves.add(new Wave((int) x, (int) y));
        } else {
            Wave last = mListOfWaves.get(mListOfWaves.size() - 1);
            Wave created = new Wave((int) x, (int) y);
            if (this.dist(last,created) > mInnerCircleRadius) {
                mListOfWaves.add(created);
                if (mListOfWaves.size() > 15) {
                    // too much waves. Remove some from center
                    mListOfWaves.remove(2);
                    mListOfWaves.remove(3);
                    mListOfWaves.remove(4);
                    mListOfWaves.remove(5);
                    mListOfWaves.remove(6);
                    mListOfWaves.remove(7);
                }
            }
        }

        // draw waves
        invalidate();
    }

    /**
     * Distance from centers of two waves
     *
     * @param w1 first wave
     * @param w2 second wave
     * @return distance from w1.center to w2.center
     */
    private double dist(final Wave w1, final Wave w2) {
        double dx2 = (w1.getX() - w2.getX()) * (w1.getX() - w2.getX()); // (x1 - x2)^2
        double dy2 = (w1.getY() - w2.getY()) * (w1.getY() - w2.getY()); // (y1 - y2)^2
        return Math.sqrt(dx2 + dy2);
    }

    /**
     * Draw all waves from list of waves
     *
     * @param canvas canvas that user in onDraw method
     */
    private void drawWaves(Canvas canvas) {
        // it is not mistake. We need only positive values
        for (Wave wave : mListOfWaves) {
            wave.draw(canvas);
        }
    }

    /**
     * Remove too "old" waves from list of waves
     */
    private void clearList() {
        while (!mListOfWaves.isEmpty()) {
            Wave wave = mListOfWaves.get(0);
            if (wave.getRemoveTime() < System.currentTimeMillis()) {
                mListOfWaves.remove(wave);
            } else {
                break;
            }
        }
    }

    private class Wave {
        private final int x;
        private final int y;
        private final long removeTime;

        private float completedPart;
        private float scale;
        private long ttl;

        public Wave(int x, int y) {
            this.x = x;
            this.y = y;
            this.removeTime = System.currentTimeMillis() + RIPPLE_DURATION;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public long getRemoveTime() {
            return removeTime;
        }

        public void draw(Canvas canvas) {
            this.ttl = removeTime - System.currentTimeMillis();
            if (this.ttl > 0) {
                this.completedPart = (float) (RIPPLE_DURATION - this.ttl) / RIPPLE_DURATION;
                this.scale = (float) Math.sin(Math.PI * this.completedPart);

                mOuterCirclePaint.setStrokeWidth(mOuterCircleStrokeWidth * this.scale);
                mMiddleCirclePaint.setStrokeWidth(mMiddleCircleStrokeWidth * this.scale);
                mInnerCirclePaint.setStrokeWidth(mInnerCircleRadius * this.scale);

                this.drawCircle(canvas, 1.0f, mOuterCirclePaint);
                this.drawCircle(canvas, 1.5f, mMiddleCirclePaint);
                this.drawCircle(canvas, 2.0f, mMiddleCirclePaint);

                this.completedPart = 1 - this.completedPart;
                this.drawCircle(canvas, 8.0f, mInnerCirclePaint);
            }
        }

        private void drawCircle(Canvas canvas, float divider, Paint paint) {
            canvas.drawCircle(
                    this.x, this.y,                            // x, y
                    mRadiusMax * this.completedPart / divider, // r
                    paint                                      // paint
            );
        }
    }

    public static class TouchListener implements OnTouchListener {
        private View mTargetView;

        public void setTargetView(View v) {
            mTargetView = v;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mTargetView != null) {
                mTargetView.dispatchTouchEvent(event);
            }

            return false;
        }
    }
}
