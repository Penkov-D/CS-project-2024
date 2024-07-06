package com.msdkremote.commandserver;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class CommandServerThreadReader implements Runnable
{
    private final String TAG;

    private final InputStream inputStream;

    private final CommandServerHandler commandHandler;


    public CommandServerThreadReader(
            @NonNull InputStream inputStream,
            @NonNull CommandServerHandler commandHandler)
    {
        this.inputStream = inputStream;
        this.commandHandler = commandHandler;
        this.TAG = null;
    }


    public CommandServerThreadReader(
            @NonNull InputStream inputStream,
            @NonNull CommandServerHandler commandHandler,
            @NonNull String TAG)
    {
        this.inputStream = inputStream;
        this.commandHandler = commandHandler;
        this.TAG = TAG;
    }


    @Override
    public void run()
    {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));

            String command = null;

            while ((command = reader.readLine()) != null)
            {
                Log.v(this.TAG, "Received command : " + command);
                this.commandHandler.onCommand(command);
            }
        }
        catch (IOException e) {
            if (this.TAG != null) {
                Log.v(this.TAG, "IOException occurred. (connection closed ?)", e);
            }
        }

        if (this.TAG != null) {
            Log.v(this.TAG, "Closing the reader thread.");
        }
    }
}
