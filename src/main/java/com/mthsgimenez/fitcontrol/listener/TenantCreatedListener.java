package com.mthsgimenez.fitcontrol.listener;

import com.mthsgimenez.fitcontrol.event.TenantCreatedEvent;
import com.mthsgimenez.fitcontrol.service.SchemaService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TenantCreatedListener {

    private final SchemaService schemaService;

    public TenantCreatedListener(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTenantCreated(TenantCreatedEvent event) {
        schemaService.createSchemaAndMigrate(event.schemaName());
    }
}
