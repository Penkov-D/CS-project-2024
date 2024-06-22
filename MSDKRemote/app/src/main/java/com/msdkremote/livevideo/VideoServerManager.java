package com.msdkremote.livevideo;

import android.util.Log;

import java.io.IOException;

public class VideoServerManager
{
    public final String TAG = this.getClass().getSimpleName();

    private static VideoServerManager instance = null;

    private VideoServer videoServer = null;
    private final AvailableCameraListener availableCameraListener = new AvailableCameraListener();

    FrameBuffer frameBuffer = null;


    public static synchronized VideoServerManager getInstance()
    {
        if (instance == null)
            instance = new VideoServerManager();

        return instance;
    }

    private VideoServerManager() { }

    public synchronized void startServer(int port) {
        if (videoServer != null)
            return;

        frameBuffer = new FrameBuffer(1_000_000);

        videoServer = new VideoServer();
        videoServer.startServer(port, frameBuffer);

        availableCameraListener.startListener(frameBuffer);
    }

    public synchronized void killServer() throws InterruptedException {
        if (videoServer == null)
            return;

        videoServer.stopServer();
        videoServer = null;
        availableCameraListener.stopListener();
    }
}
