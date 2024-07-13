package com.msdkremote.commandserver;

import androidx.annotation.NonNull;

/**
 * Interface to dictate how to handle new commands.
 */
public interface CommandServerHandler
{
    /**
     * This method is called whenever new command is
     * received by the command server it is hooked to.
     * <p>
     * Note on implementation, this method is called
     * on the same thread that handle the communication,
     * so any time consuming operations should be
     * handled carefully.
     *
     * @param commandServer the server from which this call was made
     * @param command the command that was received.
     */
    public void onCommand(@NonNull CommandServerThread commandServer, @NonNull String command);
}
