/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.core.gfac.services.impl;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.airavata.common.utils.LocalEventPublisher;
//import org.apache.airavata.commons.gfac.type.ActualParameter;
//import org.apache.airavata.commons.gfac.type.ApplicationDescription;
//import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.gfac.core.GFacConfiguration;
//import org.apache.airavata.gfac.core.GFacException;
//import org.apache.airavata.gfac.core.context.ApplicationContext;
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.context.MessageContext;
//import org.apache.airavata.gfac.core.provider.GFacProviderException;
//import org.apache.airavata.gfac.local.handler.LocalDirectorySetupHandler;
//import org.apache.airavata.gfac.local.provider.impl.LocalProvider;
//import org.apache.airavata.model.experiment.ExecutionUnit;
//import org.apache.airavata.model.experiment.Experiment;
//import org.apache.airavata.model.experiment.TaskDetails;
//import org.apache.airavata.model.experiment.WorkflowNodeDetails;
//import org.apache.airavata.registry.core.experiment.registry.jpa.impl.LoggingRegistryImpl;
//import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
//import org.apache.airavata.schemas.gfac.InputParameterType;
//import org.apache.airavata.schemas.gfac.OutputParameterType;
//import org.apache.airavata.schemas.gfac.StringParameterType;
//import org.apache.commons.lang.SystemUtils;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import com.google.common.eventbus.EventBus;
//
//public class LocalProviderTest {
//    private JobExecutionContext jobExecutionContext;
//    @BeforeTest
//    public void setUp() throws Exception {
//
//        URL resource = this.getClass().getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
//        File configFile = new File(resource.getPath());
//        GFacConfiguration gFacConfiguration = GFacConfiguration.create(configFile, null);
//        //have to set InFlwo Handlers and outFlowHandlers
//        ApplicationContext applicationContext = new ApplicationContext();
//        HostDescription host = new HostDescription();
//        host.getType().setHostName("localhost");
//        host.getType().setHostAddress("localhost");
//        applicationContext.setHostDescription(host);
//        /*
//           * App
//           */
//        ApplicationDescription appDesc = new ApplicationDescription();
//        ApplicationDeploymentDescriptionType app = appDesc.getType();
//        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//        name.setStringValue("EchoLocal");
//        app.setApplicationName(name);
//
//        /*
//           * Use bat file if it is compiled on Windows
//           */
//        if (SystemUtils.IS_OS_WINDOWS) {
//            URL url = this.getClass().getClassLoader().getResource("echo.bat");
//            app.setExecutableLocation(url.getFile());
//        } else {
//            //for unix and Mac
//            app.setExecutableLocation("/bin/echo");
//        }
//
//        /*
//           * Default tmp location
//           */
//        String tempDir = System.getProperty("java.io.tmpdir");
//        if (tempDir == null) {
//            tempDir = "/tmp";
//        }
//
//        app.setScratchWorkingDirectory(tempDir);
//        app.setStaticWorkingDirectory(tempDir);
//        app.setInputDataDirectory(tempDir + File.separator + "input");
//        app.setOutputDataDirectory(tempDir + File.separator + "output");
//        app.setStandardOutput(tempDir + File.separator + "echo.stdout");
//        app.setStandardError(tempDir + File.separator + "echo.stderr");
//
//        applicationContext.setApplicationDeploymentDescription(appDesc);
//
//        /*
//           * Service
//           */
//        ServiceDescription serv = new ServiceDescription();
//        serv.getType().setName("SimpleEcho");
//
//        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
//        InputParameterType input = InputParameterType.Factory.newInstance();
//        input.setParameterName("echo_input");
//        input.setParameterType(StringParameterType.Factory.newInstance());
//        inputList.add(input);
//        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
//                .size()]);
//
//        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
//        OutputParameterType output = OutputParameterType.Factory.newInstance();
//        output.setParameterName("echo_output");
//        output.setParameterType(StringParameterType.Factory.newInstance());
//        outputList.add(output);
//        OutputParameterType[] outputParamList = outputList
//                .toArray(new OutputParameterType[outputList.size()]);
//
//        serv.getType().setInputParametersArray(inputParamList);
//        serv.getType().setOutputParametersArray(outputParamList);
//
//        jobExecutionContext = new JobExecutionContext(gFacConfiguration, serv.getType().getName());
//        jobExecutionContext.setApplicationContext(applicationContext);
//        /*
//        * Host
//        */
//        applicationContext.setServiceDescription(serv);
//
//        MessageContext inMessage = new MessageContext();
//        ActualParameter echo_input = new ActualParameter();
//        ((StringParameterType) echo_input.getType()).setValue("echo_output=hello");
//        inMessage.addParameter("echo_input", echo_input);
//
//        jobExecutionContext.setInMessageContext(inMessage);
//
//        MessageContext outMessage = new MessageContext();
//        ActualParameter echo_out = new ActualParameter();
//        outMessage.addParameter("echo_output", echo_out);
//
//        jobExecutionContext.setOutMessageContext(outMessage);
//
//        jobExecutionContext.setExperimentID("test123");
//        jobExecutionContext.setExperiment(new Experiment("test123","project1","admin","testExp"));
//        jobExecutionContext.setTaskData(new TaskDetails(jobExecutionContext.getExperimentID()));
//        jobExecutionContext.setRegistry(new LoggingRegistryImpl());
//        jobExecutionContext.setWorkflowNodeDetails(new WorkflowNodeDetails(jobExecutionContext.getExperimentID(),"none", ExecutionUnit.APPLICATION));
//
//
//    }
//
//    @Test
//    public void testLocalDirectorySetupHandler() throws GFacException {
//        LocalDirectorySetupHandler localDirectorySetupHandler = new LocalDirectorySetupHandler();
//        localDirectorySetupHandler.invoke(jobExecutionContext);
//
//        ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
//        ApplicationDeploymentDescriptionType app = applicationDeploymentDescription.getType();
//        junit.framework.Assert.assertTrue(new File(app.getStaticWorkingDirectory()).exists());
//        junit.framework.Assert.assertTrue(new File(app.getScratchWorkingDirectory()).exists());
//        junit.framework.Assert.assertTrue(new File(app.getInputDataDirectory()).exists());
//        junit.framework.Assert.assertTrue(new File(app.getOutputDataDirectory()).exists());
//    }
//
//    @Test
//    public void testLocalProvider() throws GFacException,GFacProviderException {
//        LocalDirectorySetupHandler localDirectorySetupHandler = new LocalDirectorySetupHandler();
//        localDirectorySetupHandler.invoke(jobExecutionContext);
//        LocalProvider localProvider = new LocalProvider();
//        localProvider.setLocalEventPublisher(new LocalEventPublisher(new EventBus()));
//        localProvider.initialize(jobExecutionContext);
//        localProvider.execute(jobExecutionContext);
//        localProvider.dispose(jobExecutionContext);
//    }
//}
