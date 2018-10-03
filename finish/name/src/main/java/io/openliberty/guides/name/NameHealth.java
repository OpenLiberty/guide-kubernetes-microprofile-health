package io.openliberty.guides.name;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class NameHealth implements HealthCheck {
    private static boolean isAlive = true;

    @Override
    public HealthCheckResponse call() {
        if (isAlive) {
            return HealthCheckResponse.named("isAlive").up().build();
        }

        return HealthCheckResponse.named("isAlive").down().build();
    }
    
    public static void kill() {
        isAlive = false;
    }
}
