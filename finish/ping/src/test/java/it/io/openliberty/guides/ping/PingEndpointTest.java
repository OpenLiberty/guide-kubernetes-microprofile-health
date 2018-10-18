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
package it.io.openliberty.guides.ping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

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
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

public class PingEndpointTest {

    private static String clusterUrl;
    private static String nameUnhealthyUrl;
    private static String nameKubeService;
    private static int sleepTime;

    private Client client;
    private Response response;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        String clusterIp = System.getProperty("cluster.ip");
        String nodePort = System.getProperty("ping.node.port");
        String nameNodePort = System.getProperty("name.node.port");
        sleepTime = Integer.parseInt(System.getProperty("test.sleep.time"));
        
        nameKubeService = System.getProperty("name.kube.service");
        clusterUrl = "http://" + clusterIp + ":" + nodePort + "/api/ping/";
        nameUnhealthyUrl = "http://" + clusterIp + ":" + nameNodePort + "/api/name/unhealthy";

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
    public void testPingValidService() {
        response = this.getResponse(clusterUrl + nameKubeService);
        this.assertResponse(clusterUrl, response);
        
        String expected = "pong";
        String actual = response.readEntity(String.class).trim();
        assertEquals("Should have receieved pong", expected, actual);
    }
    
    @Test
    public void testPingInvalidService() {
        String invalidServiceName = "donkey-pong";
        response = this.getResponse(clusterUrl + invalidServiceName);
        this.assertResponse(clusterUrl, response);
        
        String expected = "Bad response from "
            + invalidServiceName
            + "\nCheck the console log for more info.";

        String actual = response.readEntity(String.class);

        assertEquals("Should have received a bad response from "
            + invalidServiceName
            + ", but didn't. Is "
            + invalidServiceName
            + " a running Kuberentes service?",
            expected, actual);
    }

    @Test
    public void testNotReady() throws InterruptedException, ApiException {
        // Make both name pods unhealthy
        response = client.target(nameUnhealthyUrl).request().post(null);
        this.assertResponse(nameUnhealthyUrl, response);

        Thread.sleep(6000);

        response = client.target(nameUnhealthyUrl).request().post(null);
        this.assertResponse(nameUnhealthyUrl, response);

        Thread.sleep(6000);

        // Check status of ping pod
        CoreV1Api kubeApi = new CoreV1Api();

        V1Pod pod = getPingPod(kubeApi);
        String podName = pod.getMetadata().getName();

        Boolean isReady = pod.getStatus().getContainerStatuses().get(0).isReady();
        for (int i = 0; i < 24 && isReady; i++) {
            // Repeatedly check if pod is ready, as long as it is not ready
            // at least then the test should pass.
            pod = getPingPod(kubeApi);
            isReady = pod.getStatus().getContainerStatuses().get(0).isReady();
            Thread.sleep(500);
        }

        assertFalse(
            String.format(
                "Expected: %s is not ready. Actual: %s is %s.",
                podName,
                podName,
                isReady ?  "ready" : "not ready"),
            isReady);

        Thread.sleep(sleepTime);
    }

    private V1Pod getPingPod(CoreV1Api api) throws ApiException {
        V1PodList pods = api.listNamespacedPod("default", null, null, null, null, null, null, null, null, null);
        Optional<V1Pod> pod = pods
            .getItems()
            .stream()
            .filter(p -> p.getMetadata().getName().startsWith("ping-"))
            .findFirst();

        assertTrue(pod.isPresent());
        return pod.get();
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
