package io.openliberty.guides.system;
	
	import javax.enterprise.context.ApplicationScoped;
	import org.eclipse.microprofile.health.Health;
	import org.eclipse.microprofile.health.Readiness;
	import org.eclipse.microprofile.health.HealthCheck;
	import org.eclipse.microprofile.health.HealthCheckResponse;
	
	@Health
	@Readiness
	@ApplicationScoped
	public class SystemHealth implements HealthCheck {
	public class SystemReadinessCheck implements HealthCheck {
	  @Override
	  public HealthCheckResponse call() {
	    if (!System.getProperty("wlp.server.name").equals("defaultServer")) {