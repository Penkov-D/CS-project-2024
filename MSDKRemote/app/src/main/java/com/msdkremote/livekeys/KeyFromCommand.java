package com.msdkremote.livekeys;

import androidx.annotation.NonNull;

import dji.sdk.keyvalue.key.BatteryKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.common.LocationCoordinate2D;

public class KeyFromCommand {

    protected String commandWord = "";
    protected String keyClass = "";
    protected String keyName = "";
    protected String param = "";

    public KeyFromCommand (String command) {
        // Split the command into words
        String[] words = command.toLowerCase().strip().split(" ");

        // Assign the words to the respective variables
        if (words.length >= 3) {
            commandWord = words[0];
            keyClass = words[1];
            keyName = words[2];
        }

        // Join the remaining words into the param
        if (words.length > 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < words.length; i++) {
                sb.append(words[i]);
                if (i < words.length - 1) {
                    sb.append(" ");
                }
            }
            param = sb.toString();
        }
    }

    public KeyItem<?, ?> getKeyItem(){
        return new KeyItem<LocationCoordinate2D, LocationCoordinate2D>(FlightControllerKey.KeyHomeLocation);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Command: %s, keyClass: %s, keyName: %s, param: %s", commandWord, keyClass, keyName, param);
    }
}
