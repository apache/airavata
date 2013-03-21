package org.apache.airavata.core.gfac.services.impl;

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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BESProviderTest extends AbstractBESTest{
	
	private String tmpFilePath;
	
	@Before
	public void initJobContext() throws Exception {
		initTest();
	}

	@Test
	public void submitJob() throws GFacException {
		JobTypeType jobType = JobTypeType.Factory.newInstance();
		jobType.set(JobTypeType.SERIAL);
		ApplicationContext appContext = getApplicationContext();
		appContext.setApplicationDeploymentDescription(getApplicationDesc(jobType));
		jobExecutionContext.setApplicationContext(appContext);
		
		GFacAPI gFacAPI = new GFacAPI();
		gFacAPI.submitJob(jobExecutionContext);
	}
	
	
	protected ApplicationDescription getApplicationDesc(JobTypeType jobType) {
		ApplicationDescription appDesc = new ApplicationDescription(
				HpcApplicationDeploymentType.type);
		HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc
				.getType();
		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
				.newInstance();
		name.setStringValue("CatRemote");
		app.setApplicationName(name);
		ProjectAccountType projectAccountType = app.addNewProjectAccount();
		projectAccountType.setProjectAccountNumber("TG-AST110064");

		app.setCpuCount(1);
		// TODO: also handle parallel jobs
		if((jobType.enumValue() == JobTypeType.SERIAL) || (jobType.enumValue() == JobTypeType.SINGLE)) {
			app.setJobType(JobTypeType.SERIAL);
		}
		else if (jobType.enumValue() == JobTypeType.MPI) {
			app.setJobType(JobTypeType.MPI);
		}
		else {
			app.setJobType(JobTypeType.OPEN_MP);
		}
		
		app.setNodeCount(1);
		app.setProcessorsPerNode(1);

		/*
		 * Use bat file if it is compiled on Windows
		 */
		app.setExecutableLocation("/bin/cat");
		

		/*
		 * Default tmp location
		 */
		String date = (new Date()).toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");

		String remoteTempDir = scratchDir + File.separator + "SimpleCat" + "_" + date + "_"
				+ UUID.randomUUID();

		System.out.println(remoteTempDir);
		
		// no need of these parameters, as unicore manages by itself
		app.setScratchWorkingDirectory(remoteTempDir);
		app.setStaticWorkingDirectory(remoteTempDir);
		app.setInputDataDirectory(remoteTempDir + File.separator + "inputData");
		app.setOutputDataDirectory(remoteTempDir + File.separator + "outputData");
		
		app.setStandardOutput(app.getOutputDataDirectory()+"/jsdl_stdout");
		
		app.setStandardError(app.getOutputDataDirectory()+"/jsdl_stderr");

		return appDesc;
	}

	protected ServiceDescription getServiceDesc() {
		ServiceDescription serv = new ServiceDescription();
		serv.getType().setName("SimpleCat");

		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
		InputParameterType input = InputParameterType.Factory.newInstance();
		input.setParameterName("copy_input");
		input.setParameterType(URIParameterType.Factory.newInstance());
		inputList.add(input);
		InputParameterType[] inputParamList = inputList
				.toArray(new InputParameterType[inputList.size()]);

		List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
		OutputParameterType output = OutputParameterType.Factory.newInstance();
		output.setParameterName("echo_output");
		output.setParameterType(StringParameterType.Factory.newInstance());
		outputList.add(output);
		OutputParameterType[] outputParamList = outputList
				.toArray(new OutputParameterType[outputList.size()]);

		serv.getType().setInputParametersArray(inputParamList);
		serv.getType().setOutputParametersArray(outputParamList);
		return serv;
	}

	protected MessageContext getInMessageContext() {
		
		File tmpFile = new File("target"+File.separator+"tmp-"+new Random().nextInt(5));
		try {
			FileUtils.touch(tmpFile);
			FileUtils.writeStringToFile(tmpFile, "tmp contents", "UTF-8");
			tmpFilePath = tmpFile.getAbsolutePath();
		} catch (Exception e) {
			assertTrue(false);
		}
		
		MessageContext inMessage = new MessageContext();
        
		ActualParameter copy_input = new ActualParameter();
        copy_input.getType().changeType(URIParameterType.type);
        ((URIParameterType)copy_input.getType()).setValue("file:///"+tmpFile.getAbsolutePath());
        inMessage.addParameter("f1", copy_input);
		
    	ActualParameter f2 = new ActualParameter();
        f2.getType().changeType(URIParameterType.type);
        ((URIParameterType)f2.getType()).setValue("http://unicore-dev.zam.kfa-juelich.de/maven/cog-globus/cog-jglobus/1.4/cog-jglobus-1.4.jar");
        inMessage.addParameter("f2", f2);
        
        
        ActualParameter a1 = new ActualParameter();
        a1.getType().changeType(StringParameterType.type);
        ((StringParameterType)a1.getType()).setValue(tmpFile.getName());
        inMessage.addParameter("arg1", a1);

        return inMessage;
	}

	protected MessageContext getOutMessageContext() {
		
		MessageContext outMessage = new MessageContext();
		ActualParameter a1 = new ActualParameter();
		a1.getType().changeType(StringParameterType.type);
		((StringParameterType)a1.getType()).setValue(new File(tmpFilePath).getName());
		outMessage.addParameter("echo_output", a1);
		
		ActualParameter o1 = new ActualParameter();
		o1.getType().changeType(URIParameterType.type);
		// this may be any gridftp / ftp directory
		((URIParameterType)o1.getType()).setValue(gridftpAddress+"/"+remoteTempDir + "/" + "outputData"+"/"+"cog-jglobus-1.4.jar");
		outMessage.addParameter("o1", o1);
		
		return outMessage;
	}
	
	@After
	public void cleanData(){
		FileUtils.deleteQuietly(new File(tmpFilePath));
	}
	
}
