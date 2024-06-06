package com.msdkremote.livevideo;

import android.util.Log;
import android.widget.Toast;

import com.msdkremote.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class VideoServer
{
    private final String TAG = this.getClass().getSimpleName();

    private Thread socketThread = null;

    private final Object threadStateLock = new Object();

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;

    public VideoServer() { }

    public synchronized void startServer (int port, InputStream inStream)
    {
        if (socketThread != null)
            return;

        serverSocket = null;
        clientSocket = null;

        socketThread = new Thread(
            new Runnable() {
                @Override
                public void run() {

                    Log.i(TAG, "Starting server port - " + port);

                    try {
                        serverSocket = new ServerSocket(port);
                    }
                    catch (IOException e) {
                        Log.e(TAG, "Could not create ServerSocket", e);
                        cleanSockets();
                        return;
                    }

                    Log.i(TAG, "Server socket set and ready");

                    synchronized (threadStateLock) {
                        if (socketThread.isInterrupted()) {
                            cleanSockets();
                            return;
                        }
                    }

                    while (!socketThread.isInterrupted()) {

                        Log.i(TAG, "Waiting for new client");

                        try {
                            clientSocket = serverSocket.accept();

                            Log.i(TAG, "New client connected");

                            synchronized (threadStateLock) {
                                if (socketThread.isInterrupted()) {
                                    cleanSockets();
                                    return;
                                }
                            }

                            OutputStream oStream = clientSocket.getOutputStream();

                            byte[] data = new byte[1024];
                            int readed = 0;

                            while ((readed = inStream.read(data)) != -1) {
                                oStream.write(data, 0, readed);
                                oStream.flush();
                            }
                        }
                        catch (IOException e) {
                            Log.e(TAG, "A wild IOException occurred", e);
                        }
                        finally {
                            Log.i(TAG, "Client disconnected");

                            if (clientSocket != null) {
                                try {
                                    clientSocket.close();
                                } catch (IOException ignore) { }
                            }
                        }
                    }

                    cleanSockets();
                }

                private void cleanSockets()
                {
                    Log.i(TAG, "Stopping server port - " + port);

                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException ignore) { }
                    }

                    if (clientSocket != null) {
                        try {
                            clientSocket.close();
                        } catch (IOException ignore) { }
                    }
                }
            }
        );

        socketThread.start();
    }

    public synchronized void stopServer () throws InterruptedException
    {
        if (socketThread == null)
            return;


        synchronized (threadStateLock)
        {
            socketThread.interrupt();

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignore) { }
            }

            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException ignore) { }
            }
        }

        socketThread.join();
        socketThread = null;
    }
}
