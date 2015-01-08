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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class GFacConfigXmlTest {

    private final static Logger log = LoggerFactory.getLogger(GFacConfigXmlTest.class);
    private BetterGfacImpl gfac;
    @BeforeClass
    public void setUp() throws Exception {
        gfac = new BetterGfacImpl();
    }

    @Test
    public void testGFacConfigWithHost(){
        Assert.assertNotNull(gfac.getGfacConfigFile());
        Assert.assertEquals(1,gfac.getDaemonHandlers().size());
        AiravataUtils.setExecutionAsServer();
        try {
            JobExecutionContext jec = new JobExecutionContext(GFacConfiguration.create(gfac.getGfacConfigFile(), null), "testService");
            ApplicationContext applicationContext = new ApplicationContext();
            ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
            computeResourceDescription.setHostName("trestles.sdsc.xsede.org");
            computeResourceDescription.setResourceDescription("SDSC Trestles Cluster");

            ResourceJobManager resourceJobManager = new ResourceJobManager();
            resourceJobManager.setResourceJobManagerType(ResourceJobManagerType.PBS);
            resourceJobManager.setPushMonitoringEndpoint("push");
            resourceJobManager.setJobManagerBinPath("/opt/torque/bin/");

            SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
            sshJobSubmission.setResourceJobManager(resourceJobManager);
            sshJobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
            sshJobSubmission.setSshPort(22);
            sshJobSubmission.setResourceJobManager(resourceJobManager);

            JobSubmissionInterface submissionInterface = new JobSubmissionInterface();
            submissionInterface.setJobSubmissionInterfaceId("testSubmissionId");
            submissionInterface.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
            submissionInterface.setPriorityOrder(0);

            computeResourceDescription.addToJobSubmissionInterfaces(submissionInterface);

            applicationContext.setComputeResourceDescription(computeResourceDescription);
            jec.setApplicationContext(applicationContext);
//            Scheduler.schedule(jec);
//            Assert.assertEquals(ExecutionMode.ASYNCHRONOUS, jec.getGFacConfiguration().getExecutionMode());
//            Assert.assertEquals("org.apache.airavata.job.TestProvider", jec.getProvider().getClass().getName());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (XPathExpressionException e) {
            log.error(e.getMessage(), e);
        }
    }
    @Test
        public void testAppSpecificConfig() {
        Assert.assertNotNull(gfac.getGfacConfigFile());
        Assert.assertEquals(1, gfac.getDaemonHandlers().size());
        AiravataUtils.setExecutionAsServer();
        try {
            JobExecutionContext jec = new JobExecutionContext(GFacConfiguration.create(gfac.getGfacConfigFile(), null), "UltraScan");
            ApplicationContext applicationContext = new ApplicationContext();
            ComputeResourceDescription computeResourceDescription = new ComputeResourceDescription();
            computeResourceDescription.setHostName("trestles.sdsc.xsede.org");
            computeResourceDescription.setResourceDescription("SDSC Trestles Cluster");

            ResourceJobManager resourceJobManager = new ResourceJobManager();
            resourceJobManager.setResourceJobManagerType(ResourceJobManagerType.PBS);
            resourceJobManager.setPushMonitoringEndpoint("push");
            resourceJobManager.setJobManagerBinPath("/opt/torque/bin/");

            SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
            sshJobSubmission.setResourceJobManager(resourceJobManager);
            sshJobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
            sshJobSubmission.setSshPort(22);
            sshJobSubmission.setResourceJobManager(resourceJobManager);

            JobSubmissionInterface submissionInterface = new JobSubmissionInterface();
            submissionInterface.setJobSubmissionInterfaceId("testSubmissionId");
            submissionInterface.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
            submissionInterface.setPriorityOrder(0);

            computeResourceDescription.addToJobSubmissionInterfaces(submissionInterface);

            applicationContext.setComputeResourceDescription(computeResourceDescription);
            jec.setApplicationContext(applicationContext);
//            Scheduler.schedule(jec);
//            Assert.assertEquals(3, jec.getGFacConfiguration().getInHandlers().size());
//            Assert.assertEquals(1, jec.getGFacConfiguration().getInHandlers().get(0).getProperties().size());
//            Assert.assertEquals(0, jec.getGFacConfiguration().getInHandlers().get(1).getProperties().size());
//            Assert.assertEquals(1, jec.getGFacConfiguration().getInHandlers().get(2).getProperties().size());
//            Assert.assertEquals(ExecutionMode.ASYNCHRONOUS, jec.getGFacConfiguration().getExecutionMode());// todo this logic might be wrong
//            Assert.assertEquals("org.apache.airavata.job.TestProvider", jec.getProvider().getClass().getName());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (XPathExpressionException e) {
            log.error(e.getMessage(), e);
        }
    }


}
