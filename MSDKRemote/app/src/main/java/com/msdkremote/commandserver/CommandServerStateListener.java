package com.msdkremote.commandserver;

import java.net.InetAddress;

public interface CommandServerStateListener
{

    public void onStartingServer();

    public void onServerRunning();

    public void onServerClosed();

    public void onServerException();


    public void onClientConnected(InetAddress address);

    public void onClientDisconnected();
}
