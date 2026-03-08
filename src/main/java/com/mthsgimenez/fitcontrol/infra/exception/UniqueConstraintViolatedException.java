package com.mthsgimenez.fitcontrol.infra.exception;

import lombok.Getter;

@Getter
public class UniqueConstraintViolatedException extends RuntimeException {
    private final String entity;
    private final String field;
    private final Object conflictingValue;

    public UniqueConstraintViolatedException(
            String entity, String field, String conflictingValue
    ) {
        super(entity + " with " + field + " = " + conflictingValue + " already exists");
        this.entity = entity;
        this.field = field;
        this.conflictingValue = conflictingValue;
    }
}
