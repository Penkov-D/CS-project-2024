package com.msdkremote.commandserver;


import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Bidirectional command server, for receiving commands and sending messages.
 */
public class CommandServer
{
    /* --------------------------- Local Variables --------------------------- */

    // Thread configurations
    private final int port;
    private final String TAG;
    private final CommandServerStateListener stateListener;


    // Thread specific variables
    private final Object threadLock = new Object();
    private Thread serverThread = null;
    private boolean serverStarted = false;

    private CommandServerReader commandReader = null;
    private CommandServerWriter commandWriter = null;

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;


    // Command Handlers
    private final Set<CommandHandler> commandHandlerSet = new HashSet<>();
    private final Object commandHandlerSetLock = new Object();

    private final MessageQueue messageQueue = new MessageQueue();



    /* --------------------------- Basic Commands --------------------------- */

    /**
     * Construct new bidirectional command server.
     * The command will not log on logcat.
     *
     * @param stateListener listener on the server state.
     * @param port port number for the server to listen on.
     */
    public CommandServer(
            @Nullable CommandServerStateListener stateListener,
            @IntRange(from = 1, to = 65535) int port)
    {
        this.stateListener = stateListener;
        this.port = port;
        this.TAG = null;
    }


    /**
     * Construct new bidirectional command server.
     *
     * @param stateListener listener on the server state.
     * @param port port number for the server to listen on.
     * @param TAG tag to print with on logcat.
     */
    public CommandServer(
            @Nullable CommandServerStateListener stateListener,
            @IntRange(from = 1, to = 65535) int port,
            @NonNull String TAG)
    {
        this.stateListener = stateListener;
        this.port = port;
        this.TAG = TAG;
    }


    /**
     * Start the server thread in a new thread.
     */
    public synchronized void startServer()
    {
        // If the server is alive, don't do anything
        if (serverStarted || (serverThread != null && serverThread.isAlive())) {
            log_i("Start server called on running server.");
            return;
        }

        // Flag to stop the server from double running
        serverStarted = true;

        // Lunch server
        log_i("Starting server.");

        // Reset all the inner variables
        this.commandReader = null;
        this.commandWriter = null;

        this.serverSocket = null;
        this.clientSocket = null;

        // Start the server thread
        this.serverThread = new Thread(this::run);
        this.serverThread.start();
    }


