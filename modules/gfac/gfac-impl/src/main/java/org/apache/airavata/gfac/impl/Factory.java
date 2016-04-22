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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacEngine;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.cluster.OutputParser;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.config.DataTransferTaskConfig;
import org.apache.airavata.gfac.core.config.GFacYamlConfigruation;
import org.apache.airavata.gfac.core.config.JobSubmitterTaskConfig;
import org.apache.airavata.gfac.core.config.ResourceConfig;
import org.apache.airavata.gfac.core.context.GFacContext;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.scheduler.HostScheduler;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.watcher.CancelRequestWatcher;
import org.apache.airavata.gfac.core.watcher.RedeliveryRequestWatcher;
import org.apache.airavata.gfac.impl.job.*;
import org.apache.airavata.gfac.impl.task.ArchiveTask;
import org.apache.airavata.gfac.impl.watcher.CancelRequestWatcherImpl;
import org.apache.airavata.gfac.impl.watcher.RedeliveryRequestWatcherImpl;
import org.apache.airavata.gfac.monitor.email.EmailBasedMonitor;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.impl.RabbitMQProcessLaunchConsumer;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusPublisher;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.process.ProcessModel;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	private static GFacContext gfacContext;
	private static Publisher statusPublisher;
	private static CuratorFramework curatorClient;
	private static EmailBasedMonitor emailBasedMonitor;
	private static Map<String, RemoteCluster> remoteClusterMap = new HashMap<>();
	private static Map<JobSubmissionProtocol, JobSubmissionTask> jobSubmissionTask = new HashMap<>();
	private static Map<DataMovementProtocol, Task> dataMovementTask = new HashMap<>();
	private static Map<ResourceJobManagerType, ResourceConfig> resources = new HashMap<>();
	private static Map<MonitorMode, JobMonitor> jobMonitorServices = new HashMap<>();
	private static RabbitMQProcessLaunchConsumer processLaunchConsumer;
	private static Map<String, Session> sessionMap = new HashMap<>();

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

	public static GFacContext getGfacContext() {
		if (gfacContext == null) {
			gfacContext = GFacContext.getInstance();
		}
		return gfacContext;
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
            case FORK:
                return new ForkJobConfiguration("ForkTemplate.xslt", ".sh", resourceJobManager.getJobManagerBinPath(),
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
	 * @param processContext
	 * @return
	 * @throws GFacException
	 * @throws AppCatalogException
	 * @throws AiravataException
	 */
	public static RemoteCluster getJobSubmissionRemoteCluster(ProcessContext processContext)
            throws GFacException, AppCatalogException, AiravataException {

        String computeResourceId = processContext.getComputeResourceId();
        JobSubmissionProtocol jobSubmissionProtocol = processContext.getJobSubmissionProtocol();
        String key = jobSubmissionProtocol.name() + ":" + computeResourceId;
		RemoteCluster remoteCluster = remoteClusterMap.get(key);
        if (remoteCluster == null) {
            JobManagerConfiguration jobManagerConfiguration = getJobManagerConfiguration(processContext.getResourceJobManager());
            if (jobSubmissionProtocol == JobSubmissionProtocol.LOCAL ||
                    jobSubmissionProtocol == JobSubmissionProtocol.LOCAL_FORK) {
                remoteCluster = new LocalRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration, null);
            } else if (jobSubmissionProtocol == JobSubmissionProtocol.SSH ||
                    jobSubmissionProtocol == JobSubmissionProtocol.SSH_FORK) {
                remoteCluster = new HPCRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration,
                        processContext.getSshKeyAuthentication());
            }
            remoteClusterMap.put(key, remoteCluster);
        }else {
            AuthenticationInfo authentication = remoteCluster.getAuthentication();
            if (authentication instanceof SSHKeyAuthentication){
                SSHKeyAuthentication sshKeyAuthentication = (SSHKeyAuthentication)authentication;
                if (!sshKeyAuthentication.getUserName().equals(getLoginUserName(processContext))){
                    JobManagerConfiguration jobManagerConfiguration = getJobManagerConfiguration(processContext.getResourceJobManager());
                    if (jobSubmissionProtocol == JobSubmissionProtocol.SSH ||
                            jobSubmissionProtocol == JobSubmissionProtocol.SSH_FORK) {
                        remoteCluster = new HPCRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration,
                                processContext.getSshKeyAuthentication());
                    }
                }

            }
        }
		return remoteCluster;
	}

    public static RemoteCluster getDataMovementRemoteCluster(ProcessContext processContext)
            throws GFacException, AiravataException {

        String computeResourceId = processContext.getComputeResourceId();
        DataMovementProtocol dataMovementProtocol = processContext.getDataMovementProtocol();
        String key = dataMovementProtocol.name() + ":" + computeResourceId;
        RemoteCluster remoteCluster = remoteClusterMap.get(key);
        if (remoteCluster == null) {
            JobManagerConfiguration jobManagerConfiguration = getJobManagerConfiguration(processContext.getResourceJobManager());
            if (dataMovementProtocol == DataMovementProtocol.LOCAL) {
                remoteCluster = new LocalRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration, null);
            } else if (dataMovementProtocol == DataMovementProtocol.SCP) {
                remoteCluster = new HPCRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration,
                        processContext.getSshKeyAuthentication());
            }

            remoteClusterMap.put(key, remoteCluster);
        }else {
            AuthenticationInfo authentication = remoteCluster.getAuthentication();
            if (authentication instanceof SSHKeyAuthentication){
                SSHKeyAuthentication sshKeyAuthentication = (SSHKeyAuthentication)authentication;
                if (!sshKeyAuthentication.getUserName().equals(getLoginUserName(processContext))){
                    JobManagerConfiguration jobManagerConfiguration = getJobManagerConfiguration(processContext.getResourceJobManager());
                    dataMovementProtocol = processContext.getDataMovementProtocol();
                    if (dataMovementProtocol == DataMovementProtocol.SCP) {
                        remoteCluster = new HPCRemoteCluster(processContext.getServerInfo(), jobManagerConfiguration,
                                processContext.getSshKeyAuthentication());
                    }
                }

            }
        }
        return remoteCluster;
    }

	public static SSHKeyAuthentication getComputerResourceSSHKeyAuthentication(ProcessContext pc) throws GFacException {
        try {
            ComputeResourcePreference computeResourcePreference = pc.getComputeResourcePreference();
            String loginUserName = getLoginUserName(pc);
            String credentialStoreToken = computeResourcePreference.getResourceSpecificCredentialStoreToken();
            if (credentialStoreToken == null || credentialStoreToken.isEmpty()) {
                credentialStoreToken = pc.getGatewayResourceProfile().getCredentialStoreToken();
            }
            return getSshKeyAuthentication(pc.getGatewayId(),loginUserName, credentialStoreToken);
        } catch (ApplicationSettingsException | IllegalAccessException | InstantiationException | CredentialStoreException e) {
            throw new GFacException("Couldn't build ssh authentication object", e);
        }
    }

    public static SSHKeyAuthentication getStorageSSHKeyAuthentication(ProcessContext pc) throws GFacException {
        try {
            StoragePreference storagePreference = pc.getStoragePreference();
            String loginUserName = storagePreference.getLoginUserName();
            String credentialStoreToken = storagePreference.getResourceSpecificCredentialStoreToken();
            if (credentialStoreToken == null || credentialStoreToken.isEmpty()) {
                credentialStoreToken = pc.getGatewayResourceProfile().getCredentialStoreToken();
            }
            return getSshKeyAuthentication(pc.getGatewayId(), loginUserName, credentialStoreToken);
        }  catch (ApplicationSettingsException | IllegalAccessException | InstantiationException | CredentialStoreException e) {
            throw new GFacException("Couldn't build ssh authentication object", e);
        }
    }

    public static String  getLoginUserName(ProcessContext processContext) throws GFacException {
        try {
            ProcessModel processModel = processContext.getProcessModel();
            String loginUserName = null;
            String overrideLoginUserName = processModel.getResourceSchedule().getOverrideLoginUserName();
            if (overrideLoginUserName != null && !overrideLoginUserName.equals("")) {
                loginUserName = overrideLoginUserName;
            } else {
                loginUserName = processContext.getComputeResourcePreference().getLoginUserName();
            }

            return loginUserName;
        }  catch (Exception e) {
            throw new GFacException("Couldn't fetch loginUserName", e);
        }
    }

    public static String  getScratchLocation(ProcessContext processContext) throws GFacException {
        try {
            ProcessModel processModel = processContext.getProcessModel();
            String scratchLocation = null;
            String overrideScratchLocation = processModel.getResourceSchedule().getOverrideScratchLocation();
            if (overrideScratchLocation != null && !overrideScratchLocation.equals("")) {
                scratchLocation = overrideScratchLocation;
            } else {
                scratchLocation = processContext.getComputeResourcePreference().getScratchLocation();
            }

            return scratchLocation;
        }  catch (Exception e) {
            throw new GFacException("Couldn't fetch scratchLocation", e);
        }
    }

    private static SSHKeyAuthentication getSshKeyAuthentication(String gatewayId,
                                                                String loginUserName,
                                                                String credentialStoreToken)
            throws ApplicationSettingsException, IllegalAccessException, InstantiationException,
            CredentialStoreException, GFacException {

        SSHKeyAuthentication sshKA;CredentialReader credentialReader = GFacUtils.getCredentialReader();
        Credential credential = credentialReader.getCredential(gatewayId, credentialStoreToken);
        if (credential instanceof SSHCredential) {
            sshKA = new SSHKeyAuthentication();
            sshKA.setUserName(loginUserName);
            SSHCredential sshCredential = (SSHCredential) credential;
            sshKA.setPublicKey(sshCredential.getPublicKey());
            sshKA.setPrivateKey(sshCredential.getPrivateKey());
            sshKA.setPassphrase(sshCredential.getPassphrase());
            sshKA.setStrictHostKeyChecking("no");
/*            sshKA.setStrictHostKeyChecking(ServerSettings.getSetting("ssh.strict.hostKey.checking", "no"));
            sshKA.setKnownHostsFilePath(ServerSettings.getSetting("ssh.known.hosts.file", null));
            if (sshKA.getStrictHostKeyChecking().equals("yes") && sshKA.getKnownHostsFilePath() == null) {
                throw new ApplicationSettingsException("If ssh strict hostkey checking property is set to yes, you must " +
                        "provide known host file path");
            }*/
            return sshKA;
        } else {
            String msg = "Provided credential store token is not valid. Please provide the correct credential store token";
            log.error(msg);
            throw new GFacException("Invalid credential store token:" + credentialStoreToken);
        }
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

	public static JobMonitor getDefaultMonitorService() throws AiravataException {
		return getMonitorService(MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR);
	}

	public static RedeliveryRequestWatcher getRedeliveryReqeustWatcher(String experimentId, String processId) {
		return new RedeliveryRequestWatcherImpl(experimentId, processId);
	}

	public static CancelRequestWatcher getCancelRequestWatcher(String experimentId, String processId) {
		return new CancelRequestWatcherImpl(experimentId, processId);
	}

	public static synchronized Session getSSHSession(AuthenticationInfo authenticationInfo, ServerInfo serverInfo) throws AiravataException {
		SSHKeyAuthentication authentication = null;
		String key = serverInfo.getUserName() + "_" + serverInfo.getHost() + "_" + serverInfo.getPort();
		Session session = sessionMap.get(key);
		boolean valid = isValidSession(session);
		// FIXME - move following info logs to debug
		if (valid) {
			log.info("SSH Session validation succeeded, key :" + key);
			valid = testChannelCreation(session);
			if (valid) {
				log.info("Channel creation test succeeded, key :" + key);
			} else {
				log.info("Channel creation test failed, key :" + key);
			}
		} else {
			log.info("Session validation failed, key :" + key);
		}

		if (!valid) {
			if (session != null) {
				log.info("Reinitialize a new SSH session for :" + key);
			} else {
				log.info("Initialize a new SSH session for :" + key);
			}
			try {
				if (authenticationInfo instanceof SSHKeyAuthentication) {
					authentication = (SSHKeyAuthentication) authenticationInfo;
				} else {
					throw new AiravataException("Support ssh key authentication only");
				}
				JSch jSch = new JSch();
				jSch.addIdentity(UUID.randomUUID().toString(), authentication.getPrivateKey(), authentication.getPublicKey(),
						authentication.getPassphrase().getBytes());
				session = jSch.getSession(serverInfo.getUserName(), serverInfo.getHost(),
						serverInfo.getPort());
				session.setUserInfo(new DefaultUserInfo(serverInfo.getUserName(), null, authentication.getPassphrase()));
				if (authentication.getStrictHostKeyChecking().equals("yes")) {
					jSch.setKnownHosts(authentication.getKnownHostsFilePath());
				} else {
					session.setConfig("StrictHostKeyChecking", "no");
				}
				session.connect(); // 0 connection timeout
				sessionMap.put(key, session);
			} catch (JSchException e) {
				throw new AiravataException("JSch initialization error ", e);
			}
		} else {
			// FIXME - move following info log to debug
			log.info("Reuse SSH session for :" + key);
		}
		return sessionMap.get(key);

	}

	public static void disconnectSSHSession(ServerInfo serverInfo) {
		// TODO - remove session from map and call disconnect
	}

	private static boolean testChannelCreation(Session session) {

		String command = "pwd ";
		Channel channel = null;
		try {
			channel = session.openChannel("exec");
			StandardOutReader stdOutReader = new StandardOutReader();
			((ChannelExec) channel).setCommand(command);
			((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
			channel.connect();
			stdOutReader.onOutput(channel);
		} catch (JSchException e) {
			log.error("Test Channel creation failed.", e);
			return false;
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
		return true;
	}

	private static boolean isValidSession(Session session) {
		return session != null && session.isConnected();
	}

	public static Task getArchiveTask() {
		return new ArchiveTask();
	}

	private static class DefaultUserInfo implements UserInfo {

		private String userName;
		private String password;
		private String passphrase;

		public DefaultUserInfo(String userName, String password, String passphrase) {
			this.userName = userName;
			this.password = password;
			this.passphrase = passphrase;
		}

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassword(String s) {
			return false;
		}

		@Override
		public boolean promptPassphrase(String s) {
			return false;
		}

		@Override
		public boolean promptYesNo(String s) {
			return false;
		}

		@Override
		public void showMessage(String s) {

		}
	}
}
