package com.RescueOnWheels.Tincidunt.MJPEG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.RescueOnWheels.Tincidunt.CardboardOverlayView;

import java.io.IOException;

public class MjpegPlayer implements SurfaceHolder.Callback {
    private MjpegViewThread thread;

    private MjpegInputStream mIn = null;

    private boolean mRun = false;
    private boolean surface1Done;
    private boolean surface2Done;

    public MjpegPlayer(CardboardOverlayView cov) {
        init(cov.getSurfaceViews());
        cov.setCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("TAG", "a surface was created!");
        if (surface1Done) {
            surface2Done = true;
        } else {
            surface1Done = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // No need to implement this function.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // No need to implement this function.
    }

    private void init(SurfaceView... holders) {
        thread = new MjpegViewThread(holders);
        Paint overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
    }

    private void startPlayback() {
        if (mIn != null) {
            mRun = true;
            thread.start();
        }
    }

    public void setSource(MjpegInputStream source) {
        mIn = source;
        startPlayback();
    }

    private class MjpegViewThread extends Thread {
        private final SurfaceView[] surfaces;

        MjpegViewThread(SurfaceView... surfaces) {
            this.surfaces = surfaces;
        }

        @Override
        public void run() {
            final Paint p = new Paint();

            while (mRun) {
                if (!surface1Done || !surface2Done) {
                    continue;
                }

                try {
                    final Bitmap frame = mIn.readMjpegFrame();
                    Bitmap scaled = Bitmap.createScaledBitmap(frame, surfaces[0].getWidth(), surfaces[0].getHeight(), false);

                    for (final SurfaceView surfaceView : surfaces) {
                        SurfaceHolder surface = surfaceView.getHolder();
                        synchronized (this) {
                            Canvas canvas = surface.lockCanvas();
                            canvas.drawColor(Color.BLACK);
                            canvas.drawBitmap(scaled, 0, 0, p);
                            surface.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
