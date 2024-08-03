package com.msdkremote.livecontrol;

import android.util.Log;

import com.msdkremote.commandserver.CommandServer;
import com.msdkremote.commandserver.CommandServerStateListener;
import com.msdkremote.livecontrol.regularStickManager.RegularStickManager;

import java.net.InetAddress;

public class ControlServerManager
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
    private static ControlServerManager instance = null;

    private ControlServerManager() {
        Log.i(TAG, "ControlServer was created for the first time!");
    }

    /**
     * Get instance of ControlServerManager
     *
     * @return single instance of ControlServerManager
     */
    public static ControlServerManager getInstance()
    {
        if (instance == null)
            instance = new ControlServerManager();

        return instance;
    }


    /* ------------------- Server Control ------------------- */

    /**
     * Initiate ControlServer on specific port.
     *
     * @param port port number used by server.
     */
    public synchronized void startServer(int port)
    {
        // Check if server already running
        if (this.commandServer != null) {
            Log.w(TAG, "Control Server already running.");
            return;
        }

        // Opens new server
        Log.i(TAG, "Starting new Control Server, port : " + port + ".");
        this.commandServer = new CommandServer(new commandServerStateListener(), port);

        commandServer.addCommandHandler(new ControlCommandHandler(new RegularStickManager()));
        commandServer.startServer();
    }


    /**
     * Stops the ControlServer.
     *
     * @throws InterruptedException if current thread was interrupted mid waiting.
     */
    public synchronized void killServer() throws InterruptedException
    {
        // Check if server already terminated
        if (this.commandServer == null) {
            Log.w(TAG, "Control Server already closed.");
            return;
        }

        // Stops control server
        Log.i(TAG, "Stop Control Server.");
        this.commandServer.removeAllCommandHandlers();
        this.commandServer.stopServer();
        this.commandServer = null;
    }


    /* ------------------- State Listener ------------------- */

    /**
     * Sets a state listener for the ControlServer.
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
            synchronized (ControlServerManager.this.StateListenerLock) {
                if (ControlServerManager.this.stateListener != null) {
                    ControlServerManager.this.stateListener.onServerRunning();
                }
            }
        }

        @Override
        public void onServerClosed() {
            synchronized (ControlServerManager.this.StateListenerLock) {
                if (ControlServerManager.this.stateListener != null) {
                    ControlServerManager.this.stateListener.onServerClosed();
                }
            }
        }

        @Override
        public void onServerException(Exception e) {
            synchronized (ControlServerManager.this.StateListenerLock) {
                if (ControlServerManager.this.stateListener != null) {
                    ControlServerManager.this.stateListener.onServerException(e);
                }
            }
        }

        @Override
        public void onClientConnected(InetAddress address) {
            synchronized (ControlServerManager.this.StateListenerLock) {
                if (ControlServerManager.this.stateListener != null) {
                    ControlServerManager.this.stateListener.onClientConnected(address);
                }
            }
        }

        @Override
        public void onClientDisconnected() {
            synchronized (ControlServerManager.this.StateListenerLock) {
                if (ControlServerManager.this.stateListener != null) {
                    ControlServerManager.this.stateListener.onClientDisconnected();
                }
            }
        }
    }
}
