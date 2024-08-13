package com.msdkremote.livekeys;

public enum Command {
    GET,
    SET,
    LISTEN,
    CANCEL_LISTEN,
    ACTION,
    UNKNOWN;

    public static Command getCommand(String commandWord){
        switch (commandWord){
            case "get": return GET;
            case "set": return SET;
            case "listen": return LISTEN;
            case "cancel_listen": return CANCEL_LISTEN;
            case "action": return ACTION;
            default: return UNKNOWN;
        }
    }
}
