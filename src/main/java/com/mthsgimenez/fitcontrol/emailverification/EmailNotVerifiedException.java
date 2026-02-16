package com.mthsgimenez.fitcontrol.emailverification;

public class EmailNotVerifiedException extends Exception {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
