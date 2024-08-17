package com.msdkremote.livekeys;

import android.util.Log;
import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.commandserver.CommandServerStateListener;
import java.net.InetAddress;

public class KeyServerManager
{
    // Logging TAG
    private final String TAG = this.getClass().getSimpleName();

    // Command Server instance
    private CommandServer commandServer = null;

    // State listener - limiting to one listener
    private final Object StateListenerLock = new Object();
    private CommandServerStateListener stateListener = null;


    /* ------------------- Singleton ------------------- */

    // Control Server Manager instance - singleton
    private static KeyServerManager instance = null;

    private KeyServerManager() {
        Log.i(TAG, "KeyServer was created for the first time!");
    }

    /**
     * Get instance of KeyServerManager
     *
     * @return single instance of KeyServerManager
     */
    public static KeyServerManager getInstance()
    {
        if (instance == null)
            instance = new KeyServerManager();

        return instance;
    }


    /* ------------------- Server Control ------------------- */

    /**
     * Initiate KeyServer on specific port.
     *
     * @param port port number used by server.
     */
    public synchronized void startServer(int port)
    {
        // Check if server already running
        if (this.commandServer != null) {
            Log.w(TAG, "Key Server already running.");
            return;
        }

        // Opens new server
        Log.i(TAG, "Starting new Key Server, port : " + port + ".");
        this.commandServer = new CommandServer(new commandServerStateListener(), port);

        commandServer.addCommandHandler(new KeyCommandHandler());
        commandServer.startServer();
    }


    /**
     * Stops the KeyServer.
     *
     * @throws InterruptedException if current thread was interrupted mid waiting.
     */
    public synchronized void killServer() throws InterruptedException
    {
        // Check if server already terminated
        if (this.commandServer == null) {
            Log.w(TAG, "Key Server already closed.");
            return;
        }

        // Stops control server
        Log.i(TAG, "Stop Key Server.");
        this.commandServer.removeAllCommandHandlers();
        this.commandServer.stopServer();
        this.commandServer = null;
    }


    /* ------------------- State Listener ------------------- */

    /**
     * Sets a state listener for the KeyServer.
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
     * Remove the state listener over the KeyServer.
     */
    public synchronized void resetStateListener()
    {
        synchronized (StateListenerLock) {
            this.stateListener = null;
        }
    }

    /**
     * Inner class to give the ability to add listener mid running.
     */
    private class commandServerStateListener implements CommandServerStateListener
    {
        @Override
        public void onServerRunning() {
            synchronized (KeyServerManager.this.StateListenerLock) {
                if (KeyServerManager.this.stateListener != null) {
                    KeyServerManager.this.stateListener.onServerRunning();
                }
            }
        }

        @Override
        public void onServerClosed() {
            synchronized (KeyServerManager.this.StateListenerLock) {
                if (KeyServerManager.this.stateListener != null) {
                    KeyServerManager.this.stateListener.onServerClosed();
                }
            }
        }

        @Override
        public void onServerException(Exception e) {
            synchronized (KeyServerManager.this.StateListenerLock) {
                if (KeyServerManager.this.stateListener != null) {
                    KeyServerManager.this.stateListener.onServerException(e);
                }
            }
        }

        @Override
        public void onClientConnected(InetAddress address) {
            synchronized (KeyServerManager.this.StateListenerLock) {
                if (KeyServerManager.this.stateListener != null) {
                    KeyServerManager.this.stateListener.onClientConnected(address);
                }
            }
        }

        @Override
        public void onClientDisconnected() {
            synchronized (KeyServerManager.this.StateListenerLock) {
                if (KeyServerManager.this.stateListener != null) {
                    KeyServerManager.this.stateListener.onClientDisconnected();
                }
            }
        }
    }
}
