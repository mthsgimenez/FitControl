package com.mthsgimenez.fitcontrol.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundWithIdException extends RuntimeException {

    private final Class<?> entityClass;
    private final Integer id;

    public NotFoundWithIdException(Class<?> entityClass, Integer id) {
        super(entityClass.getSimpleName() + " not found with id: " + id);
        this.entityClass = entityClass;
        this.id = id;
    }

    public String getEntityName() {
        return entityClass.getSimpleName();
    }

    public Integer getEntityId() {
        return id;
    }
}
