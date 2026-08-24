package com.laneway.reliability;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
    name = "laneway.reliability.scheduling-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ReliabilityConfig {}
