package com.mthsgimenez.fitcontrol.infra.email;

public record EmailMessage(
        String to,
        String subject,
        String text
){}
