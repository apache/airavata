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
package org.apache.airavata.cloud.aurora.client;

import java.util.HashSet;
import java.util.Set;

import org.apache.airavata.cloud.aurora.client.bean.GetJobsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.JobConfigBean;
import org.apache.airavata.cloud.aurora.client.bean.JobDetailsResponseBean;
import org.apache.airavata.cloud.aurora.client.bean.JobKeyBean;
import org.apache.airavata.cloud.aurora.client.bean.PendingJobReasonBean;
import org.apache.airavata.cloud.aurora.client.bean.ResponseBean;
import org.apache.airavata.cloud.aurora.client.sdk.AuroraSchedulerManager;
import org.apache.airavata.cloud.aurora.client.sdk.JobConfiguration;
import org.apache.airavata.cloud.aurora.client.sdk.JobKey;
import org.apache.airavata.cloud.aurora.client.sdk.ReadOnlyScheduler;
import org.apache.airavata.cloud.aurora.client.sdk.Response;
import org.apache.airavata.cloud.aurora.client.sdk.TaskQuery;
import org.apache.airavata.cloud.aurora.util.AuroraThriftClientUtil;
import org.apache.airavata.cloud.aurora.util.Constants;
import org.apache.airavata.cloud.aurora.util.ResponseResultType;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AuroraThriftClient.
 */
public class AuroraThriftClient {
	
	/** The Constant logger. */
	private final static Logger logger = LoggerFactory.getLogger(AuroraThriftClient.class);
	
	/** The read only scheduler client. */
	private ReadOnlyScheduler.Client readOnlySchedulerClient = null;
	
	/** The aurora scheduler manager client. */
	private AuroraSchedulerManager.Client auroraSchedulerManagerClient = null;
	
	/** The thrift client. */
	private static AuroraThriftClient thriftClient = null;
	
	/**
	 * Instantiates a new aurora thrift client.
	 */
	private AuroraThriftClient() {}
	
