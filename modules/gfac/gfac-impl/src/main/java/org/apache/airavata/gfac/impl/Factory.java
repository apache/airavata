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
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.config.DataTransferTaskConfig;
import org.apache.airavata.gfac.core.config.GFacYamlConfigruation;
import org.apache.airavata.gfac.core.config.JobSubmitterTaskConfig;
import org.apache.airavata.gfac.core.config.ResourceConfig;
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
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Factory {

	private static GFacEngine engine;
	private static LocalEventPublisher localEventPublisher;
	private static CuratorFramework curatorClient;
	private static EmailBasedMonitor emailBasedMonitor;
	private static Date startMonitorDate = Calendar.getInstance().getTime();
	private static Map<String, RemoteCluster> remoteClusterMap = new HashMap<>();
	private static Map<JobSubmissionProtocol, JobSubmissionTask> jobSubmissionTask = new HashMap<>();
	private static Map<DataMovementProtocol, Task> dataMovementTask = new HashMap<>();
	private static Map<ResourceJobManagerType, ResourceConfig> resources = new HashMap<>();
	private static boolean readConfig = false;

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

	public static LocalEventPublisher getLocalEventPublisher() {
		if (localEventPublisher == null) {
			synchronized (LocalEventPublisher.class) {
				if (localEventPublisher == null) {
					localEventPublisher = new LocalEventPublisher(new EventBus());
				}
			}
		}
		return localEventPublisher;
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

	public static JobMonitor getJobMonitor(ResourceJobManagerType resourceJobManagerType) throws AiravataException {
		if (resourceJobManagerType == ResourceJobManagerType.FORK) {
			return null; // TODO write a job monitor for this.
		} else {
			if (emailBasedMonitor == null) {
				synchronized (EmailBasedMonitor.class) {
					if (emailBasedMonitor == null) {
						emailBasedMonitor = new EmailBasedMonitor(resourceJobManagerType);
						emailBasedMonitor.setDate(startMonitorDate);
						new Thread(emailBasedMonitor).start();
					}
				}
			}
			return emailBasedMonitor;
		}
	}

	public static JobManagerConfiguration getJobManagerConfiguration(ResourceJobManager resourceJobManager) {
		switch (resourceJobManager.getResourceJobManagerType()) {
			case PBS:
				return new PBSJobConfiguration("PBSTemplate.xslt", ".pbs", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), new PBSOutputParser());
			case SLURM:
				return new SlurmJobConfiguration("SLURMTemplate.xslt", ".slurm", resourceJobManager
						.getJobManagerBinPath(), resourceJobManager.getJobManagerCommands(), new SlurmOutputParser());
			case LSF:
				return new LSFJobConfiguration("LSFTemplate.xslt", ".lsf", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), new LSFOutputParser());
			case UGE:
				return new UGEJobConfiguration("UGETemplate.xslt", ".pbs", resourceJobManager.getJobManagerBinPath(),
						resourceJobManager.getJobManagerCommands(), new UGEOutputParser());

			default:
				return null;
		}
	}

	public static HostScheduler getHostScheduler() {
		return new DefaultHostScheduler();
	}


	public static RemoteCluster getRemoteCluster(ComputeResourcePreference cRP) throws GFacException,
			AppCatalogException, AiravataException {

		String key = cRP.getPreferredJobSubmissionProtocol().toString() + ":" + cRP.getComputeResourceId();
		RemoteCluster remoteCluster = remoteClusterMap.get(key);
		if (remoteCluster == null) {
			String hostName = Factory.getDefaultAppCatalog().getComputeResource().getComputeResource(cRP
					.getComputeResourceId()).getHostName();
			// fixme - read login user name from computeResourcePreference
			ServerInfo serverInfo = new ServerInfo(ServerSettings.getSetting("ssh.username"), hostName);
			List<JobSubmissionInterface> jobSubmissionInterfaces = Factory.getDefaultAppCatalog().getComputeResource()
					.getComputeResource(cRP.getComputeResourceId())
					.getJobSubmissionInterfaces();

			ResourceJobManager resourceJobManager = null;
			JobSubmissionInterface jsInterface = null;
			for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
				if (jobSubmissionInterface.getJobSubmissionProtocol() == cRP.getPreferredJobSubmissionProtocol()) {
					jsInterface = jobSubmissionInterface;
					break;
				}
			}
			if (jsInterface == null) {
				// TODO: throw an exception.
			} else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
				SSHJobSubmission sshJobSubmission = getDefaultAppCatalog().getComputeResource().getSSHJobSubmission
						(jsInterface.getJobSubmissionInterfaceId());
				resourceJobManager = sshJobSubmission.getResourceJobManager();
			} else if (jsInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.LOCAL) {
				LOCALSubmission localSubmission = getDefaultAppCatalog().getComputeResource().getLocalJobSubmission
						(jsInterface.getJobSubmissionInterfaceId());
				resourceJobManager = localSubmission.getResourceJobManager();
			} else {
				// TODO : throw an not supported jobsubmission protocol exception. we only support SSH and LOCAL
			}

			if (resourceJobManager == null) {
				// TODO throw an exception
			}

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

	public static JobSubmissionTask getJobSubmissionTask(JobSubmissionProtocol jobSubmissionProtocol) throws
			GFacException {
		if (!readConfig) {
			loadConfiguration();
		}
		return jobSubmissionTask.get(jobSubmissionProtocol);
	}

	public static Task getDataMovementTask(DataMovementProtocol dataMovementProtocol) throws GFacException {
		if (!readConfig) {
			loadConfiguration();
		}
		return dataMovementTask.get(dataMovementProtocol);
	}

	public static ResourceConfig getResourceConfig(ResourceJobManagerType resourceJobManagerType) throws
			GFacException {
		if (!readConfig) {
			loadConfiguration();
		}
		return resources.get(resourceJobManagerType);
	}

	private static void loadConfiguration() throws GFacException {
		GFacYamlConfigruation config = new GFacYamlConfigruation();
		for (JobSubmitterTaskConfig jobSubmitterTaskConfig : config.getJobSbumitters()) {
			jobSubmissionTask.put(jobSubmitterTaskConfig.getSubmissionProtocol(), null);
		}

		for (DataTransferTaskConfig dataTransferTaskConfig : config.getFileTransferTasks()) {
			dataMovementTask.put(dataTransferTaskConfig.getTransferProtocol(), null);
		}

		for (ResourceConfig resourceConfig : config.getResourceConfiguration()) {
			resources.put(resourceConfig.getJobManagerType(), resourceConfig);
		}
		readConfig = true;
	}


}
