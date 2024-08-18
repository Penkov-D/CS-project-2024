package com.msdkremote.livequery;

/**
 * Exception when a key-module is searched by non-existing name.
 */
public class UnknownModuleException extends Exception
{
    public UnknownModuleException() {
        super();
    }

    public UnknownModuleException(String message) {
        super(message);
    }

    public UnknownModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownModuleException(Throwable cause) {
        super(cause);
    }
}
