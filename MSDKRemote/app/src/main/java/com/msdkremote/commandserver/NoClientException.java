package com.msdkremote.commandserver;

public class NoClientException extends Exception
{
    public NoClientException() {
        super();
    }

    public NoClientException(String message) {
        super(message);
    }

    public NoClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoClientException(Throwable cause) {
        super(cause);
    }
}
