package com.msdkremote.livevideo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

import dji.v5.manager.datacenter.camera.StreamInfo;
import dji.v5.manager.interfaces.ICameraStreamManager;

class CameraListener
    implements ICameraStreamManager.ReceiveStreamListener
{
    private final String TAG = this.getClass().getSimpleName();

    private FrameBuffer frameBuffer = null;

    public CameraListener(@NonNull FrameBuffer buffer) {
        this.frameBuffer = buffer;
    }

    @Override
    public synchronized void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info)
    {
        frameBuffer.addFrame(new Frame(data, offset, length, info));
    }
}
