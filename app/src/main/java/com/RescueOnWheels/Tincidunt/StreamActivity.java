package com.RescueOnWheels.Tincidunt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.RescueOnWheels.Tincidunt.HTTP.WaitingRequestQueue;
import com.RescueOnWheels.Tincidunt.MJPEG.MjpegInputStream;
import com.RescueOnWheels.Tincidunt.MJPEG.MjpegPlayer;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.net.URISyntaxException;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;

import static com.RescueOnWheels.Tincidunt.R.*;
import static com.RescueOnWheels.Tincidunt.R.id.*;

/**
 * A Cardboard application that streams video from a RPi and sends pitch & yaw to the RPi.
 */
public class StreamActivity extends CardboardActivity implements CardboardView.StereoRenderer, View.OnTouchListener {

    private static final String TAG = "StreamActivity";
    private final float[] mEulerAngles = new float[3];
    private MjpegPlayer player;
    private CardboardOverlayView mOverlayView;

    private int i = 0;

    private int servoStep = 5;

    private String baseUrl = "http://";

    private WaitingRequestQueue mQueue;
    private float[] mOffsetEulerAngles = {0.0f, 0.0f, 0.0f};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.common_ui);
        CardboardView cardboardView = findViewById(cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);
        cardboardView.setOnTouchListener(this);

        shift();

        Intent intent = getIntent();
        baseUrl += Objects.requireNonNull(intent.getExtras()).get("ip");
        mOverlayView = findViewById(overlay);
        mOverlayView.show3DToast();
        startPlayer();

        try {
            mQueue = new WaitingRequestQueue(baseUrl + ":3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mQueue.addRequest(0f, 0f);
    }

    private void startPlayer() {
        String URL = baseUrl + ":8080/stream/video.mjpeg";
        player = new MjpegPlayer(mOverlayView);
        (new DoRead()).execute(URL);
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");

    }

    /**
     * Creates the buffers we use to store information about the 3D world. OpenGL doesn't use Java
     * arrays, but rather needs data in a format it can understand. Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getEulerAngles(mEulerAngles, 0);
        shift();
        int x = (int) Math.round(mEulerAngles[0] / (Math.PI / 2) * servoStep);
        x = Math.min(x, servoStep);
        x = Math.max(x, -servoStep);
        x *= 100/servoStep;
        int y = (int) Math.round(-mEulerAngles[1] / (Math.PI / 2) * servoStep);
        y = Math.min(y, servoStep);
        y = Math.max(y, -servoStep);
        y *= 100/servoStep;

        if (i % 10 == 0) {
            Log.i(TAG, "Axis: " + x + " " + y);
        }

        mQueue.addRequest(x, y);
        i++;
    }
    private void shift(){
        for(int i = 0;i <mEulerAngles.length; i++){
            mEulerAngles[i] -= mOffsetEulerAngles[i];
        }
    }

    @Override
    public void onDrawEye(EyeTransform transform) {
        // No need to implement this function.
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        // No need to implement this function.
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mOffsetEulerAngles = mEulerAngles.clone();
        return false;
    }


    private class DoRead extends AsyncTask<String, Void, MjpegInputStream> {

        @Override
        protected MjpegInputStream doInBackground(String... params) {
            return MjpegInputStream.read(params[0]);
        }

        protected void onPostExecute(MjpegInputStream result) throws NullPointerException {
            if (result == null) {
                throw new NullPointerException("No stream was found on the given address.");
            }

            player.setSource(result);
            Log.i(TAG, "running mjpeg input stream");
        }
    }
}