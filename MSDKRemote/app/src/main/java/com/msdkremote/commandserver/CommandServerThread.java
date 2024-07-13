package com.msdkremote.commandserver;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Bidirectional command server, for receiving commands and sending messages.
 */
class CommandServerThread
{
    // Thread configurations
    private final int port;
    private final CommandServerStateListener stateListener;


    // Thread specific variables
    private final Object threadLock = new Object();
    private Thread serverThread = null;

    private CommandServerThreadReader commandReader = null;
    private CommandServerThreadWriter commandWriter = null;

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;


    // Command Handlers
    private final Set<CommandServerHandler> commandHandlerSet = new HashSet<>();
    private final Object commandHandlerSetLock = new Object();

    private final MessageQueue messageQueue = new MessageQueue();


    public CommandServerThread(@Nullable CommandServerStateListener stateListener, int port)
    {
        this.stateListener = stateListener;
        this.port = port;
    }


    public synchronized void startServer()
    {
        if (serverThread != null && serverThread.isAlive())
            return;


    }



    public synchronized void stopServer()
    {
        if (serverThread == null)
            return;

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
    public void sendMessage(@NonNull String message) {
        messageQueue.addMessage(message);
    }



    public void addCommandHandler(@NonNull CommandServerHandler commandHandler)
    {
        synchronized (commandHandlerSetLock)
        {
            commandHandlerSet.add(commandHandler);
        }
    }

    public boolean removeCommandHandler(@NonNull CommandServerHandler commandHandler)
    {
        synchronized (commandHandlerSetLock)
        {
            return commandHandlerSet.remove(commandHandler);
        }
    }

    public void removeAllCommandHandlers()
    {
        synchronized (commandHandlerSetLock)
        {
            commandHandlerSet.clear();
        }
    }



    private void run()
    {
        if (this.stateListener != null)
            this.stateListener.onServerRunning();


    }
}
