package com.mthsgimenez.fitcontrol.auth.refreshtokens;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
