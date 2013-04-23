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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.handler.GFacHandlerConfig;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.junit.Before;
import org.junit.Test;

public class ParamChemTest {
    private JobExecutionContext jobExecutionContext;

    @Before
    public void setUp() throws Exception {

        GFacConfiguration gFacConfiguration = new GFacConfiguration(null);
        GSISecurityContext context = new GSISecurityContext();
		context.setMyproxyLifetime(3600);
		context.setMyproxyServer("myproxy.teragrid.org");
		context.setMyproxyUserName("*****");
		context.setMyproxyPasswd("*****");
		context.setTrustedCertLoc("./certificates");

        //have to set InFlwo Handlers and outFlowHandlers
        gFacConfiguration.setInHandlers(Arrays.asList(new GFacHandlerConfig[]{new GFacHandlerConfig(null,"org.apache.airavata.gfac.handler.GramDirectorySetupHandler"), new GFacHandlerConfig(null,"org.apache.airavata.gfac.handler.GridFTPInputHandler")}));
        gFacConfiguration.setOutHandlers(Arrays.asList(new GFacHandlerConfig[] {new GFacHandlerConfig(null,"org.apache.airavata.gfac.handler.GridFTPOutputHandler")}));
        /*
        * Host
        */
        String serviceName = "Prepare_Model_Reference_Data";
        HostDescription host = new HostDescription(GlobusHostType.type);
        host.getType().setHostName("trestles");
        host.getType().setHostAddress("trestles.sdsc.edu");
        ((GlobusHostType) host.getType()).addGridFTPEndPoint("gsiftp://trestles-dm.sdsc.edu:2811");
        ((GlobusHostType) host.getType()).addGlobusGateKeeperEndPoint("trestles-login2.sdsc.edu:2119/jobmanager-pbstest2");

        /*
        * App
        */
        ApplicationDescription appDesc =
                new ApplicationDescription(HpcApplicationDeploymentType.type);
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType
                = appDesc.getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue(serviceName);
        String tempDir = "/oasis/projects/nsf/uic151/gridchem/airavata-workdirs";
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + serviceName + "_" + date + "_" + UUID.randomUUID();
        applicationDeploymentDescriptionType.setExecutableLocation("/home/gridchem/workflow_script/sys_exec/scripts/step1/step1_model_refdata_prep.sh");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory(tempDir);
        applicationDeploymentDescriptionType.setStaticWorkingDirectory(tempDir);
        applicationDeploymentDescriptionType.setInputDataDirectory(tempDir + File.separator + "inputData");
        applicationDeploymentDescriptionType.setOutputDataDirectory(tempDir + File.separator + "outputData");
        applicationDeploymentDescriptionType.setStandardOutput(tempDir + File.separator + applicationDeploymentDescriptionType.getApplicationName().getStringValue() + ".stdout");
        applicationDeploymentDescriptionType.setStandardError(tempDir + File.separator + applicationDeploymentDescriptionType.getApplicationName().getStringValue() + ".stderr");

        ProjectAccountType projectAccountType = ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("uic151");

        QueueType queueType = ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).addNewQueue();
        queueType.setQueueName("shared");

        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setJobType(JobTypeType.SERIAL);
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setMaxWallTime(30);
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setMaxMemory(2000);
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setCpuCount(1);
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setNodeCount(1);
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setProcessorsPerNode(1);


        /*
        * Service
        */
        ServiceDescription serv = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();

        serv.getType().setName(serviceName);
        serv.getType().setDescription(serviceName);

        //Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("molecule_id");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("geom_mol2");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("toppar_main_tgz");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("toppar_usr_tgz");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("toppar_mol_str");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("molecule_dir_in_tgz");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("GC_UserName");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("GC_ProjectName");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("GC_WorkflowName");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        //Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("opt_freq_input_gjf");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.URI);
        outputParaType.setName("URI");
        outputParameters.add(outputParameter);

        outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("charmm_miminized_crd");
        outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.URI);
        outputParaType.setName("URI");
        outputParameters.add(outputParameter);

        outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("step1_log");
        outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.URI);
        outputParaType.setName("URI");
        outputParameters.add(outputParameter);

        outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("molecule_dir_out_tgz");
        outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.URI);
        outputParaType.setName("URI");
        outputParameters.add(outputParameter);

        outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("gcvars");
        outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.URI);
        outputParaType.setName("URI");
        outputParameters.add(outputParameter);

        //Setting input and output parameters to serviceDescriptor
        serv.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serv.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        jobExecutionContext = new JobExecutionContext(gFacConfiguration,serv.getType().getName());
        jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, context);
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setHostDescription(host);
        applicationContext.setApplicationDeploymentDescription(appDesc);
        jobExecutionContext.setApplicationContext(applicationContext);
        applicationContext.setServiceDescription(serv);

        MessageContext inMessage = new MessageContext();

        ActualParameter echo_input = new ActualParameter();
        ((StringParameterType) echo_input.getType()).setValue("ai");
        inMessage.addParameter("molecule_id", echo_input);

        ActualParameter geom_mol2 = new ActualParameter(URIParameterType.type);
        ((URIParameterType) geom_mol2.getType()).setValue("http://ccg-mw1.ncsa.uiuc.edu/cgenff/leoshen/cgenff_project/ai/ai.mol2");
        inMessage.addParameter("geom_mol2", geom_mol2);

        ActualParameter toppar_main_tgz = new ActualParameter(URIParameterType.type);
        ((URIParameterType) toppar_main_tgz.getType()).setValue("/home/gridchem/workflow_script/toppar/cgenff/releases/2b7/main.tgz");
        inMessage.addParameter("toppar_main_tgz", toppar_main_tgz);

        ActualParameter toppar_usr_tgz = new ActualParameter(URIParameterType.type);
        ((URIParameterType) toppar_usr_tgz.getType()).setValue("gsiftp://trestles.sdsc.edu");
        inMessage.addParameter("toppar_usr_tgz", toppar_usr_tgz);

        ActualParameter toppar_mol_str = new ActualParameter(URIParameterType.type);
        ((URIParameterType) toppar_mol_str.getType()).setValue("http://ccg-mw1.ncsa.uiuc.edu/cgenff/leoshen/cgenff_project/ai/toppar/ai.str");
        inMessage.addParameter("toppar_mol_str", toppar_mol_str);

        ActualParameter molecule_dir_in_tgz = new ActualParameter(URIParameterType.type);
        ((URIParameterType) molecule_dir_in_tgz.getType()).setValue("");
        inMessage.addParameter("molecule_dir_in_tgz", molecule_dir_in_tgz);

        ActualParameter GC_UserName = new ActualParameter();
        ((StringParameterType) GC_UserName.getType()).setValue("leoshen");
        inMessage.addParameter("GC_UserName", GC_UserName);

        ActualParameter GC_ProjectName = new ActualParameter();
        ((StringParameterType) GC_ProjectName.getType()).setValue("leoshen");
        inMessage.addParameter("GC_ProjectName", GC_ProjectName);

        ActualParameter GC_WorkflowName = new ActualParameter();
        ((StringParameterType) GC_WorkflowName.getType()).setValue("ai__1339258840");
        inMessage.addParameter("GC_WorkflowName", GC_WorkflowName);

        jobExecutionContext.setInMessageContext(inMessage);

        MessageContext outMessage = new MessageContext();

        ActualParameter opt_freq_input_gjf = new ActualParameter(URIParameterType.type);
        outMessage.addParameter("opt_freq_input_gjf", opt_freq_input_gjf);

        ActualParameter charmm_miminized_crd = new ActualParameter(URIParameterType.type);
        outMessage.addParameter("charmm_miminized_crd", charmm_miminized_crd);

        ActualParameter step1_log = new ActualParameter(URIParameterType.type);
        outMessage.addParameter("step1_log", step1_log);

        ActualParameter molecule_dir_out_tgz = new ActualParameter(URIParameterType.type);
        outMessage.addParameter("molecule_dir_out_tgz", molecule_dir_out_tgz);

        ActualParameter gcvars = new ActualParameter(URIParameterType.type);
        outMessage.addParameter("gcvars", gcvars);

        jobExecutionContext.setOutMessageContext(outMessage);

    }

    @Test
    public void testGramProvider() throws GFacException {
//        GFacAPI gFacAPI = new GFacAPI();
//        gFacAPI.submitJob(jobExecutionContext);
//        MessageContext outMessageContext = jobExecutionContext.getOutMessageContext();
//        Assert.assertFalse(outMessageContext.getParameters().isEmpty());
//        Assert.assertEquals(MappingFactory.toString((ActualParameter) outMessageContext.getParameter("echo_output")), "hello");
    }
}
