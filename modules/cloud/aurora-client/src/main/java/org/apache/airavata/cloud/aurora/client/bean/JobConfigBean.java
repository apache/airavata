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
package org.apache.airavata.cloud.aurora.client.bean;

// TODO: Auto-generated Javadoc
/**
 * The Class JobConfigBean.
 */
public class JobConfigBean {

	/** The job. */
	private JobKeyBean job;
	
	/** The owner. */
	private IdentityBean owner;
	
	/** The task config. */
	private TaskConfigBean taskConfig;
	
	/** The is service. */
	private boolean isService;
	
	/** The priority. */
	private int priority;
	
	/** The production. */
	private boolean production;
	
	/** The max task failures. */
	private int maxTaskFailures;
	
	/** The instances. */
	private int instances;
	
	/** The cluster. */
	private String cluster;
	
	/**
	 * Instantiates a new job config bean.
	 *
	 * @param job the job
	 * @param owner the owner
	 * @param taskConfig the task config
	 * @param cluster the cluster
	 */
	public JobConfigBean(JobKeyBean job, IdentityBean owner, TaskConfigBean taskConfig, String cluster) {
		this.job = job;
		this.owner = owner;
		this.taskConfig = taskConfig;
		this.cluster = cluster;
		
		// set defaults
		this.isService = false;
		this.maxTaskFailures = 1;
		this.instances = 1;
	}
	
	/**
	 * Gets the job.
	 *
	 * @return the job
	 */
	public JobKeyBean getJob() {
		return job;
	}
	
	/**
	 * Sets the job.
	 *
	 * @param job the new job
	 */
	public void setJob(JobKeyBean job) {
		this.job = job;
	}

	/**
	 * Gets the owner.
	 *
	 * @return the owner
	 */
	public IdentityBean getOwner() {
		return owner;
	}

	/**
	 * Sets the owner.
	 *
	 * @param owner the new owner
	 */
	public void setOwner(IdentityBean owner) {
		this.owner = owner;
	}

	/**
	 * Checks if is service.
	 *
	 * @return true, if is service
	 */
	public boolean isService() {
		return isService;
	}

	/**
	 * Sets the service.
	 *
	 * @param isService the new service
	 */
	public void setService(boolean isService) {
		this.isService = isService;
	}
	
	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 *
	 * @param priority the new priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Checks if is production.
	 *
	 * @return true, if is production
	 */
	public boolean isProduction() {
		return production;
	}

	/**
	 * Sets the production.
	 *
	 * @param production the new production
	 */
	public void setProduction(boolean production) {
		this.production = production;
	}

	/**
	 * Gets the task config.
	 *
	 * @return the task config
	 */
	public TaskConfigBean getTaskConfig() {
		return taskConfig;
	}

	/**
	 * Sets the task config.
	 *
	 * @param taskConfig the new task config
	 */
	public void setTaskConfig(TaskConfigBean taskConfig) {
		this.taskConfig = taskConfig;
	}

	/**
	 * Gets the max task failures.
	 *
	 * @return the max task failures
	 */
	public int getMaxTaskFailures() {
		return maxTaskFailures;
	}

	/**
	 * Sets the max task failures.
	 *
	 * @param maxTaskFailures the new max task failures
	 */
	public void setMaxTaskFailures(int maxTaskFailures) {
		this.maxTaskFailures = maxTaskFailures;
	}

	/**
	 * Gets the instances.
	 *
	 * @return the instances
	 */
	public int getInstances() {
		return instances;
	}

	/**
	 * Sets the instances.
	 *
	 * @param instances the new instances
	 */
	public void setInstances(int instances) {
		this.instances = instances;
	}

	/**
	 * Gets the cluster.
	 *
	 * @return the cluster
	 */
	public String getCluster() {
		return cluster;
	}

	/**
	 * Sets the cluster.
	 *
	 * @param cluster the new cluster
	 */
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
}
