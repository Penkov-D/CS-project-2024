package com.msdkremote.commandserver;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class CommandServerReader
{
    // Tag for logcat
    private final String TAG;

    // The output stream to write the messages on
    private final InputStream inputStream;
    private boolean isClosed = false;

    // The command handler - the method called on new command.
    private final CommandServerReaderHandler commandHandler;

    // Thread holder
    private final Thread threadReader;


    /**
     * CommandServerThreadReader is a thread dedicated to inputStream of a socket,
     * or more specifically to handle the messages sent to this server.
     * This class is disposable, meaning that for new connection new class should be created.
     *
     * @param inputStream the InputStream to read from.
     * @param commandHandler handle to process the incoming commands.
     */
    public CommandServerReader(
            @NonNull InputStream inputStream,
            @NonNull CommandServerReaderHandler commandHandler)
    {
        this.inputStream = inputStream;
        this.commandHandler = commandHandler;
        this.TAG = null;

        // Start the thread
        this.threadReader = new Thread(this::run);
        this.threadReader.start();
    }


    /**
     * CommandServerThreadReader is a thread dedicated to inputStream of a socket,
     * or more specifically to handle the messages sent to this server.
     * This class is disposable, meaning that for new connection new class should be created.
     *
     * @param inputStream the InputStream to read from.
     * @param commandHandler handle to process the incoming commands.
     * @param TAG customizable tag to use with logcat, mainly for debugging.
     */
    public CommandServerReader(
            @NonNull InputStream inputStream,
            @NonNull CommandServerReaderHandler commandHandler,
            @NonNull String TAG)
    {
        this.inputStream = inputStream;
        this.commandHandler = commandHandler;
        this.TAG = TAG;

        // Start the thread
        this.threadReader = new Thread(this::run);
        this.threadReader.start();
    }


    /**
     * Immediately stopping this thread from running.
     * This method will return only when the thread is fully terminated,
     * or interrupt was called on the caller thread.
     *
     * @throws InterruptedException if the calling thread was interrupted.
     */
    public void stopServer() throws InterruptedException
    {
        synchronized (this)
        {
            // Check if this thread is running
            if (!this.threadReader.isAlive()) return;

            // Close the thread
            this.threadReader.interrupt();
            // The clean method should interrupt the thread if it is waiting for a message.
            // This is the way that thread will be closed most of the times.
            this.clean();
        }

        // Return from this method only when the thread was fully terminated.
        this.threadReader.join();
    }


    /**
     * Waits for this thread to end.
     *
     * @throws InterruptedException if the calling thread was interrupted.
     */
    public void joinServer() throws InterruptedException {
        this.threadReader.join();
    }


    /**
     * The core of this class. Dictates the behavior of the thread.
     */
    private void run()
    {
        try {
            // Wrap by writer, to make string reading by line easier.
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));

            String command = null;

            // readLine() will return null if the stream is closed.
            // Reading from the stream while it is opened
            while ((command = reader.readLine()) != null)
            {
                if (this.TAG != null)
                    Log.v(this.TAG, "Received command : " + command);

                // Handle the command
                this.commandHandler.onCommand(command);

                // Close the thread if it is terminated
                if (this.threadReader.isInterrupted())
                    break;
            }
        }
        // This is must by the outputStream specification,
        // May be called due to internal error, or by closing the thread mid-reading.
        catch (IOException e) {
            if (this.TAG != null) {
                Log.v(this.TAG, "IOException occurred. (connection closed ?)", e);
            }
        }

        if (this.TAG != null) {
            Log.v(this.TAG, "Closing the reader thread.");
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
                this.inputStream.close();

                if (this.TAG != null) {
                    Log.v(this.TAG, "InputStream was closed.");
                }
            }
        }
        // This is must by the outputStream specification,
        // May be called due to internal error, or by closing the output stream twice.
        catch (IOException e) {
            if (this.TAG != null) {
                Log.w(this.TAG, "IOException occurred while closing. (connection closed twice ?)", e);
            }
        }
    }
}
