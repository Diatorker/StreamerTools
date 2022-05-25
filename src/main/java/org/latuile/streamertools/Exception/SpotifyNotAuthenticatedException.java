package org.latuile.streamertools.Exception;

public class SpotifyNotAuthenticatedException extends Exception{
    public SpotifyNotAuthenticatedException() {
        super();
    }

    public SpotifyNotAuthenticatedException(String message) {
        super(message);
    }

    public SpotifyNotAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpotifyNotAuthenticatedException(Throwable cause) {
        super(cause);
    }

    protected SpotifyNotAuthenticatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
