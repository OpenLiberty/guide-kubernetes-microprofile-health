package io.openliberty.guides.ping;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.guides.ping.client.NameClient;
import io.openliberty.guides.ping.client.UnknownUrlException;

@Health
@ApplicationScoped
public class PingHealth implements HealthCheck {
    @Inject
    @ConfigProperty(name = "NAME_HOSTNAME")
    private String hostname;

    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(hostname);
        if (isNameServiceReachable()) {
            builder = builder.up();
        } else {
            builder = builder.down();
        }

        return builder.build();
    }

    private boolean isNameServiceReachable() {
        try {
            NameClient client = RestClientBuilder
                .newBuilder()
                .baseUrl(new URL("http://" + hostname + ":9080/api"))
                .register(UnknownUrlException.class)
                .build(NameClient.class);

            client.getContainerName();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}