package com.satellite.tracker.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the {@link TimedAspect} so the {@code @Timed} annotation used on
 * the controller layer is actually processed and turned into Micrometer
 * timers (requires spring-boot-starter-aop on the classpath).
 */
@Configuration
public class MetricsConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
