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
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.impl.job.LSFJobConfiguration;
import org.apache.airavata.gfac.impl.job.LSFOutputParser;
import org.apache.airavata.gfac.impl.job.PBSJobConfiguration;
import org.apache.airavata.gfac.impl.job.PBSOutputParser;
import org.apache.airavata.gfac.impl.job.SlurmJobConfiguration;
import org.apache.airavata.gfac.impl.job.SlurmOutputParser;
import org.apache.airavata.gfac.impl.job.UGEJobConfiguration;
import org.apache.airavata.gfac.impl.job.UGEOutputParser;
import org.apache.airavata.gfac.monitor.email.EmailBasedMonitor;
import org.apache.airavata.gfac.monitor.email.EmailMonitorFactory;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class Factory {

	private static GFacEngine engine;
	private static Map<String, RemoteCluster> remoteClusterMap;
	private static LocalEventPublisher localEventPublisher;
	private static CuratorFramework curatorClient;
	private static EmailBasedMonitor emailBasedMonitor;
	private static Date startMonitorDate = Calendar.getInstance().getTime();

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

	public static RemoteCluster getRemoteCluster(ServerInfo serverInfo) {
		return remoteClusterMap.get(serverInfo.getHost());
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
				synchronized (EmailMonitorFactory.class){
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

	public static JobManagerConfiguration getPBSJobManager(String installedPath) {
		return new PBSJobConfiguration("PBSTemplate.xslt",".pbs", installedPath, new PBSOutputParser());
	}

	public static JobManagerConfiguration getSLURMJobManager(String installedPath) {
		return new SlurmJobConfiguration("SLURMTemplate.xslt", ".slurm", installedPath, new SlurmOutputParser());
	}

	public static JobManagerConfiguration getUGEJobManager(String installedPath) {
		return new UGEJobConfiguration("UGETemplate.xslt", ".pbs", installedPath, new UGEOutputParser());
	}

	public static JobManagerConfiguration getLSFJobManager(String installedPath) {
		return new LSFJobConfiguration("LSFTemplate.xslt", ".lsf", installedPath, new LSFOutputParser());
	}
}
