package com.msdkremote.livevideo;

import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class VideoServerManager
{
    public final String TAG = this.getClass().getSimpleName();

    private static VideoServerManager instance = null;

    private VideoServer videoServer = null;
    private final AvailableCameraListener availableCameraListener = new AvailableCameraListener();

    private PipedOutputStream pipedOStream = null;
    private PipedInputStream pipedIStream = null;


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

        try {
            pipedOStream = new PipedOutputStream();
            pipedIStream = new PipedInputStream(pipedOStream);

            videoServer = new VideoServer();
            videoServer.startServer(port, pipedIStream);

            availableCameraListener.startListener(pipedOStream);
        }
        catch (IOException e) {
            Log.e(TAG, "IOException occurred while starting the server", e);
        }
    }

    public synchronized void killServer() throws InterruptedException {
        if (videoServer == null)
            return;

        try {
            if (pipedIStream != null)
                pipedIStream.close();
        }
        catch (IOException e) {
            Log.e(TAG, "IOException occurred while closing input pipe", e);
        }

        try {
            if (pipedOStream != null)
                pipedOStream.close();
        }
        catch (IOException e) {
            Log.e(TAG, "IOException occurred while closing output pipe", e);
        }

        videoServer.stopServer();
        videoServer = null;

        availableCameraListener.stopListener();
    }
}
