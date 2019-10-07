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

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 @author Peter Mackay
 @EATSERVERS : Wildfly,Eap72x,Eap72x-Proposed
 @EATSERVERMODULE : concurrency
 @EATLOWERLIMITVERSIONS : 18.0.0.Final, 7.2.5, 7.2.5
 */
@WebServlet(TestServlet.URL_PATTERN)
public class TestServlet extends HttpServlet {

    public static final String URL_PATTERN = "/test";

    @Resource(lookup = "java:jboss/test/testExecService")
    private ManagedExecutorService executorService;

    @Inject
    private PollMembersTask task;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Future<Integer> submit = executorService.submit(task);
        Integer integer = null;
        try {
            integer = submit.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new ServletException(e);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        PrintWriter writer = resp.getWriter();
        writer.print(integer);
        writer.close();
    }
}