    /**
     * Stops the server thread from running.
     * This method will return after the thread is terminated.
     *
     * @throws InterruptedException if calling thread interrupted while executing this method.
     */
    public synchronized void stopServer() throws InterruptedException
    {
        // If the server is closed, don't do anything
        if (serverThread == null) {
            log_i("Stop server called on terminated server.");
            return;
        }

        // Terminate server
        log_i("Terminating the server.");

        // Rise interrupt flag
        this.serverThread.interrupt();

        // Stop the thread if its waiting for new clients
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            }
            catch (IOException ignored) { }
        }

        // Closing the client should be enough to make all the threads to close.
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();
            }
            catch (IOException ignored) { }
        }

        // Wait for the thread to fully close.
        this.serverThread.join();
        log_i("Server terminated.");

        // Reset all the inner variables
        if (this.commandReader != null) {
            this.commandReader.stopServer();
            this.commandReader = null;
        }

        if (this.commandWriter != null) {
            this.commandWriter.stopServer();
            this.commandWriter = null;
        }

        this.serverSocket = null;
        this.clientSocket = null;

        this.serverThread = null;
    }


    /**
     * The server thread main function.
     * This function is the solly defines the server thread.
     */
    private void run()
    {
        // The server started -
        // from now can look at thread.isAlive()
        serverStarted = false;

        // Inform the state listener that the server started
        if (this.stateListener != null)
            this.stateListener.onServerRunning();

        // Create server socket
        try {
            log_v("Creating new server socket.");
            this.serverSocket = new ServerSocket(this.port);
        }
        catch (IOException e) {
            // Inform about exception
            if (this.stateListener != null)
                this.stateListener.onServerException(e);

            log_w("Couldn't create server socket.", e);
            return;
        }

        try {
            // Network loop
            while (!this.serverThread.isInterrupted())
            {
                // Accept new client
                this.clientSocket = this.serverSocket.accept();

                // Catch interrupts that occurred while accepting the client.
                if (this.serverThread.isInterrupted())
                    break;

                // Inform the state listener about new client
                if (this.stateListener != null)
                    this.stateListener.onClientConnected(this.clientSocket.getInetAddress());

                // Create command reader and writer without tag
                if (this.TAG == null) {
                    this.commandWriter = new CommandServerWriter(
                            this.clientSocket.getOutputStream(),
                            this.messageQueue);
                    this.commandReader = new CommandServerReader(
                            this.clientSocket.getInputStream(),
                            this.commandDistribute);
                }
                // Create command reader and writer wit tag
                else {
                    this.commandWriter = new CommandServerWriter(
                            this.clientSocket.getOutputStream(),
                            this.messageQueue,
                            this.TAG);
                    this.commandReader = new CommandServerReader(
                            this.clientSocket.getInputStream(),
                            this.commandDistribute,
                            this.TAG);
                }

                // Wait for the command reader threads to close.
                // this will happen when the connection is closed.
                this.commandReader.joinServer();
                this.commandWriter.stopServer();

                this.commandWriter = null;
                this.commandReader = null;
                this.clientSocket = null;

                if (this.stateListener != null)
                    this.stateListener.onClientDisconnected();
            }
        }
        catch (InterruptedException e) {
            log_i("Server thread was interrupted and closing.");
        }
        catch (IOException e) {
            // Didn't call state listener as it might be due to closing the thread.
            log_w("Server thread got IO exception", e);
        }
        finally {
            // Close the server socket
            try {
                this.serverSocket.close();
            } catch (IOException ignored) { }

            // Close the client socket
            try {
                if (this.clientSocket != null)
                    this.serverSocket.close();
            } catch (IOException ignored) { }
        }

        // Inform the state listener that the server is closing
        if (this.stateListener != null)
            this.stateListener.onServerClosed();

    }


    /**
     * Sending specific message over this command server.
     * Note: this will schedule the message,
     *   and send it only when a connection is established.
     *
     * @param message the message to send.
     */
    public void sendMessage(@NonNull String message)
    {
        log_v("Message registered: " + message);
        messageQueue.addMessage(message);
    }


    /**
     * Log informative message (if TAG was set).
     *
     * @param message message to log.
     */
    private void log_i(@NonNull String message)
    {
        if (this.TAG != null)
            Log.i(this.TAG, message);
    }

    /**
     * Log verbose message (if TAG was set).
     *
     * @param message message to log.
     */
    private void log_v(@NonNull String message)
    {
        if (this.TAG != null)
            Log.v(this.TAG, message);
    }

    /**
     * Log error message (if TAG was set).
     *
     * @param message message to log.
     */
    private void log_w(@NonNull String message)
    {
        if (this.TAG != null)
            Log.w(this.TAG, message);
    }

    /**
     * Log error message and its error (if TAG was set).
     *
     * @param message message to log.
     * @param e error message to log.
     */
    private void log_w(@NonNull String message, @NonNull Throwable e)
    {
        if (this.TAG != null)
            Log.w(this.TAG, message, e);
    }



    /* --------------------------- Command Handlers --------------------------- */

    /**
     * Adding command handler to this server.
     * All the handlers will receive the same messages,
     * its up to them to process rightly the command.
     *
     * @param commandHandler handler to add receiving updates on.
     */
    public void addCommandHandler(@NonNull CommandHandler commandHandler)
    {
        synchronized (commandHandlerSetLock) {
            log_v("Adding new handler to the server.");
            commandHandlerSet.add(commandHandler);
        }
    }


    /**
     * Removes handler from the server.
     *
     * @param commandHandler the handler to remove from this server.
     * @return true if the handler was removed,
     *         false if the handler wasn't registered in the first place.
     */
    public boolean removeCommandHandler(@NonNull CommandHandler commandHandler)
    {
        synchronized (commandHandlerSetLock) {
            log_v("Removing handler from the server.");
            return commandHandlerSet.remove(commandHandler);
        }
    }


    /**
     * Removes all the handles from this server.
     * If a command comes when there are no handles,
     * the message is simply discarded.
     */
    public void removeAllCommandHandlers()
    {
        synchronized (commandHandlerSetLock) {
            log_v("Removing all handlers from the server.");
            commandHandlerSet.clear();
        }
    }


    /**
     * Proxy handler, to call all the registered handlers with pointer
     * to the server which the call was made from.
     */
    private final CommandServerReaderHandler commandDistribute
            = new CommandServerReaderHandler() {
        @Override
        public void onCommand(@NonNull String command)
        {
            log_v("New command received: " + command);

            // Get the list of the handlers, to not stuck on the lock.
            CommandHandler[] handlers = new CommandHandler[0];
            synchronized (commandHandlerSetLock) {
                handlers = commandHandlerSet.toArray(handlers);
            }

            // Iterate over the handlers
            for (CommandHandler handler : handlers)
                handler.onCommand(CommandServer.this, command);
        }
    };
}
