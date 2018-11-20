package io.openliberty.guides.name;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("unhealthy")
public class UnhealthyResource {
    @POST
    public String unhealthy() {
        NameHealth.setUnhealthy();
        return System.getenv("HOSTNAME") + " is now unhealthy...\n";
    }
}
