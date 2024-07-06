package com.msdkremote.networkstate;

import androidx.annotation.NonNull;

public enum NetworkType
{
    NETWORK_WIFI("wifi"),
    NETWORK_MOBILE("mobile"),
    NETWORK_UNKNOWN("unknown"),
    NETWORK_DISCONNECTED("disconnected");

    private final String typeString;

    private NetworkType(String string) {
        this.typeString = string;
    }

    @NonNull
    @Override
    public String toString() {
        return typeString;
    }
}
