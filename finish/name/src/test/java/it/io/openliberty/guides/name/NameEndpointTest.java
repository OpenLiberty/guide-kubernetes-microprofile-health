// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;

public class NameEndpointTest {

    private static String clusterUrl;
    private static String healthUrl;
    private static int sleepTime;

    private Client client;
    private Response response;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        String clusterIp = System.getProperty("cluster.ip");
        String nodePort = System.getProperty("name.node.port");
        sleepTime = Integer.parseInt(System.getProperty("test.sleep.time"));

        String baseUrl = "http://" + clusterIp + ":" + nodePort;
        clusterUrl = baseUrl + "/api/name/";
        healthUrl = baseUrl + "/health";

        ApiClient apiClient = Config.defaultClient();
        Configuration.setDefaultApiClient(apiClient);
    }
    
    @Before
    public void setup() {
        response = null;
        client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
    }

    @After
    public void teardown() {
        response.close();
        client.close();
    }
    
    @Test
    public void testContainerNameNotNull() {
        response = this.getResponse(clusterUrl);
        this.assertResponse(clusterUrl, response);
        String greeting = response.readEntity(String.class);
        
        String containerName = greeting.substring(greeting.lastIndexOf(" ") + 1);
        containerName = (containerName.equals("null")) ? null : containerName;
        assertNotNull("Container name should not be null but it was. The service is robably not running inside a container",
            containerName);
    }

    @Test
    public void testHealthEndpoint() {
        response = this.getResponse(healthUrl);
        this.assertResponse(healthUrl, response);
    }

    @Test
    public void testNotReady() throws InterruptedException, ApiException {
        String unhealthyUrl = clusterUrl + "unhealthy";

        // Make pod unhealthy
        response = client.target(unhealthyUrl).request().post(null);
        this.assertResponse(unhealthyUrl, response);

        // Wait for the readiness probe to pick up the change in status
        Thread.sleep(6000);

        // Check that the pod is no longer READY
        String responseText = response.readEntity(String.class);
        String podName = responseText.substring(0, responseText.indexOf(' '));

        CoreV1Api kubeApi = new CoreV1Api();
        V1Pod pod = kubeApi.readNamespacedPod(podName, "default", null, null, null);

        Boolean isReady = pod.getStatus().getContainerStatuses().get(0).isReady();
        assertFalse(
            String.format(
                "Expected: %s is not ready. Actual: %s is %s.",
                podName,
                podName,
                isReady ?  "ready" : "not ready"),
            isReady);

        Thread.sleep(sleepTime);
    }

    /**
     * <p>
     * Returns response information from the specified URL.
     * </p>
     * 
     * @param url
     *          - target URL.
     * @return Response object with the response from the specified URL.
     */
    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    /**
     * <p>
     * Asserts that the given URL has the correct response code of 200.
     * </p>
     * 
     * @param url
     *          - target URL.
     * @param response
     *          - response received from the target URL.
     */
    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }

}
