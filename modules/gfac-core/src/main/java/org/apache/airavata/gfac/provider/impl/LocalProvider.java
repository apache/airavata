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
package org.apache.airavata.gfac.provider.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.notification.events.StartExecutionEvent;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.provider.utils.ProviderUtils;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.InputStreamToFileWriter;
import org.apache.airavata.gfac.utils.InputUtils;
import org.apache.airavata.gfac.utils.OutputUtils;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalProvider implements GFacProvider {
    private static final Logger log = LoggerFactory.getLogger(LocalProvider.class);
    private ProcessBuilder builder;
    private List<String> cmdList;
    private String jobId;
    
    public static class LocalProviderJobData{
    	private String applicationName;
    	private List<String> inputParameters;
    	private String workingDir;
    	private String inputDir;
    	private String outputDir;
		public String getApplicationName() {
			return applicationName;
		}
		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}
		public List<String> getInputParameters() {
			return inputParameters;
		}
		public void setInputParameters(List<String> inputParameters) {
			this.inputParameters = inputParameters;
		}
		public String getWorkingDir() {
			return workingDir;
		}
		public void setWorkingDir(String workingDir) {
			this.workingDir = workingDir;
		}
		public String getInputDir() {
			return inputDir;
		}
		public void setInputDir(String inputDir) {
			this.inputDir = inputDir;
		}
		public String getOutputDir() {
			return outputDir;
		}
		public void setOutputDir(String outputDir) {
			this.outputDir = outputDir;
		}
    }
    public LocalProvider(){
        cmdList = new ArrayList<String>();
    }

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().
                getApplicationDeploymentDescription().getType();

        buildCommand(app.getExecutableLocation(), ProviderUtils.getInputParameters(jobExecutionContext));
        initProcessBuilder(app);

        // extra environment variables
        builder.environment().put(Constants.INPUT_DATA_DIR_VAR_NAME, app.getInputDataDirectory());
        builder.environment().put(Constants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDataDirectory());

        // set working directory
        builder.directory(new File(app.getStaticWorkingDirectory()));

        // log info
        log.info("Command = " + InputUtils.buildCommand(cmdList));
        log.info("Working dir = " + builder.directory());
        for (String key : builder.environment().keySet()) {
            log.info("Env[" + key + "] = " + builder.environment().get(key));
        }
    }

    public void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        jobExecutionContext.getNotifier().publish(new StartExecutionEvent());
         ApplicationDeploymentDescriptionType app = jobExecutionContext.
                 getApplicationContext().getApplicationDeploymentDescription().getType();

        try {
            // running cmd
            Process process = builder.start();
            jobId="Local_"+Calendar.getInstance().getTimeInMillis();
            if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
        		ApplicationJob appJob = GFacUtils.createApplicationJob(jobExecutionContext);
                appJob.setJobId(jobId);
                LocalProviderJobData data = new LocalProviderJobData();
                data.setApplicationName(app.getExecutableLocation());
                data.setInputDir(app.getInputDataDirectory());
                data.setOutputDir(app.getOutputDataDirectory());
                data.setWorkingDir(builder.directory().toString());
                data.setInputParameters(ProviderUtils.getInputParameters(jobExecutionContext));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                JAXB.marshal(data, stream);
                appJob.setJobData(stream.toString());
                appJob.setSubmittedTime(Calendar.getInstance().getTime());
                appJob.setJobStatus(ApplicationJobStatus.SUBMITTED);
                appJob.setStatusUpdateTime(appJob.getSubmittedTime());
                try {
					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().addApplicationJob(appJob);
				} catch (AiravataAPIInvocationException e) {
					e.printStackTrace();
				}
        	}
            if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
                try {
					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateApplicationJobStatus(jobId, ApplicationJobStatus.INITIALIZE, Calendar.getInstance().getTime());
				} catch (AiravataAPIInvocationException e) {
					e.printStackTrace();
				}
        	}
            Thread standardOutWriter = new InputStreamToFileWriter(process.getInputStream(), app.getStandardOutput());
            Thread standardErrorWriter = new InputStreamToFileWriter(process.getErrorStream(), app.getStandardError());

            // start output threads
            standardOutWriter.setDaemon(true);
            standardErrorWriter.setDaemon(true);
            standardOutWriter.start();
            standardErrorWriter.start();
            if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
                try {
					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateApplicationJobStatus(jobId, ApplicationJobStatus.EXECUTING, Calendar.getInstance().getTime());
				} catch (AiravataAPIInvocationException e) {
					e.printStackTrace();
				}
        	}
            // wait for the process (application) to finish executing
            int returnValue = process.waitFor();

        	if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
                try {
					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateApplicationJobStatus(jobId, ApplicationJobStatus.FINALIZE, Calendar.getInstance().getTime());
				} catch (AiravataAPIInvocationException e) {
					e.printStackTrace();
				}
        	}

            // make sure other two threads are done
            standardOutWriter.join();
            standardErrorWriter.join();

            /*
             * check return value. usually not very helpful to draw conclusions based on return values so don't bother.
             * just provide warning in the log messages
             */
            if (returnValue != 0) {
            	if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
                    try {
    					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateApplicationJobStatus(jobId, ApplicationJobStatus.FAILED, Calendar.getInstance().getTime());
    				} catch (AiravataAPIInvocationException e) {
    					e.printStackTrace();
    				}
            	}
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
            	if(jobExecutionContext.getGFacConfiguration().getAiravataAPI() != null){
                    try {
    					jobExecutionContext.getGFacConfiguration().getAiravataAPI().getProvenanceManager().updateApplicationJobStatus(jobId, ApplicationJobStatus.FINISHED, Calendar.getInstance().getTime());
    				} catch (AiravataAPIInvocationException e) {
    					e.printStackTrace();
    				}
            	}
                log.info("Process finished with return value of zero.");
            }

            StringBuffer buf = new StringBuffer();
            buf.append("Executed ").append(InputUtils.buildCommand(cmdList))
                    .append(" on the localHost, working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" tempDirectory = ").append(app.getScratchWorkingDirectory()).append(" With the status ")
                    .append(String.valueOf(returnValue));

            log.info(buf.toString());

        } catch (IOException io) {
            throw new GFacProviderException(io.getMessage(), io,jobExecutionContext);
        } catch (InterruptedException e) {
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        }
    }

    public void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();

        try {
            String stdOutStr = GFacUtils.readFileToString(app.getStandardOutput());
            String stdErrStr = GFacUtils.readFileToString(app.getStandardError());
			Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr);
        } catch (XmlException e) {
            throw new GFacProviderException("Cannot read output:" + e.getMessage(), e, jobExecutionContext);
        } catch (IOException io) {
            throw new GFacProviderException(io.getMessage(), io, jobExecutionContext);
        } catch (Exception e){
        	throw new GFacProviderException("Error in retrieving results",e,jobExecutionContext);
        }
    }

    private void buildCommand(String executable, List<String> inputParameterList){
        cmdList.add(executable);
        cmdList.addAll(inputParameterList);
    }

    private void initProcessBuilder(ApplicationDeploymentDescriptionType app){
        builder = new ProcessBuilder(cmdList);

        NameValuePairType[] env = app.getApplicationEnvironmentArray();

        if(env != null && env.length > 0){
            Map<String,String> builderEnv = builder.environment();
            for (NameValuePairType entry : env) {
                builderEnv.put(entry.getName(), entry.getValue());
            }
        }
    }

    public void initProperties(Map<String, String> properties) throws GFacProviderException, GFacException {

    }
}
