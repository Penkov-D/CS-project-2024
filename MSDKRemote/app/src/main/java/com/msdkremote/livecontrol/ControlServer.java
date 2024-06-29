package com.msdkremote.livecontrol;

import java.net.ServerSocket;
import java.net.Socket;

public class ControlServer
{
    private final String TAG = this.getClass().getSimpleName();

    private Thread socketThread = null;

    private final Object threadStateLock = new Object();

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;

    public ControlServer() { }

    public synchronized void startServer(int port, ControlServer controlServer)
    {}
}
