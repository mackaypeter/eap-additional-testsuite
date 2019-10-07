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

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;

import java.io.IOException;

/**
 @author Peter Mackay
 @EATSERVERS : Wildfly,Eap72x,Eap72x-Proposed
 @EATSERVERMODULE : concurrency
 @EATLOWERLIMITVERSIONS : 18.0.0.Final, 7.2.5, 7.2.5
 */
public class ManagedExecutorServiceServerSetup implements ServerSetupTask {
    @Override
    // /subsystem=ee/managed-executor-service=testExecService
    //     :add(jndi-name=java:jboss/test/testExecService,
    //          core-threads=1,
    //          thread-factory=default)
    public void setup(ManagementClient managementClient, String s) throws Exception {
        ModelNode add = new ModelNode();
        add.get(ClientConstants.OP).set(ClientConstants.ADD);
        add.get(ClientConstants.ADDRESS).add("subsystem", "ee");
        add.get(ClientConstants.ADDRESS).add("managed-executor-service", "testExecService");
        add.get("jndi-name").set("java:jboss/test/testExecService");
        add.get("thread-factory").set("default");
        add.get("core-threads").set("1");
        executeOperation(managementClient, add);
    }

    @Override
    public void tearDown(ManagementClient managementClient, String s) throws Exception {
        ModelNode removeExec = new ModelNode();
        removeExec.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
        removeExec.get(ClientConstants.ADDRESS).add("subsystem", "ee");
        removeExec.get(ClientConstants.ADDRESS).add("managed-executor-service", "testExecService");
        executeOperation(managementClient, removeExec);
    }

    private ModelNode executeOperation(ManagementClient mgmtClient, final ModelNode op) throws IOException {
        ModelNode result = mgmtClient.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assert.fail(Operations.getFailureDescription(result).toString());
        }
        return result;
    }
}
