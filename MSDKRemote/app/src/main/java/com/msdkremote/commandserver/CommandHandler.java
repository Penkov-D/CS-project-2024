package com.msdkremote.commandserver;

import androidx.annotation.NonNull;

import java.net.InetAddress;

public interface CommandHandler
{
    public void onClientConnected(InetAddress address);

    public String onCommand(@NonNull String command);

    public void onClientDisconnected();
}
