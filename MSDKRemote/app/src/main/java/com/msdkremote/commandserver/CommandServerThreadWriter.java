package com.msdkremote.commandserver;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

class CommandServerThreadWriter
{
    // Tag for logcat
    private final String TAG;

    // The output stream to write the messages on
    private final OutputStream outputStream;
    private boolean isClosed = false;

    // The message queue - place to take the messages from
    private final MessageQueue messageQueue;

    // Thread holder
    private final Thread threadWriter;


    /**
     * CommandServerThreadWriter is a thread dedicated to the outputStream of a socket,
     * or more specifically to connection on the server.
     * The class is disposable, meaning that for new connection new class should be created.
     *
     * @param outputStream the OutputStream to write into.
     * @param messageQueue queue for the messages needed to deliver.
     */
    public CommandServerThreadWriter(
            @NonNull OutputStream outputStream,
            @NonNull MessageQueue messageQueue)
    {
        this.outputStream = outputStream;
        this.messageQueue = messageQueue;
        this.TAG = null;

        // Start the thread
        this.threadWriter = new Thread(this::run);
        this.threadWriter.start();
    }


    /**
     * CommandServerThreadWriter is a thread dedicated to the outputStream of a socket,
     * or more specifically to connection on the server.
     * The class is disposable, meaning that for new connection new class should be created.
     *
     * @param outputStream the OutputStream to write into.
     * @param messageQueue queue for the messages needed to deliver.
     * @param TAG customizable tag to use with logcat, mainly for debugging.
     */
    public CommandServerThreadWriter(
            @NonNull OutputStream outputStream,
            @NonNull MessageQueue messageQueue,
            @NonNull String TAG)
    {
        this.outputStream = outputStream;
        this.messageQueue = messageQueue;
        this.TAG = TAG;

        // Start the thread
        this.threadWriter = new Thread(this::run);
        this.threadWriter.start();
    }


    /**
     * Immediately stopping this thread from running.
     * This method will return only when the thread is fully terminated,
     * or interrupt was called on the caller thread.
     *
     * @throws InterruptedException if the calling thread was interrupted.
     */
    public synchronized void stopServer() throws InterruptedException
    {
        // Check if this thread is running
        if (!this.threadWriter.isAlive()) return;

        // Close the thread
        this.threadWriter.interrupt();
        // The clean method should interrupt the thread in the spacial case that it is mid writing.
        this.clean();

        // Return from this method only when the thread was fully terminated.
        this.threadWriter.join();
    }


    /**
     * The core of this class. Dictates the behavior of the thread.
     */
    private void run()
    {
        try {
            // Wrap by writer, to make string sending easier.
            Writer writer = new OutputStreamWriter(this.outputStream);

            // The server should run until it is interrupted.
            while (!Thread.currentThread().isInterrupted())
            {
                // Get message from the queue, to send.
                // Note this is blocking until there is new message,
                // or throw InterruptedException if this thread is interrupted.
                String message = messageQueue.getMessage(0);

                if (this.TAG != null) {
                    Log.v(this.TAG, "Sending message : " + message);
                }

                // Writing the message
                writer.write(message);
                writer.flush();
            }
        }
        // This is must by the outputStream specification,
        // May be called due to internal error, or by closing the thread mid-writing.
        catch (IOException e) {
            if (this.TAG != null) {
                Log.v(this.TAG, "IOException occurred while writing. (connection closed ?)", e);
            }
        }
        // Will be called when the server is closing
        catch (InterruptedException ignored) {
            // No need to re-enable the flag, as at this point the method is closing
        }

        if (this.TAG != null) {
            Log.v(this.TAG, "Closing the writer thread.");
        }

        // Clean the thread
        this.clean();
    }


    /**
     * This method will clean up the resources of this class.
     */
    private synchronized void clean()
    {
        try {
            // Close only if wasn't closed
            if (!this.isClosed) {
                this.isClosed = true;
                this.outputStream.close();

                if (this.TAG != null) {
                    Log.v(this.TAG, "OutputStream was closed");
                }
            }
        }
        // This is must by the outputStream specification,
        // May be called due to internal error, or by closing the output stream twice.
        catch (IOException e) {
            if (this.TAG != null) {
                Log.v(this.TAG, "IOException occurred while closing. (connection closed twice ?)", e);
            }
        }
    }
}
