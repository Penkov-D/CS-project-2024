package com.msdkremote.livequery;

import android.util.Log;

import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.commandserver.CommandServerStateListener;

import java.net.InetAddress;

public class QueryServerManager
{
    // Logging TAG
    private final String TAG = this.getClass().getSimpleName();

    // Query Server instance
    private CommandServer queryServer = null;

    // State listener - limiting to one listener
    private final Object StateListenerLock = new Object();
    private CommandServerStateListener stateListener = null;


    /* ------------------- Singleton ------------------- */

    // Query Server Manager instance - singleton
    private static QueryServerManager instance = null;

    private QueryServerManager() {
        Log.i(TAG, "QueryServer was created for the first time!");
    }

    /**
     * Get instance of QueryServerManager
     *
     * @return single instance of QueryServerManager
     */
    public static QueryServerManager getInstance()
    {
        if (instance == null)
            instance = new QueryServerManager();

        return instance;
    }



    /* ------------------- Server Control ------------------- */

    /**
     * Initiate QueryServer on specific port.
     *
     * @param port port number used by server.
     */
    public synchronized void startServer(int port)
    {
        // Check if server already running
        if (this.queryServer != null) {
            Log.w(TAG, "Query Server already running.");
            return;
        }

        // Opens new server
        Log.i(TAG, "Starting new Query Server, port : " + port + ".");
        this.queryServer = new CommandServer(new commandServerStateListener(), port);

        queryServer.addCommandHandler(new QueryCommandHandler());
        queryServer.startServer();
    }


    /**
     * Stops the QueryServer.
     *
     * @throws InterruptedException if current thread was interrupted mid waiting.
     */
    public synchronized void killServer() throws InterruptedException
    {
        // Check if server already terminated
        if (this.queryServer == null) {
            Log.w(TAG, "Query Server already closed.");
            return;
        }

        // Stops control server
        Log.i(TAG, "Stop Query Server.");
        this.queryServer.removeAllCommandHandlers();
        this.queryServer.stopServer();
        this.queryServer = null;
    }


    /* ------------------- State Listener ------------------- */

    /**
     * Sets a state listener for the QueryServer.
     * <br>
     * Note: There can be only one state listener over the program.
     * <br>
     * Note: Setting state listener when one already registered will remove
     *       the previous state listener.
     *
     * @param listener the state listener to set.
     */
    public synchronized void setStateListener(CommandServerStateListener listener)
    {
        synchronized (StateListenerLock) {
            this.stateListener = listener;
        }
    }

    /**
     * Remove the state listener over the ControlServer.
     */
    public synchronized void resetStateListener()
    {
        synchronized (StateListenerLock) {
            this.stateListener = null;
        }
    }

    // Inner class to give the ability to add listener mid running.
    private class commandServerStateListener implements CommandServerStateListener
    {
        @Override
        public void onServerRunning() {
            synchronized (QueryServerManager.this.StateListenerLock) {
                if (QueryServerManager.this.stateListener != null) {
                    QueryServerManager.this.stateListener.onServerRunning();
                }
            }
        }

        @Override
        public void onServerClosed() {
            synchronized (QueryServerManager.this.StateListenerLock) {
                if (QueryServerManager.this.stateListener != null) {
                    QueryServerManager.this.stateListener.onServerClosed();
                }
            }
        }

        @Override
        public void onServerException(Exception e) {
            synchronized (QueryServerManager.this.StateListenerLock) {
                if (QueryServerManager.this.stateListener != null) {
                    QueryServerManager.this.stateListener.onServerException(e);
                }
            }
        }

        @Override
        public void onClientConnected(InetAddress address) {
            synchronized (QueryServerManager.this.StateListenerLock) {
                if (QueryServerManager.this.stateListener != null) {
                    QueryServerManager.this.stateListener.onClientConnected(address);
                }
            }
        }

        @Override
        public void onClientDisconnected() {
            synchronized (QueryServerManager.this.StateListenerLock) {
                if (QueryServerManager.this.stateListener != null) {
                    QueryServerManager.this.stateListener.onClientDisconnected();
                }
            }
        }
    }
}
