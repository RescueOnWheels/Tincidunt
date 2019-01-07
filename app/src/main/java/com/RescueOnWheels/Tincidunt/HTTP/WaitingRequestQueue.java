package com.RescueOnWheels.Tincidunt.HTTP;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class WaitingRequestQueue {
    private final Socket mSocket;
    private float prevPitch;
    private float prevYaw;

    public WaitingRequestQueue(String baseUrl) throws URISyntaxException {
        mSocket = IO.socket(baseUrl);
        mSocket.connect();

        mSocket.emit("headset");
    }

    public void addRequest(final float pitch, final float yaw) {
        if (pitch == prevPitch && yaw == prevYaw) {
            return;
        }

        mSocket.emit("headset position", String.format("{\"horizontal\":%s, \"vertical\":%s}", (double) yaw / -100, (double) pitch / 100));

        prevPitch = pitch;
        prevYaw = yaw;
    }
}
