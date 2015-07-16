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
package org.apache.airavata.gfac.impl;

import com.google.common.eventbus.EventBus;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.cluster.OutputParser;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.config.DataTransferTaskConfig;
import org.apache.airavata.gfac.core.config.GFacYamlConfigruation;
import org.apache.airavata.gfac.core.config.JobSubmitterTaskConfig;
import org.apache.airavata.gfac.core.config.ResourceConfig;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.impl.job.LSFJobConfiguration;
import org.apache.airavata.gfac.impl.job.LSFOutputParser;
import org.apache.airavata.gfac.impl.job.PBSJobConfiguration;
import org.apache.airavata.gfac.impl.job.PBSOutputParser;
import org.apache.airavata.gfac.impl.job.SlurmJobConfiguration;
import org.apache.airavata.gfac.impl.job.SlurmOutputParser;
import org.apache.airavata.gfac.impl.job.UGEJobConfiguration;
import org.apache.airavata.gfac.impl.job.UGEOutputParser;
import org.apache.airavata.gfac.monitor.email.EmailBasedMonitor;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.impl.RabbitMQProcessLaunchConsumer;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusPublisher;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Factory {

	private static final Logger log = LoggerFactory.getLogger(Factory.class);
/*	static{
		try {
			loadConfiguration();
		} catch (GFacException e) {
			log.error("Error while loading configurations");
		}
	}*/

	private static GFacEngine engine;
	private static Publisher statusPublisher;
	private static CuratorFramework curatorClient;
	private static EmailBasedMonitor emailBasedMonitor;
	private static Map<String, RemoteCluster> remoteClusterMap = new HashMap<>();
	private static Map<JobSubmissionProtocol, JobSubmissionTask> jobSubmissionTask = new HashMap<>();
	private static Map<DataMovementProtocol, Task> dataMovementTask = new HashMap<>();
	private static Map<ResourceJobManagerType, ResourceConfig> resources = new HashMap<>();
	private static Map<MonitorMode, JobMonitor> jobMonitorServices = new HashMap<>();
	private static RabbitMQProcessLaunchConsumer processLaunchConsumer;

	public static GFacEngine getGFacEngine() throws GFacException {
		if (engine == null) {
			synchronized (GFacEngineImpl.class) {
				if (engine == null) {
					engine = new GFacEngineImpl();
				}
			}
		}
		return engine;
	}

	public static ExperimentCatalog getDefaultExpCatalog() throws RegistryException {
		return RegistryFactory.getDefaultExpCatalog();
	}

	public static AppCatalog getDefaultAppCatalog() throws AppCatalogException {
		return RegistryFactory.getAppCatalog();
	}

	public static Publisher getStatusPublisher() throws AiravataException {
		if (statusPublisher == null) {
			synchronized (RabbitMQStatusPublisher.class) {
				if (statusPublisher == null) {
					statusPublisher = new RabbitMQStatusPublisher();
				}
			}
		}
		return statusPublisher;
	}

	public static CuratorFramework getCuratorClient() throws ApplicationSettingsException {
		if (curatorClient == null) {
			synchronized (Factory.class) {
				if (curatorClient == null) {
					String connectionSting = ServerSettings.getZookeeperConnection();
					RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
					curatorClient = CuratorFrameworkFactory.newClient(connectionSting, retryPolicy);
				}
			}
		}
		return curatorClient;
	}

	public static RabbitMQProcessLaunchConsumer getProcessLaunchConsumer() throws AiravataException {
		if (processLaunchConsumer == null) {
			processLaunchConsumer = new RabbitMQProcessLaunchConsumer();
		}
		return processLaunchConsumer;
	}

	public static JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager) throws GFacException {
		ResourceConfig resourceConfig = Factory.getResourceConfig(resourceJobManager.getResourceJobManagerType());
		OutputParser outputParser;
		try {
			Class<? extends OutputParser> aClass = Class.forName(resourceConfig.getCommandOutputParser()).asSubclass
					(OutputParser.class);
			outputParser = aClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new GFacException("Error while instantiating output parser for " + resourceJobManager
					.getResourceJobManagerType().name());
		}

		switch (resourceJobManager.getResourceJobManagerType()) {

			case PBS:
				return new PBSJobConfiguration("PBSTemplate.xslt", ".pbs", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), outputParser);
			case SLURM:
				return new SlurmJobConfiguration("SLURMTemplate.xslt", ".slurm", resourceJobManager
						.getJobManagerBinPath(), resourceJobManager.getJobManagerCommands(), outputParser);
			case LSF:
				return new LSFJobConfiguration("LSFTemplate.xslt", ".lsf", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), outputParser);
			case UGE:
				return new UGEJobConfiguration("UGETemplate.xslt", ".pbs", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), outputParser);
			default:
				return null;
		}

	}

	public static HostScheduler getHostScheduler() {
		return new DefaultHostScheduler();
	}


	/**
	 * Factory class manage reomete cluster map, this will solve too many connections/ sessions issues with cluster
	 * communications.
	 * @param jobSubmissionProtocol
	 * @param computeResourceId
	 * @param resourceJobManager
	 * @return
	 * @throws GFacException
	 * @throws AppCatalogException
	 * @throws AiravataException
	 */
	public static RemoteCluster getRemoteCluster(JobSubmissionProtocol jobSubmissionProtocol, String computeResourceId,
	                                             ResourceJobManager resourceJobManager) throws GFacException,
			AppCatalogException, AiravataException {

		String key = jobSubmissionProtocol.toString() + ":" + computeResourceId;
		RemoteCluster remoteCluster = remoteClusterMap.get(key);
		if (remoteCluster == null) {
			String hostName = Factory.getDefaultAppCatalog().getComputeResource().getComputeResource(computeResourceId).getHostName();
			// fixme - read login user name from computeResourcePreference
			ServerInfo serverInfo = new ServerInfo(ServerSettings.getSetting("ssh.username"), hostName);
			JobManagerConfiguration jobManagerConfiguration = getJobManagerConfiguration(resourceJobManager);
			AuthenticationInfo authenticationInfo = getSSHKeyAuthentication();
			remoteCluster = new HPCRemoteCluster(serverInfo, jobManagerConfiguration, authenticationInfo);
			remoteClusterMap.put(key, remoteCluster);
		}
		return remoteCluster;
	}

	private static SSHKeyAuthentication getSSHKeyAuthentication() throws ApplicationSettingsException {
		SSHKeyAuthentication sshKA = new SSHKeyAuthentication();
		sshKA.setUserName(ServerSettings.getSetting("ssh.username"));
		sshKA.setPassphrase(ServerSettings.getSetting("ssh.keypass"));
		sshKA.setPrivateKeyFilePath(ServerSettings.getSetting("ssh.private.key"));
		sshKA.setPublicKeyFilePath(ServerSettings.getSetting("ssh.public.key"));
		sshKA.setStrictHostKeyChecking(ServerSettings.getSetting("ssh.strict.hostKey.checking", "no"));
		sshKA.setKnownHostsFilePath(ServerSettings.getSetting("ssh.known.hosts.file", null));
		if (sshKA.getStrictHostKeyChecking().equals("yes") && sshKA.getKnownHostsFilePath() == null) {
			throw new ApplicationSettingsException("If ssh scrict hostky checking property is set to yes, you must " +
					"provid known host file path");
		}
		return sshKA;
	}

	public static JobSubmissionTask getJobSubmissionTask(JobSubmissionProtocol jobSubmissionProtocol) {
		return jobSubmissionTask.get(jobSubmissionProtocol);
	}

	public static Task getDataMovementTask(DataMovementProtocol dataMovementProtocol){
		return dataMovementTask.get(dataMovementProtocol);
	}

	public static ResourceConfig getResourceConfig(ResourceJobManagerType resourceJobManagerType) {
		return resources.get(resourceJobManagerType);
	}

	public static Map<ResourceJobManagerType, ResourceConfig> getResourceConfig() {
		return resources;
	}

	public static void loadConfiguration() throws GFacException {
		GFacYamlConfigruation config = new GFacYamlConfigruation();
		try {
			for (JobSubmitterTaskConfig jobSubmitterTaskConfig : config.getJobSbumitters()) {
				String taskClass = jobSubmitterTaskConfig.getTaskClass();
				Class<?> aClass = Class.forName(taskClass);
				Constructor<?> constructor = aClass.getConstructor();
				JobSubmissionTask task = (JobSubmissionTask) constructor.newInstance();
				task.init(jobSubmitterTaskConfig.getProperties());
				jobSubmissionTask.put(jobSubmitterTaskConfig.getSubmissionProtocol(), task);
			}

			for (DataTransferTaskConfig dataTransferTaskConfig : config.getFileTransferTasks()) {
				String taskClass = dataTransferTaskConfig.getTaskClass();
				Class<?> aClass = Class.forName(taskClass);
				Constructor<?> constructor = aClass.getConstructor();
				Task task = (Task) constructor.newInstance();
				task.init(dataTransferTaskConfig.getProperties());
				dataMovementTask.put(dataTransferTaskConfig.getTransferProtocol(), task);
			}

			for (ResourceConfig resourceConfig : config.getResourceConfiguration()) {
				resources.put(resourceConfig.getJobManagerType(), resourceConfig);
			}
		} catch (Exception e) {
			throw new GFacException("Gfac config issue", e);
		}
	}

	public static JobMonitor getMonitorService(MonitorMode monitorMode) throws AiravataException {
		JobMonitor jobMonitor = jobMonitorServices.get(monitorMode);
		if (jobMonitor == null) {
			synchronized (JobMonitor.class) {
				jobMonitor = jobMonitorServices.get(monitorMode);
				if (jobMonitor == null) {
					switch (monitorMode) {
						case JOB_EMAIL_NOTIFICATION_MONITOR:
							EmailBasedMonitor emailBasedMonitor = new EmailBasedMonitor(Factory.getResourceConfig());
							jobMonitorServices.put(MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR, emailBasedMonitor);
							jobMonitor = ((JobMonitor) emailBasedMonitor);
							new Thread(emailBasedMonitor).start();
					}
				}
			}
		}
		return jobMonitor;
	}

}
