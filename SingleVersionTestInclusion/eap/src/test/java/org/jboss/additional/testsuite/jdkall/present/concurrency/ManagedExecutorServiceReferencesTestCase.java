/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.additional.testsuite.jdkall.present.concurrency;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 @author Peter Mackay
 @EATSERVERS : Wildfly,Eap72x,Eap72x-Proposed
 @EATSERVERMODULE : concurrency
 @EATLOWERLIMITVERSIONS : 18.0.0.Final, 7.2.5, 7.2.5
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(ManagedExecutorServiceServerSetup.class)
public class ManagedExecutorServiceReferencesTestCase {

    private static final String ARCHIVE_NAME = "jbeap17412";

    @Deployment(name = "DEFAULT", managed = false, testable = false)
    public static Archive<?> getDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + ".war");
        archive.addClass(Member.class);
        archive.addClass(PollMembersTask.class);
        archive.addClass(TestServlet.class);
        archive.addAsWebInfResource("beans.xml");
        archive.addAsWebInfResource("ds.xml");
        archive.addAsResource("import.sql");
        archive.addAsResource("persistence.xml", "META-INF/persistence.xml");
        return archive;
    }

    /**
     * Test case for https://issues.jboss.org/browse/JBEAP-17412
     * Checks that ManagedExecutorService threads don't keep old references on application redeployment.
     */
    @Test
    @OperateOnDeployment("DEFAULT")
    public void testThreadsNotKeepingReferencesOnRedeploy(@ArquillianResource Deployer deployer) throws Exception {
        String deplyomentUrl = "http://" + TestSuiteEnvironment.getServerAddress() + ":" + TestSuiteEnvironment.getHttpPort() + "/" + ARCHIVE_NAME + TestServlet.URL_PATTERN;
        deployer.deploy("DEFAULT");
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpGet req = new HttpGet(deplyomentUrl);
            response = httpClient.execute(req);
            assertOkResponse(response, "1");

            // redeploy and check that ManagedExecutorService threads are not keeping old references
            deployer.undeploy("DEFAULT");
            deployer.deploy("DEFAULT");
            response.close();
            response = httpClient.execute(req);
            assertOkResponse(response, "1");
        } finally {
            deployer.undeploy("DEFAULT");
            response.close();
            httpClient.close();
        }
    }

    private void assertOkResponse(HttpResponse response, String expected) throws IOException {
        Assert.assertEquals("Request not successful. Might be because of https://issues.jboss.org/browse/JBEAP-17412.",
                HTTP_OK, response.getStatusLine().getStatusCode());
        String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Assert.assertEquals(expected, responseContent);
    }

}
