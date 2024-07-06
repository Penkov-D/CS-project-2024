package com.msdkremote.commandserver;

import androidx.annotation.NonNull;

public interface CommandServerHandler
{
    public void onCommand(@NonNull String command);
}
