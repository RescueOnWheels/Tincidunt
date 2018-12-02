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

import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A Cardboard application that streams video from a RPi and sends pitch & yaw to the RPi.
 */
public class StreamActivity extends CardboardActivity implements CardboardView.StereoRenderer, View.OnTouchListener {

    private static final String TAG = "StreamActivity";

    private float[] mEulerAngles = new float[3];
    private float[] mInitEulerAngles = new float[3];

    private CardboardOverlayView mOverlayView;

    private MjpegPlayer mp;

    private int i = 0;


    private String baseUrl = "http://";

    private WaitingRequestQueue mQueue;

    private boolean tracking = false;

    public StreamActivity() {
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     * //@param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);
        cardboardView.setOnTouchListener(this);

        Intent i = getIntent();
        baseUrl += Objects.requireNonNull(i.getExtras()).get("ip");
        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast();
        startPlayer();

        mQueue = new WaitingRequestQueue(this, baseUrl + ":8080/move");
        mQueue.addRequest(0f, 0f);
    }

    private void startPlayer() {
        String URL = baseUrl + ":8000/stream/video.mjpeg";
        mp = new MjpegPlayer(mOverlayView);
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

        if (i % 10 == 0) {
            int x = (int)Math.round(mEulerAngles[0] / (Math.PI / 2) * 100);
                x = Math.min(x, 100);
                x = Math.max(x, -100);
            int y = (int)Math.round(-mEulerAngles[1] / (Math.PI / 2) * 100);
                y = Math.min(y, 100);
                y = Math.max(y, -100);

            Log.i(TAG, "Axis: " + x + " " + y);
        }
        i++;
        if (tracking) {
            shift();
            mQueue.addRequest(mEulerAngles[0], mEulerAngles[1]);
        }
    }

    private void shift() {
        for (int i = 0; i < mEulerAngles.length; i++) {
            mEulerAngles[i] -= mInitEulerAngles[i];
        }
    }

    /**
     * Draws a frame for an eye. The transformation for that eye (from the camera) is passed in as
     * a parameter.
     *
     * @param transform The transformations to apply to render this eye.
     */
    @Override
    public void onDrawEye(EyeTransform transform) {
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!tracking) {
                Log.i(TAG, "starting tracking");
                mInitEulerAngles = mEulerAngles.clone();
                tracking = true;
                mOverlayView.fade3DToast();
                mQueue.start();
            } else {
                Log.i(TAG, "stopping tracking");
                tracking = false;
                mOverlayView.show3DToast();
                mQueue.stopAndRecenter();
            }
        }
        return true;
    }

    class DoRead extends AsyncTask<String, Void, MjpegInputStream> {

        @Override
        protected MjpegInputStream doInBackground(String... params) {
            return MjpegInputStream.read(params[0]);
        }

        protected void onPostExecute(MjpegInputStream result) {
            if (result == null) {
                throw new RuntimeException("stream is null!!!");
            }
            mp.setSource(result);
            Log.i(TAG, "running mjpeg input stream");
        }
    }
}