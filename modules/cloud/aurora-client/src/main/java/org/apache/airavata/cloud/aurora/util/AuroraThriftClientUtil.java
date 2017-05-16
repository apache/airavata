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
package org.apache.airavata.cloud.aurora.util;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.airavata.cloud.aurora.client.AuroraSchedulerClientFactory;
import org.apache.airavata.cloud.aurora.client.bean.GetJobsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.IdentityBean;
import org.apache.airavata.cloud.aurora.client.bean.JobConfigBean;
import org.apache.airavata.cloud.aurora.client.bean.JobDetailsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.JobKeyBean;
import org.apache.airavata.cloud.aurora.client.bean.PendingJobReasonBean;
import org.apache.airavata.cloud.aurora.client.bean.ProcessBean;
import org.apache.airavata.cloud.aurora.client.bean.ResourceBean;
import org.apache.airavata.cloud.aurora.client.bean.ResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.ServerInfoBean;
import org.apache.airavata.cloud.aurora.client.sdk.ExecutorConfig;
import org.apache.airavata.cloud.aurora.client.sdk.Identity;
import org.apache.airavata.cloud.aurora.client.sdk.JobConfiguration;
import org.apache.airavata.cloud.aurora.client.sdk.JobKey;
import org.apache.airavata.cloud.aurora.client.sdk.ReadOnlyScheduler;
import org.apache.airavata.cloud.aurora.client.sdk.Resource;
import org.apache.airavata.cloud.aurora.client.sdk.Response;
import org.apache.airavata.cloud.aurora.client.sdk.TaskConfig;
import org.apache.airavata.common.utils.ServerSettings;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuroraThriftClientUtil.
 */
public class AuroraThriftClientUtil {

	/** The Constant logger. */
	private final static Logger logger = LoggerFactory.getLogger(AuroraThriftClientUtil.class);
	
