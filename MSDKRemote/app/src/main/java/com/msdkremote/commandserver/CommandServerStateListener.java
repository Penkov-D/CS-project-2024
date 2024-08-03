package com.msdkremote.commandserver;

import java.net.InetAddress;

/**
 * Listener on the server state.
 */
public interface CommandServerStateListener
{
    /**
     * Called when the server is initiated and started running.
     */
    public void onServerRunning();

    /**
     * Called when the server stops from legitimate reason.
     */
    public void onServerClosed();

    /**
     * Called when the server stops due to an error.
     *
     * @param e exception thrown by the server.
     */
    public void onServerException(Exception e);

    /**
     * Called when new client connected.
     *
     * @param address the address of the client
     */
    public void onClientConnected(InetAddress address);

    /**
     * Called when the client disconnected.
     */
    public void onClientDisconnected();
}
