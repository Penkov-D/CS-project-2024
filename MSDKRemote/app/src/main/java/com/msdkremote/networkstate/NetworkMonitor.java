package com.msdkremote.networkstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

public class NetworkMonitor
{
    // Set for all the listeners on this monitor
    private final Set<NetworkChangeListener> listeners = new LinkedHashSet<>();

    // Connectivity manager - to get the connection type
    private final ConnectivityManager connectivityManager;

    // WifiManager - to get the wifi IP address
    private final WifiManager wifiManager;


    /**
     * Construct NetworkMonitor that will watch on network state changes,
     * and will notify listeners of current network type and IP address.
     *
     * @param context context of the application.
     */
    public NetworkMonitor(Context context)
    {
        // Get ConnectivityManager and WifiManager for future use
        connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        // Register network state listener, which in his turn notify its listeners.
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder().build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override public void onAvailable(@NonNull Network network) { notifyListeners(); }
                    @Override public void onLost(@NonNull Network network) { notifyListeners(); }
                }
        );
    }


    /**
     * Register network listener that will be called on network state change.
     * Will be called upon registration with latest data.
     *
     * @param listener the listener to add.
     */
    public synchronized void registerListener(NetworkChangeListener listener)
    {
        listeners.add(listener);

        // Inform the listener with current information
        listener.onNetworkChange(getCurrentNetworkType(), getIPAddress());
    }


    /**
     * Remove specific network listener from this network monitor.
     *
     * @param listener the listener to remove.
     */
    public synchronized void unregisterListener(NetworkChangeListener listener) {
        listeners.remove(listener);
    }


    /**
     * Remove all the network listeners from this network monitor.
     */
    public synchronized void removeAllListeners() {
        listeners.clear();
    }


    /**
     * Inner method - notifying all the listeners.
     */
    private synchronized void notifyListeners()
    {
        NetworkType networkType = getCurrentNetworkType();
        InetAddress address = getIPAddress();

        for (NetworkChangeListener listener : listeners) {
            listener.onNetworkChange(networkType, address);
        }
    }

    /**
     * Inner method - get current network type.
     *
     * @return current network connection type.
     */
    private NetworkType getCurrentNetworkType()
    {
        // Get current network state
        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        // No network connection presents
        if (capabilities == null)
            return NetworkType.NETWORK_DISCONNECTED;

        // Check if network connected to wifi
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            return NetworkType.NETWORK_WIFI;

        // Check if network connected to mobile network
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return NetworkType.NETWORK_MOBILE;

        // Unknown capabilities
        return NetworkType.NETWORK_UNKNOWN;
    }


    /**
     * Inner method - get current IP address (if connected to wifi)
     *
     * @return InetAddress
     */
    private InetAddress getIPAddress()
    {
        try {
            // Get current wifi address (represented by int)
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

            // Convert int to byte array for InetAddress
            byte[] bytes = {
                    (byte) (ipAddress),
                    (byte) (ipAddress >>> 8),
                    (byte) (ipAddress >>> 16),
                    (byte) (ipAddress >>> 24),
            };

            // Create InetAddress
            return InetAddress.getByAddress(bytes);
        }
        catch (UnknownHostException ignored) {
            // Thrown only if the bytes array is of illegal size (not 4 or 16)
        }

        // Will never happen
        return null;
    }
}
