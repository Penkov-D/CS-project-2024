package com.msdkremote.livequery;

/**
 * Exception when a key (DJIKey) is searched by non-existing name.
 */
public class UnknownKeyException extends Exception
{
    public UnknownKeyException() {
        super();
    }

    public UnknownKeyException(String message) {
        super(message);
    }

    public UnknownKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownKeyException(Throwable cause) {
        super(cause);
    }
}
