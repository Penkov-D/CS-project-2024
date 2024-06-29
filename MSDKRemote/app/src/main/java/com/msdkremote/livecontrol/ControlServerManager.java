package com.msdkremote.livecontrol;

import com.msdkremote.livevideo.VideoServerManager;

public class ControlServerManager
{
    public final String TAG = this.getClass().getSimpleName();

    private static ControlServerManager instance = null;



    public static synchronized ControlServerManager getInstance()
    {
        if (instance == null)
            instance = new ControlServerManager();

        return instance;
    }

    private ControlServerManager() { }

    public synchronized void startServer(int port)
    {

    }

    public synchronized void killServer() throws InterruptedException
    {

    }
}
