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
package org.apache.airavata.job;

import junit.framework.Assert;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.Scheduler;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.GFacImpl;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class GFacConfigXmlTest {

    private GFacImpl gfac;
    @BeforeClass
    public void setUp() throws Exception {
        gfac = new GFacImpl();
    }

    @Test
    public void testGFacConfigWithHost(){
        Assert.assertNotNull(gfac.getGfacConfigFile());
        Assert.assertEquals(1,gfac.getDaemonHandlers().size());
        try {
            JobExecutionContext jec = new JobExecutionContext(GFacConfiguration.create(gfac.getGfacConfigFile(), null, null), "testService");
            ApplicationContext applicationContext = new ApplicationContext();
            HostDescription host = new HostDescription(GsisshHostType.type);
            host.getType().setHostAddress("trestles.sdsc.edu");
            host.getType().setHostName("trestles");
            ((GsisshHostType) host.getType()).setPort(22);
            ((GsisshHostType) host.getType()).setInstalledPath("/opt/torque/bin/");
            applicationContext.setHostDescription(host);
            jec.setApplicationContext(applicationContext);
            Scheduler.schedule(jec);
            Assert.assertEquals(ExecutionMode.ASYNCHRONOUS, jec.getGFacConfiguration().getExecutionMode());
            Assert.assertEquals("org.apache.airavata.job.TestProvider", jec.getProvider().getClass().getName());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XPathExpressionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GFacException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @Test
        public void testAppSpecificConfig(){
            Assert.assertNotNull(gfac.getGfacConfigFile());
            Assert.assertEquals(1,gfac.getDaemonHandlers().size());
            try {
                JobExecutionContext jec = new JobExecutionContext(GFacConfiguration.create(gfac.getGfacConfigFile(), null, null), "UltraScan");
                ApplicationContext applicationContext = new ApplicationContext();
                HostDescription host = new HostDescription(GsisshHostType.type);
                host.getType().setHostAddress("trestles.sdsc.edu");
                host.getType().setHostName("trestles");
                ((GsisshHostType) host.getType()).setPort(22);
                ((GsisshHostType) host.getType()).setInstalledPath("/opt/torque/bin/");
                applicationContext.setHostDescription(host);
                jec.setApplicationContext(applicationContext);
                Scheduler.schedule(jec);
                Assert.assertEquals(3, jec.getGFacConfiguration().getInHandlers().size());
                Assert.assertEquals(1, jec.getGFacConfiguration().getInHandlers().get(0).getProperties().size());
                Assert.assertEquals(0, jec.getGFacConfiguration().getInHandlers().get(1).getProperties().size());
                Assert.assertEquals(1,jec.getGFacConfiguration().getInHandlers().get(2).getProperties().size());
                Assert.assertEquals(ExecutionMode.ASYNCHRONOUS, jec.getGFacConfiguration().getExecutionMode());// todo this logic might be wrong
                Assert.assertEquals("org.apache.airavata.job.TestProvider", jec.getProvider().getClass().getName());
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (XPathExpressionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (GFacException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


}
