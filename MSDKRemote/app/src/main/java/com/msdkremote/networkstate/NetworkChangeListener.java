package com.msdkremote.networkstate;

import java.net.InetAddress;

public interface NetworkChangeListener
{
    /**
     * Network state listener - will be called each time the network state is changed.
     *
     * @param networkType the type of connected network.
     * @param address the address of current network.
     */
    void onNetworkChange (NetworkType networkType, InetAddress address);
}
