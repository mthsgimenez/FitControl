package com.mthsgimenez.fitcontrol.infra.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class NotFoundWithIdentifierException extends RuntimeException {

    private final String entity;
    private final Object identifier;

    public NotFoundWithIdentifierException(String entity, Object identifier) {
        super(entity + " not found with identifier: " + identifier);
        this.entity = entity;
        this.identifier = identifier;
    }
}
