package com.msdkremote.commandserver;

import androidx.annotation.NonNull;

/**
 * Interface to dictate to be called on new command.
 * The difference between the other command handler,
 * is that this is made to use internally by the server,
 * and will call from here the other method with a pointer
 * to the server, to send responds on the same time.
 */
interface CommandServerReaderHandler
{
    /**
     * Handler to manage a new command.
     *
     * @param command string representation of a command.
     */
    public void onCommand(@NonNull String command);
}
