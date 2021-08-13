// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2019, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
 // end::copyright[]
package io.openliberty.guides.system;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("unhealthy")
public class UnhealthyResource {
    
    @GET
    public String unhealthy() {
        SystemReadinessCheck.setUnhealthy();
        return System.getenv("HOSTNAME") + " is now unhealthy...\n";
    }
}
