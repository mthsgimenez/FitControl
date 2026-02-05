package com.mthsgimenez.fitcontrol.infra.ratelimit;

import java.time.Duration;

public record RateLimitConfig(
        Long limit,
        Duration window
){}
