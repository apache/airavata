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

package org.apache.airavata.gfac.bes.utils;

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.FileNameType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.UserNameType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.NumberOfProcessesType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.ProcessesPerHostType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.ThreadsPerProcessType;


public class ApplicationProcessor {
	
	public static void generateJobSpecificAppElements(JobDefinitionType value, JobExecutionContext context){
		
		String userName = getUserNameFromContext(context);
		if (userName.equalsIgnoreCase("admin")){
			userName = "CN=zdv575, O=Ultrascan Gateway, C=DE";
		}
		
		ApplicationDeploymentDescription appDep= context.getApplicationContext().getApplicationDeploymentDescription();
        String appname = context.getApplicationContext().getApplicationInterfaceDescription().getApplicationName();
        ApplicationParallelismType parallelism = appDep.getParallelism();
        ApplicationType appType = JSDLUtils.getOrCreateApplication(value);
        appType.setApplicationName(appname);
        

//		if (appDep.getSetEnvironment().size() > 0) {
//            createApplicationEnvironment(value, appDep.getSetEnvironment(), parallelism);
//		}
//
        String stdout = context.getStandardOutput();
        String stderr = context.getStandardError();
        
        if (appDep.getExecutablePath() != null) {
			FileNameType fNameType = FileNameType.Factory.newInstance();
			fNameType.setStringValue(appDep.getExecutablePath());
			if(isParallelJob(context)) {
				JSDLUtils.getOrCreateSPMDApplication(value).setExecutable(fNameType);
                if (parallelism.equals(ApplicationParallelismType.OPENMP_MPI)){
                    JSDLUtils.getSPMDApplication(value).setSPMDVariation(SPMDVariations.OpenMPI.value());
                }else if (parallelism.equals(ApplicationParallelismType.MPI)){
                    JSDLUtils.getSPMDApplication(value).setSPMDVariation(SPMDVariations.MPI.value());
                }

                int totalCPUCount = context.getTaskData().getTaskScheduling().getTotalCPUCount();
                if(totalCPUCount > 0){
					NumberOfProcessesType num = NumberOfProcessesType.Factory.newInstance();
                    num.setStringValue(String.valueOf(totalCPUCount));
					JSDLUtils.getSPMDApplication(value).setNumberOfProcesses(num);
				}

                int totalNodeCount = context.getTaskData().getTaskScheduling().getNodeCount();
                if(totalNodeCount > 0){
                    int ppn = totalCPUCount / totalNodeCount;
                    ProcessesPerHostType pph = ProcessesPerHostType.Factory.newInstance();
                    pph.setStringValue(String.valueOf(ppn));
                    JSDLUtils.getSPMDApplication(value).setProcessesPerHost(pph);
                }

                int totalThreadCount = context.getTaskData().getTaskScheduling().getNumberOfThreads();

                if(totalThreadCount > 0){
					ThreadsPerProcessType tpp = ThreadsPerProcessType.Factory.newInstance();
					tpp.setStringValue(String.valueOf(totalThreadCount));
					JSDLUtils.getSPMDApplication(value).setThreadsPerProcess(tpp);
				}
				
				if(userName != null) {
					UserNameType userNameType = UserNameType.Factory.newInstance();
					userNameType.setStringValue(userName);
					JSDLUtils.getSPMDApplication(value).setUserName(userNameType);
				}
                if (stdout != null){
                    FileNameType fName = FileNameType.Factory.newInstance();
                    fName.setStringValue(stdout);
                    JSDLUtils.getOrCreateSPMDApplication(value).setOutput(fName);
                }
                if (stderr != null){
                    FileNameType fName = FileNameType.Factory.newInstance();
                    fName.setStringValue(stderr);
                    JSDLUtils.getOrCreateSPMDApplication(value).setError(fName);
                }


			}
			else {
				JSDLUtils.getOrCreatePOSIXApplication(value).setExecutable(fNameType);
				if(userName != null) {
					UserNameType userNameType = UserNameType.Factory.newInstance();
					userNameType.setStringValue(userName);
					JSDLUtils.getOrCreatePOSIXApplication(value).setUserName(userNameType);
				}
                if (stdout != null){
                    FileNameType fName = FileNameType.Factory.newInstance();
                    fName.setStringValue(stdout);
                    JSDLUtils.getOrCreatePOSIXApplication(value).setOutput(fName);
                }
                if (stderr != null){
                    FileNameType fName = FileNameType.Factory.newInstance();
                    fName.setStringValue(stderr);
                    JSDLUtils.getOrCreatePOSIXApplication(value).setError(fName);
                }
			}
		}
	}
	
	public static String getUserNameFromContext(JobExecutionContext jobContext) {
		if(jobContext.getTaskData() == null)
			return null;
		//TODO: Extend unicore model to specify optional unix user id (allocation account) 
		return "admin";
	}

	public static void addApplicationArgument(JobDefinitionType value, JobExecutionContext context, String stringPrm) {
		if(isParallelJob(context)){ 		
			JSDLUtils.getOrCreateSPMDApplication(value).addNewArgument().setStringValue(stringPrm);
		}
		else { 
		    JSDLUtils.getOrCreatePOSIXApplication(value).addNewArgument().setStringValue(stringPrm);
		}
	}
	
	public static String getApplicationStdOut(JobDefinitionType value, JobExecutionContext context) throws RuntimeException {
		if (isParallelJob(context)) return JSDLUtils.getOrCreateSPMDApplication(value).getOutput().getStringValue();
		else return JSDLUtils.getOrCreatePOSIXApplication(value).getOutput().getStringValue();
	}
	
	public static String getApplicationStdErr(JobDefinitionType value, JobExecutionContext context) throws RuntimeException {
		if (isParallelJob(context)) return JSDLUtils.getOrCreateSPMDApplication(value).getError().getStringValue();
		else return JSDLUtils.getOrCreatePOSIXApplication(value).getError().getStringValue();
	}
	
	public static void createGenericApplication(JobDefinitionType value, String appName) {
        ApplicationType appType = JSDLUtils.getOrCreateApplication(value);
        appType.setApplicationName(appName);
    }
	
	public static boolean isParallelJob(JobExecutionContext context) {
		
		ApplicationDeploymentDescription appDep = context.getApplicationContext().getApplicationDeploymentDescription();
		ApplicationParallelismType parallelism = appDep.getParallelism();
		
		boolean isParallel = false;
		
		if(parallelism.equals(ApplicationParallelismType.MPI) || 
		   parallelism.equals(ApplicationParallelismType.OPENMP_MPI) || 
		   parallelism.equals(ApplicationParallelismType.OPENMP	)) {
			isParallel = true;
		}
		
		return isParallel;
	}
	

}
