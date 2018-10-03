package io.openliberty.guides.name;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("kill")
public class KillResource {
    @POST
    public String kill() {
        NameHealth.kill();
        return System.getenv("HOSTNAME") + " is now unhealthy...";
    }
}
