package com.msdkremote.commandserver;

import java.net.InetAddress;

public interface CommandServerStateListener
{
    public void onServerRunning();

    public void onServerClosed();

    public void onServerException(Exception e);


    public void onClientConnected(InetAddress address);

    public void onClientDisconnected();
}