	/**
	 * Gets the aurora thrift client.
	 *
	 * @return the aurora thrift client
	 * @throws Exception the exception
	 */
	public static AuroraThriftClient getAuroraThriftClient() throws Exception {
		try {
			if(thriftClient == null) {
				synchronized(AuroraThriftClient.class) {
					if(thriftClient == null) {
						thriftClient = new AuroraThriftClient();
						
						// construct connection url for scheduler
						String auroraHosts = ServerSettings.getAuroraSchedulerHosts();
						Integer connectTimeout = ServerSettings.getAuroraSchedulerTimeout();
						
						// check reachable scheduler host
						if(auroraHosts != null && !auroraHosts.trim().isEmpty()) {
							auroraHosts = auroraHosts.trim();
							for(String auroraHost : auroraHosts.split(",")) {
								// malformed host string, should be of form <host:port>
								if(auroraHost.split(":").length != 2) {
									throw new Exception("Scheduler Host String: " + auroraHost + ", is malformed. Should be of form <hostname:port>!");
								}
								
								// read hostname, port & construct connection-url
								String hostname = auroraHost.split(":")[0];
								String port = auroraHost.split(":")[1];
								String connectionUrl = String.format(Constants.AURORA_SCHEDULER_CONNECTION_URL, hostname, port);
								
								// verify if connection succeeds
								if(AuroraThriftClientUtil.isSchedulerHostReachable(connectionUrl, connectTimeout)) {
									thriftClient.readOnlySchedulerClient = AuroraSchedulerClientFactory.createReadOnlySchedulerClient(connectionUrl, connectTimeout);
									thriftClient.auroraSchedulerManagerClient = AuroraSchedulerClientFactory.createSchedulerManagerClient(connectionUrl, connectTimeout);
									break;
								}
							}
							
							// check if scheduler connection successful
							if(thriftClient.auroraSchedulerManagerClient == null || 
									thriftClient.readOnlySchedulerClient == null) {
								throw new Exception("None of the Aurora scheduler hosts : <" + auroraHosts + "> were reachable, hence connection not established!");
							}
						} else {
							// aurora hosts not defined in the properties file
							throw new Exception("Aurora hosts not specified in airavata-server.properties file.");
						}
					}
				}
			}
		} catch(Exception ex) {
			logger.error("Couldn't initialize Aurora thrift client", ex);
			throw ex;
		}
		return thriftClient;
	}
	
	
	/**
	 * Reconnect with aurora scheduler.
	 *
	 * @return true, if successful
	 */
	private boolean reconnectWithAuroraScheduler() {
		boolean connectionSuccess = false;
		
		try {
			// construct connection url for scheduler
			String auroraHosts = ServerSettings.getAuroraSchedulerHosts();
			Integer connectTimeout = ServerSettings.getAuroraSchedulerTimeout();
			
			// check reachable scheduler host
			if(auroraHosts != null) {
				for(String auroraHost : auroraHosts.split(",")) {
					// malformed host string, should be of form <host:port>
					if(auroraHost.split(":").length != 2) {
						throw new Exception("Scheduler Host String: " + auroraHost + ", is malformed. Should be of form <hostname:port>!");
					}
					
					// read hostname, port & construct connection-url
					String hostname = auroraHost.split(":")[0];
					String port = auroraHost.split(":")[1];
					String connectionUrl = String.format(Constants.AURORA_SCHEDULER_CONNECTION_URL, hostname, port);
					
					// verify if connection succeeds
					if(AuroraThriftClientUtil.isSchedulerHostReachable(connectionUrl, connectTimeout)) {
						thriftClient.readOnlySchedulerClient = AuroraSchedulerClientFactory.createReadOnlySchedulerClient(connectionUrl, connectTimeout);
						thriftClient.auroraSchedulerManagerClient = AuroraSchedulerClientFactory.createSchedulerManagerClient(connectionUrl, connectTimeout);
						
						// set connection-success flag
						connectionSuccess = true;
					}
				}
			} else {
				// aurora hosts not defined in the properties file
				throw new Exception("Aurora hosts not specified in airavata-server.properties file.");
			}
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return connectionSuccess;
	}
	
	
	/**
	 * Creates the job.
	 *
	 * @param jobConfigBean the job config bean
	 * @return the response bean
	 * @throws Exception the exception
	 */
	public ResponseBean createJob(JobConfigBean jobConfigBean) throws Exception {
		ResponseBean response = null;
		// try till we get response or scheduler connection not found
		while(response == null) {
			try {
				if(jobConfigBean != null) {
					JobConfiguration jobConfig = AuroraThriftClientUtil.getAuroraJobConfig(jobConfigBean);
					Response createJobResponse = this.auroraSchedulerManagerClient.createJob(jobConfig);
					response = AuroraThriftClientUtil.getResponseBean(createJobResponse, ResponseResultType.CREATE_JOB);
				}
			} catch(Exception ex) {
				if (ex instanceof TTransportException) {
					// if re-connection success, retry command
					if (this.reconnectWithAuroraScheduler()) {
						continue;
					}
				}
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		
		return response;
	}
	
	/**
	 * Kill tasks.
	 *
	 * @param jobKeyBean the job key bean
	 * @param instances the instances
	 * @return the response bean
	 * @throws Exception the exception
	 */
	public ResponseBean killTasks(JobKeyBean jobKeyBean, Set<Integer> instances) throws Exception {
		ResponseBean response = null;
		// try till we get response or scheduler connection not found
		while(response == null) {
			try {
				if(jobKeyBean != null) {
					JobKey jobKey = AuroraThriftClientUtil.getAuroraJobKey(jobKeyBean);
					Response killTaskResponse = this.auroraSchedulerManagerClient.killTasks(jobKey, instances);
					response = AuroraThriftClientUtil.getResponseBean(killTaskResponse, ResponseResultType.KILL_TASKS);
				}
			} catch(Exception ex) {
				if (ex instanceof TTransportException) {
					// if re-connection success, retry command
					if (this.reconnectWithAuroraScheduler()) {
						continue;
					}
				}
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		return response;
	}
	
	/**
	 * Gets the job list.
	 *
	 * @param ownerRole the owner role
	 * @return the job list
	 * @throws Exception the exception
	 */
	public GetJobsResponseBean getJobList(String ownerRole) throws Exception {
		GetJobsResponseBean response = null;
		// try till we get response or scheduler connection not found
		while(response == null) {
			try {
					Response jobListResponse = this.readOnlySchedulerClient.getJobs(ownerRole);
					response = (GetJobsResponseBean) AuroraThriftClientUtil.getResponseBean(jobListResponse, ResponseResultType.GET_JOBS);
			} catch(Exception ex) {
				if (ex instanceof TTransportException) {
					// if re-connection success, retry command
					if (this.reconnectWithAuroraScheduler()) {
						continue;
					}
				}
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		return response;
	}
	
	/**
	 * Gets the pending reason for job.
	 *
	 * @param jobKeyBean the job key bean
	 * @return the pending reason for job
	 * @throws Exception the exception
	 */
	public PendingJobReasonBean getPendingReasonForJob(JobKeyBean jobKeyBean) throws Exception {
		PendingJobReasonBean response = null;
		// try till we get response or scheduler connection not found
		while(response == null) {
			try {
					JobKey jobKey = AuroraThriftClientUtil.getAuroraJobKey(jobKeyBean);
					Set<JobKey> jobKeySet = new HashSet<>();
					jobKeySet.add(jobKey);
					
					TaskQuery query = new TaskQuery();
					query.setJobKeys(jobKeySet);
					
					Response pendingReasonResponse = this.readOnlySchedulerClient.getPendingReason(query);
					response = (PendingJobReasonBean) AuroraThriftClientUtil.getResponseBean(pendingReasonResponse, ResponseResultType.GET_PENDING_JOB_REASON);
			} catch(Exception ex) {
				if (ex instanceof TTransportException) {
					// if re-connection success, retry command
					if (this.reconnectWithAuroraScheduler()) {
						continue;
					}
				}
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		return response;
	}
	
	/**
	 * Gets the job details.
	 *
	 * @param jobKeyBean the job key bean
	 * @return the job details
	 * @throws Exception the exception
	 */
	public JobDetailsResponseBean getJobDetails(JobKeyBean jobKeyBean) throws Exception {
		JobDetailsResponseBean response = null;
		// try till we get response or scheduler connection not found
		while(response == null) {
			try {
				if(jobKeyBean != null) {
					JobKey jobKey = AuroraThriftClientUtil.getAuroraJobKey(jobKeyBean);
					Set<JobKey> jobKeySet = new HashSet<>();
					jobKeySet.add(jobKey);
					
					TaskQuery query = new TaskQuery();
					query.setJobKeys(jobKeySet);
					
					Response jobDetailsResponse = this.readOnlySchedulerClient.getTasksStatus(query);
					response = (JobDetailsResponseBean) AuroraThriftClientUtil.getResponseBean(jobDetailsResponse, ResponseResultType.GET_JOB_DETAILS);
				}
			} catch(Exception ex) {
				if (ex instanceof TTransportException) {
					// if re-connection success, retry command
					if (this.reconnectWithAuroraScheduler()) {
						continue;
					}
				}
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		return response;
	}
}
