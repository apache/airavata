/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.core.gfac.services.impl;

import static org.junit.Assert.fail;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.ExecutionContextImpl;
import org.apache.airavata.core.gfac.context.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.context.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.notification.DummyNotification;
import org.apache.airavata.core.gfac.type.parameter.StringParameter;
import org.junit.Assert;
import org.junit.Test;

public class PropertiesBasedServiceImplTest {

    @Test
    public void testInit() {
        try {
            InvocationContext ct = new InvocationContext();
            ct.setExecutionContext(new ExecutionContextImpl());

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }

    @Test
    public void testExecute() {
        try {
            InvocationContext ct = new InvocationContext();
            ct.setExecutionContext(new ExecutionContextImpl());

            ct.getExecutionContext().setNotificationService(new DummyNotification());

            GSISecurityContext gsiSecurityContext = new GSISecurityContext();
            gsiSecurityContext.setMyproxyServer("myproxy.teragrid.org");
            gsiSecurityContext.setMyproxyUserName("ogce");
            gsiSecurityContext.setMyproxyPasswd("Jdas7wph");
            gsiSecurityContext.setMyproxyLifetime(14400);
            ct.addSecurityContext("myproxy", gsiSecurityContext);

            ct.setServiceName("{http://www.extreme.indiana.edu/namespaces/2004/01/gFac}Echo_Service");

            // parameter
            ParameterContextImpl x = new ParameterContextImpl();
            StringParameter parameter = new StringParameter();
            parameter.parseStringVal("Hello");
            x.addParameter("echo", parameter);
            ct.addMessageContext("input", x);

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.init();
            service.execute(ct);

            Assert.assertNotNull(ct.getMessageContext("output"));
            Assert.assertNotNull(ct.getMessageContext("output").getParameterValue("Echoed_Output"));
            Assert.assertEquals("\"Hello\"", ct.getMessageContext("output").getParameterValue("Echoed_Output")
                    .toString());

        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }

    @Test
    public void testDispose() {
        try {
            InvocationContext ct = new InvocationContext();
            ct.setExecutionContext(new ExecutionContextImpl());

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }

}
