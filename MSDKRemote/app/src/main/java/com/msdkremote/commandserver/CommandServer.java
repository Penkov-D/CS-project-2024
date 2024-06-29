package com.msdkremote.commandserver;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandServer
{
    private final String TAG;

    private Thread socketThread = null;
    private final Object socketThreadLock = new Object();
    private CommandHandler commandHandler = null;

    private final int port;
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;

    public CommandServer(int port)
    {
        this.port = port;
        this.TAG = this.getClass().getSimpleName();
    }

    public CommandServer(int port, @NonNull String logName)
    {
        this.port = port;
        this.TAG = logName;
    }

    public synchronized void startServer (@NonNull CommandHandler handler)
    {
        if (socketThread != null) {
            this.commandHandler = handler;
            return;
        }

        this.commandHandler = handler;
        this.serverSocket = null;
        this.clientSocket = null;

        socketThread = new Thread(new ServerThread(this));
        socketThread.start();
    }

    public synchronized void stopServer () throws InterruptedException
    {
        if (socketThread == null) {
            this.commandHandler = null;
            this.serverSocket = null;
            this.clientSocket = null;
            return;
        }

        if (!socketThread.isAlive()) {
            this.socketThread = null;
            this.commandHandler = null;
            this.serverSocket = null;
            this.clientSocket = null;
            return;
        }

        synchronized (socketThreadLock)
        {
            this.socketThread.interrupt();

            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException ignore) { }
            }

            if (clientSocket != null) {
                try {
                    this.clientSocket.close();
                } catch (IOException ignore) { }
            }
        }

        this.socketThread.join();
        this.socketThread = null;
        this.commandHandler = null;
        this.serverSocket = null;
        this.clientSocket = null;
    }

    private static class ServerThread implements Runnable
    {
        CommandServer cs;

        public ServerThread(@NonNull CommandServer commandServer) {
            this.cs = commandServer;
        }

        @Override
        public void run()
        {
            this.cs.serverSocket = null;
            this.cs.clientSocket = null;

            try {
                Log.i(this.cs.TAG, "Initiate CommandServer!");
                this.cs.serverSocket = new ServerSocket(
                        this.cs.port);
            }
            catch (IOException e) {
                Log.e(this.cs.TAG, "Couldn't create CommandServer", e);
                cleanSockets();
                return;
            }

            synchronized (this.cs.socketThreadLock) {
                if (this.cs.socketThread.isInterrupted()) {
                    Log.i(this.cs.TAG, "Interrupted after server creation");
                    cleanSockets();
                    return;
                }
            }

            while (!this.cs.socketThread.isInterrupted())
            {
                try {
                    this.cs.clientSocket = this.cs.serverSocket.accept();
                    Log.i(this.cs.TAG, "Client connected : "
                            + this.cs.clientSocket.getInetAddress());

                    synchronized (this.cs.socketThreadLock) {
                        if (this.cs.socketThread.isInterrupted()) {
                            Log.i(this.cs.TAG, "Interrupted after accepting a client");
                            cleanSockets();
                            return;
                        }
                    }

                    this.cs.commandHandler.onClientConnected(this.cs.clientSocket.getInetAddress());

                    OutputStreamWriter streamWriter = new OutputStreamWriter(
                            this.cs.clientSocket.getOutputStream());

                    BufferedReader streamReader = new BufferedReader(
                            new InputStreamReader(
                                    this.cs.clientSocket.getInputStream()));

                    String line = null;
                    while ((line = streamReader.readLine()) != null
                            && !this.cs.socketThread.isInterrupted())
                    {
                        String retString = this.cs.commandHandler.onCommand(line);

                        if (retString != null) {
                            streamWriter.write(retString);
                            streamWriter.flush();
                        }
                    }

                    if (this.cs.socketThread.isInterrupted())
                    {
                        Log.i(this.cs.TAG, "Interrupted while handling a client");
                        cleanSockets();
                        return;
                    }
                }
                catch (IOException e) {
                    Log.e(this.cs.TAG, "A wild IOException occurred (interrupted ?)", e);
                }
                finally {
                    Log.i(this.cs.TAG, "Client disconnected");
                    this.cs.commandHandler.onClientDisconnected();

                    if (this.cs.clientSocket != null) {
                        try {
                            this.cs.clientSocket.close();
                        }
                        catch (IOException ignore) { }
                    }
                }
            }

            Log.i(this.cs.TAG, "Interrupted after handling clients");
            cleanSockets();
        }

        private void cleanSockets()
        {
            Log.i(this.cs.TAG, "Closing CommandServer");

            if (this.cs.serverSocket != null)
            {
                try {
                    this.cs.serverSocket.close();
                }
                catch (IOException e) {
                    Log.w(this.cs.TAG, "Error while closing CommandServer server socket", e);
                }
            }

            if (this.cs.clientSocket != null)
            {
                try {
                    this.cs.clientSocket.close();
                }
                catch (IOException e) {
                    Log.w(this.cs.TAG, "Error while closing CommandServer client socket", e);
                }
            }

            this.cs.serverSocket = null;
            this.cs.clientSocket = null;
        }
    }
}
