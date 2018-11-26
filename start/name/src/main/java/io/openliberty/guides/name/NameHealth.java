package io.openliberty.guides.name;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class NameHealth implements HealthCheck {
    private static final int ALIVE_DELAY_SECONDS = 60;
    private static LocalDateTime aliveAfter = LocalDateTime.now();

    @Override
    public HealthCheckResponse call() {
        if (isAlive()) {
            return HealthCheckResponse.named("isAlive").up().build();
        }

        return HealthCheckResponse.named("isAlive").down().build();
    }

    public static void setUnhealthy() {
        aliveAfter = LocalDateTime.now().plusSeconds(ALIVE_DELAY_SECONDS);
    }

    private static boolean isAlive() {
        return LocalDateTime.now().isAfter(aliveAfter);
    }
}
