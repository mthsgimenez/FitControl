package com.mthsgimenez.fitcontrol.dto;

import java.time.Duration;

public record RateLimitDTO(
        Long limit,
        Duration window
){}
