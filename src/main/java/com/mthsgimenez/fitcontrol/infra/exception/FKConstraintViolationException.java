package com.mthsgimenez.fitcontrol.infra.exception;

public class FKConstraintViolationException extends RuntimeException {
    private final String entity;
    private final Object identifier;

    public FKConstraintViolationException(String entity, Object identifier) {
        super("cannot delete " + entity + " with identifier: " + identifier + ". " + entity + " is being referenced by other data");
        this.entity = entity;
        this.identifier = identifier;
    }
}
