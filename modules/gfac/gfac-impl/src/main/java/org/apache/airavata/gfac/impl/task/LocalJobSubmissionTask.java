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

package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.local.utils.InputStreamToFileWriter;
import org.apache.airavata.gfac.local.utils.InputUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LocalJobSubmissionTask implements JobSubmissionTask{
    private static final Logger log = LoggerFactory.getLogger(LocalJobSubmissionTask.class);
    private ProcessBuilder builder;

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {
    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
     /*   try {
            ProcessContext processContext = taskContext.getParentProcessContext();
            // build command with all inputs
            List<String> cmdList = buildCommand(processContext);
            initProcessBuilder(processContext.getApplicationDeploymentDescription(), cmdList);

            // extra environment variables
            builder.environment().put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, processContext.getInputDir());
            builder.environment().put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, processContext.getOutputDir());

            // set working directory
            builder.directory(new File(processContext.getWorkingDir()));

            // log info
            log.info("Command = " + InputUtils.buildCommand(cmdList));
            log.info("Working dir = " + builder.directory());
            JobModel jobModel = processContext.getJobModel();
            if (jobModel == null) {
                jobModel = new JobModel();
            }
            String jobId = taskContext.getTaskModel().getTaskId();
            jobModel.setJobId(jobId);
            jobModel.setJobDescription("sample local job");
            processContext.setJobModel(jobModel);
            GFacUtils.saveJobStatus(taskContext, jobModel, JobState.SUBMITTED);
            // running cmd
            Process process = builder.start();

            Thread standardOutWriter = new InputStreamToFileWriter(process.getInputStream(), processContext.getStdoutLocation());
            Thread standardErrorWriter = new InputStreamToFileWriter(process.getErrorStream(), processContext.getStderrLocation());

            // start output threads
            standardOutWriter.setDaemon(true);
            standardErrorWriter.setDaemon(true);
            standardOutWriter.start();
            standardErrorWriter.start();

            int returnValue = process.waitFor();

            // make sure other two threads are done
            standardOutWriter.join();
            standardErrorWriter.join();

            *//*
             * check return value. usually not very helpful to draw conclusions based on return values so don't bother.
             * just provide warning in the log messages
             *//*
            if (returnValue != 0) {
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
                log.info("Process finished with return value of zero.");
            }

            StringBuffer buf = new StringBuffer();
            buf.append("Executed ").append(InputUtils.buildCommand(cmdList))
                    .append(" on the localHost, working directory = ").append(processContext.getWorkingDir())
                    .append(" tempDirectory = ").append(processContext.getWorkingDir()).append(" With the status ")
                    .append(String.valueOf(returnValue));

            log.info(buf.toString());
            GFacUtils.saveJobStatus(taskContext, jobModel, JobState.COMPLETE);
        } catch (GFacException e) {
            log.error("Error while submitting local job", e);
            throw new TaskException("Error while submitting local job", e);
        } catch (InterruptedException e) {
            log.error("Error while submitting local job", e);
            throw new TaskException("Error while submitting local job", e);
        } catch (IOException e) {
            log.error("Error while submitting local job", e);
            throw new TaskException("Error while submitting local job", e);
        }*/
	    return new TaskStatus(TaskState.COMPLETED);
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        return null;
    }

    private List<String> buildCommand(ProcessContext processContext) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(processContext.getApplicationDeploymentDescription().getExecutablePath());
        List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();

        // sort the inputs first and then build the command List
        Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
            @Override
            public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                return inputDataObjectType.getInputOrder() - t1.getInputOrder();
            }
        };
        Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
        for (InputDataObjectType input : processInputs) {
                sortedInputSet.add(input);
        }
        for (InputDataObjectType inputDataObjectType : sortedInputSet) {
            if (inputDataObjectType.getApplicationArgument() != null
                    && !inputDataObjectType.getApplicationArgument().equals("")) {
                cmdList.add(inputDataObjectType.getApplicationArgument());
            }

            if (inputDataObjectType.getValue() != null
                    && !inputDataObjectType.getValue().equals("")) {
                cmdList.add(inputDataObjectType.getValue());
            }
        }
        return cmdList;
    }

    private void initProcessBuilder(ApplicationDeploymentDescription app, List<String> cmdList){
        builder = new ProcessBuilder(cmdList);

        List<SetEnvPaths> setEnvironment = app.getSetEnvironment();
        if (setEnvironment != null) {
            for (SetEnvPaths envPath : setEnvironment) {
                Map<String,String> builderEnv = builder.environment();
                builderEnv.put(envPath.getName(), envPath.getValue());
            }
        }
    }

	@Override
	public TaskTypes getType() {
		return TaskTypes.JOB_SUBMISSION;
	}

	@Override
	public JobStatus cancel(TaskContext taskcontext) {
		// TODO - implement Local Job cancel
		return null;
	}
}
