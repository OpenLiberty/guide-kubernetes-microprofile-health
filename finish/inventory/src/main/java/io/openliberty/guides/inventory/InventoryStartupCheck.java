// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::InventoryStartupCheck[]
package io.openliberty.guides.inventory;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Startup;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

// tag::Startup[]
@Startup
// end::Startup[]
@ApplicationScoped
public class InventoryStartupCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
        long cpuUsed = bean.getSystemCpuLoad();
        return HealthCheckResponse.named(SystemResource.class
                                            .getSimpleName() + " Startup Check")
                                            .withData("cpu used", cpuUsed)
                                            .status(cpuUsed < 0.95).build();
    }
}

// end::InventoryStartupCheck[]
