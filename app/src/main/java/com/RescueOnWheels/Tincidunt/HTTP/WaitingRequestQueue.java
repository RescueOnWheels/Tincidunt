package com.RescueOnWheels.Tincidunt.HTTP;

import android.content.Context;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class WaitingRequestQueue {
    private final Socket mSocket;

    private boolean ready = true;

    private boolean stopped = false;

    public WaitingRequestQueue(Context context, String baseUrl) throws URISyntaxException {
        mSocket = IO.socket(baseUrl);
        mSocket.connect();

        mSocket.emit("headset");
    }

    public void addRequest(final float pitch, final float yaw) {
        addRequest(pitch, yaw, false);
    }

    private float prevPitch;
    private float prevYaw;

    private void addRequest(final float pitch, final float yaw, boolean priority) {
        if (!priority && (!ready || stopped)) {
            return;
        }
        if (pitch == prevPitch && yaw == prevYaw) {
            return;
        }

        mSocket.emit("headset position", String.format("{\"horizontal\":%s, \"vertical\":%s}", (double) yaw / 100, (double) pitch / 100));

        prevPitch = pitch;
        prevYaw = yaw;
    }

    public void stopAndRecenter() {
        addRequest(0, 0, true);
        stopped = true;
    }

    public void start() {
        stopped = false;
        ready = true;
    }
}
