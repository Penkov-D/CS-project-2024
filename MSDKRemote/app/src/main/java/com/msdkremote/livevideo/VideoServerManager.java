package com.msdkremote.livevideo;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class VideoServerManager
{
    private static VideoServerManager instance = null;

    private VideoServer videoServer = null;


    public static synchronized VideoServerManager getInstance()
    {
        if (instance == null)
            instance = new VideoServerManager();

        return instance;
    }

    private VideoServerManager() { }

    public synchronized void startServer(int port)
    {
        if (videoServer != null)
            return;

        videoServer = new VideoServer();
        videoServer.startServer(port);
    }

    public synchronized void killServer() throws InterruptedException {
        if (videoServer == null)
            return;

        videoServer.stopServer();
        videoServer = null;
    }
}