	/**
	 * Gets the executor config json.
	 *
	 * @param jobConfig the job config
	 * @return the executor config json
	 * @throws Exception the exception
	 */
	public static String getExecutorConfigJson(JobConfigBean jobConfig) throws Exception {
		String exeConfigJson = null;
		try {
			// read the executor config json template
			InputStream resourceAsStream = AuroraThriftClientUtil.class.getClassLoader()
					.getResourceAsStream(ServerSettings.getAuroraExecutorConfigTemplateFileName());
			JSONObject exeConfig = new JSONObject(new JSONTokener(resourceAsStream));
			if(exeConfig != null) {
				exeConfig.put("environment", jobConfig.getJob().getEnvironment());
				exeConfig.put("name", jobConfig.getJob().getName());
				exeConfig.put("role", jobConfig.getJob().getRole());
				exeConfig.put("cluster", jobConfig.getCluster());
				exeConfig.put("max_task_failures", jobConfig.getMaxTaskFailures());
				exeConfig.put("service", jobConfig.isService());
				
				exeConfig.getJSONObject("task").put("name", jobConfig.getTaskConfig().getTaskName());
				
				// add task resources
				exeConfig.getJSONObject("task").getJSONObject("resources")
					.put("cpu", jobConfig.getTaskConfig().getResources().getNumCpus());
				
				exeConfig.getJSONObject("task").getJSONObject("resources")
				.put("disk", jobConfig.getTaskConfig().getResources().getDiskMb() * 1024 * 1024);
				
				exeConfig.getJSONObject("task").getJSONObject("resources")
				.put("ram", jobConfig.getTaskConfig().getResources().getRamMb() * 1024 * 1024);
				
				// iterate over all processes
				for(ProcessBean process : jobConfig.getTaskConfig().getProcesses()) {
					// add process to constraints
					exeConfig.getJSONObject("task")
						.getJSONArray("constraints")
						.getJSONObject(0)
						.getJSONArray("order").put(process.getName());
					
					// define the process json
					JSONObject processJson = new JSONObject();
					processJson.put("final", process.isFinal())
						.put("daemon", process.isDaemon())
						.put("name", process.getName())
						.put("ephemeral", process.isEphemeral())
						.put("max_failures", process.getMax_failures())
						.put("min_duration", process.getMin_duration())
						.put("cmdline", process.getCmdLine());
					
					// add process json to list
					exeConfig.getJSONObject("task")
					.getJSONArray("processes").put(processJson);
				}
				
				// convert json object to string
				exeConfigJson = exeConfig.toString();
			}
 		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		return exeConfigJson;
	}
	
	/**
	 * Gets the resource set.
	 *
	 * @param resources the resources
	 * @return the resource set
	 * @throws Exception the exception
	 */
	public static Set<Resource> getResourceSet(ResourceBean resources) throws Exception {
		Set<Resource> resourceSet = new HashSet<>();
		
		try {
			if(resources != null) {
				// add numCpus
				Resource resource = new Resource();
				resource.setNumCpus(resources.getNumCpus());
				resourceSet.add(resource);
				
				// add diskMb
				resource = new Resource();
				resource.setDiskMb(resources.getDiskMb());
				resourceSet.add(resource);
				
				// add ramMb
				resource = new Resource();
				resource.setRamMb(resources.getRamMb());
				resourceSet.add(resource);
			} else {
				throw new Exception("Resource Bean is NULL!");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		
		return resourceSet;
	}
	
	/**
	 * Gets the executor config.
	 *
	 * @param exeConfigJson the exe config json
	 * @return the executor config
	 * @throws Exception the exception
	 */
	public static ExecutorConfig getExecutorConfig(String exeConfigJson) throws Exception {
		ExecutorConfig exeConfig = null;
		
		try {
			String executorName = ServerSettings.getAuroraExecutorName();
			
			// create the executor config
			if(exeConfigJson != null) {
				exeConfig = new ExecutorConfig(executorName, exeConfigJson);
			} else {
				throw new Exception("Aurora Executor Config Data is NULL!");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		
		return exeConfig;
	}
	
	/**
	 * Gets the aurora job key.
	 *
	 * @param jobKeyBean the job key bean
	 * @return the aurora job key
	 * @throws Exception the exception
	 */
	public static JobKey getAuroraJobKey(JobKeyBean jobKeyBean) throws Exception {
		JobKey jobKey = null;
		
		try {
			if(jobKeyBean != null) {
				jobKey = new JobKey(jobKeyBean.getRole(), 
						jobKeyBean.getEnvironment(), 
						jobKeyBean.getName());
			} else {
				throw new Exception("JobKey Bean is NULL!");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		
		return jobKey;
	}
	
	/**
	 * Gets the aurora identity.
	 *
	 * @param identityBean the identity bean
	 * @return the aurora identity
	 * @throws Exception the exception
	 */
	public static Identity getAuroraIdentity(IdentityBean identityBean) throws Exception {
		Identity owner = null;
		
		try {
			if(identityBean != null) {
				owner = new Identity(identityBean.getUser());
			} else {
				throw new Exception("Identity Bean is NULL!");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		
		return owner;
	}
	
	/**
	 * Gets the aurora job config.
	 *
	 * @param jobConfigBean the job config bean
	 * @return the aurora job config
	 * @throws Exception the exception
	 */
	public static JobConfiguration getAuroraJobConfig(JobConfigBean jobConfigBean) throws Exception {
		JobConfiguration jobConfig = null;
		
		try {
			if(jobConfigBean != null && 
					jobConfigBean.getTaskConfig() != null) {
				
				JobKey jobKey = getAuroraJobKey(jobConfigBean.getJob());
				Identity owner = getAuroraIdentity(jobConfigBean.getOwner());
				// Construct the task config
				TaskConfig taskConfig = new TaskConfig();
				taskConfig.setJob(jobKey);
				taskConfig.setOwner(owner);
				taskConfig.setIsService(jobConfigBean.isService()); 
				taskConfig.setNumCpus(jobConfigBean.getTaskConfig().getResources().getNumCpus()); 
				taskConfig.setRamMb(jobConfigBean.getTaskConfig().getResources().getRamMb());
				taskConfig.setDiskMb(jobConfigBean.getTaskConfig().getResources().getDiskMb()); 
				taskConfig.setPriority(jobConfigBean.getPriority());
				taskConfig.setMaxTaskFailures(jobConfigBean.getMaxTaskFailures()); 
				taskConfig.setResources(getResourceSet(jobConfigBean.getTaskConfig().getResources()));
				
				// construct the executor config for this job
				taskConfig.setExecutorConfig(getExecutorConfig(getExecutorConfigJson(jobConfigBean)));
				
				// construct the job configuration
				jobConfig = new JobConfiguration(jobKey, 
						owner, null, taskConfig, jobConfigBean.getInstances());
				
			} else {
				throw new Exception("JobConfig, TaskConfig Bean is/are NULL!");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		
		return jobConfig;
	}
	
	/**
	 * Gets the response bean.
	 *
	 * @param response the response
	 * @param resultType the result type
	 * @return the response bean
	 */
	public static ResponseBean getResponseBean(Response response, ResponseResultType resultType) {
		switch (resultType) {
			case GET_JOBS:
				return getJobsResponseBean(response);
			case GET_JOB_DETAILS:
				return getJobDetailsResponseBean(response);
			case GET_PENDING_JOB_REASON:
				return getPendingJobReasonBean(response);
			default:
				return getJobResponse(response);
		}
	}
	
	/**
	 * Gets the job details response bean.
	 *
	 * @param response the response
	 * @return the job details response bean
	 */
	private static JobDetailsResponseBean getJobDetailsResponseBean(Response response) {
		JobDetailsResponseBean responseBean = null;
		if(response != null) {
			responseBean = new JobDetailsResponseBean(getJobResponse(response));
			responseBean.setTasks(response.getResult().getScheduleStatusResult().getTasks());
		}
		
		return responseBean;
	}
	
	/**
	 * Gets the pending job reason bean.
	 *
	 * @param response the response
	 * @return the pending job reason bean
	 */
	private static PendingJobReasonBean getPendingJobReasonBean(Response response) {
		PendingJobReasonBean responseBean = null;
		if(response != null) {
			responseBean = new PendingJobReasonBean(getJobResponse(response));
			responseBean.setReasons(response.getResult().getGetPendingReasonResult().getReasons());
		}
		
		return responseBean;
	}
	
	/**
	 * Gets the jobs response bean.
	 *
	 * @param response the response
	 * @return the jobs response bean
	 */
	private static GetJobsResponseBean getJobsResponseBean(Response response) {
		GetJobsResponseBean responseBean = null;
		if(response != null) {
			responseBean = new GetJobsResponseBean(getJobResponse(response));
			//TODO: set jobconfig list in response
		}
		
		return responseBean;
	}
	
	/**
	 * Gets the job response.
	 *
	 * @param response the response
	 * @return the job response
	 */
	private static ResponseBean getJobResponse(Response response) {
		ResponseBean responseBean = null;
		if(response != null) {
			responseBean = new ResponseBean();
			responseBean.setResponseCode(ResponseCodeEnum
					.findByValue(response.getResponseCode().getValue()));
			
			ServerInfoBean serverInfo = new ServerInfoBean(response.getServerInfo().getClusterName(), 
					response.getServerInfo().getStatsUrlPrefix()); 
			responseBean.setServerInfo(serverInfo);
		}
		
		return responseBean;
	}
	
	/**
	 * Checks if is scheduler host reachable.
	 *
	 * @param connectionUrl the connection url
	 * @param connectionTimeout the connection timeout
	 * @return true, if is scheduler host reachable
	 */
	public static boolean isSchedulerHostReachable(String connectionUrl, int connectionTimeout) {
		boolean isReachable = false;
		ReadOnlyScheduler.Client auroraSchedulerClient = null;
		try {
			// connect to scheduler & run dummy command
			auroraSchedulerClient = AuroraSchedulerClientFactory.createReadOnlySchedulerClient(connectionUrl, connectionTimeout);
			auroraSchedulerClient.getTierConfigs();
			
			// host is reachable
			isReachable = true;
		} catch(Exception ex) {
			logger.error("Timed-out connecting to URL: " + connectionUrl);
		}
		return isReachable;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
//		JobKeyBean jobKey = new JobKeyBean("devel", "centos", "test_job");
//		IdentityBean owner = new IdentityBean("centos");
//		
//		ProcessBean proc1 = new ProcessBean("process_1", "echo 'hello_world_1'", false);
//		ProcessBean proc2 = new ProcessBean("process_2", "echo 'hello_world_2'", false);
//		Set<ProcessBean> processes = new HashSet<>();
//		processes.add(proc1);
//		processes.add(proc2);
//		
//		ResourceBean resources = new ResourceBean(0.1, 8, 1);
//		
//		TaskConfigBean taskConfig = new TaskConfigBean("task_hello_world", processes, resources);
//		JobConfigBean jobConfig = new JobConfigBean(jobKey, owner, taskConfig, "example");
//		
//		String executorConfigJson = getExecutorConfigJson(jobConfig);
//		System.out.println(executorConfigJson);
		
//		System.out.println(new Scanner(AuroraThriftClientUtil.class.getClassLoader().getResourceAsStream("executor-config-template.json"), "UTF-8").useDelimiter("\\A").next());
	}
}
