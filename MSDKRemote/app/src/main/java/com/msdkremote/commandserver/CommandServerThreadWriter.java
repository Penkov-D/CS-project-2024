package com.msdkremote.commandserver;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

class CommandServerThreadWriter implements Runnable
{
    private final String TAG;

    private final OutputStream outputStream;

    private final MessageQueue messageQueue;


    public CommandServerThreadWriter(
            @NonNull OutputStream outputStream,
            @NonNull MessageQueue messageQueue)
    {
        this.outputStream = outputStream;
        this.messageQueue = messageQueue;
        this.TAG = null;
    }


    public CommandServerThreadWriter(
            @NonNull OutputStream outputStream,
            @NonNull MessageQueue messageQueue,
            @NonNull String TAG)
    {
        this.outputStream = outputStream;
        this.messageQueue = messageQueue;
        this.TAG = TAG;
    }


    @Override
    public void run()
    {
        try {
            Writer writer = new OutputStreamWriter(this.outputStream);

            while (!Thread.currentThread().isInterrupted())
            {
                String message = messageQueue.getMessage(0);

                if (this.TAG != null) {
                    Log.v(this.TAG, "Sending message : " + message);
                }

                writer.write(message);
                writer.flush();
            }
        }
        catch (IOException e) {
            if (this.TAG != null) {
                Log.v(this.TAG, "IOException occurred. (connection closed ?)", e);
            }
        }
        catch (InterruptedException ignored) { }

        if (this.TAG != null) {
            Log.v(this.TAG, "Closing the writer thread.");
        }
    }
}
