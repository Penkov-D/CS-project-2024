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

    public CameraListener()
    {

    }

    private OutputStream outputStream = null;

    public synchronized void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    @Override
    public synchronized void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info)
    {
        if (outputStream != null) {
            try {
                outputStream.write(data, offset, length);
            }
            catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }
}
