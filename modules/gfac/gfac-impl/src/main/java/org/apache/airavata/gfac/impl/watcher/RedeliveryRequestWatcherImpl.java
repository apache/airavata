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
package org.apache.airavata.gfac.impl.watcher;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.watcher.RedeliveryRequestWatcher;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;

public class RedeliveryRequestWatcherImpl implements RedeliveryRequestWatcher {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(RedeliveryRequestWatcherImpl.class);
	private final String processId;
	private final String experimentId;

	public RedeliveryRequestWatcherImpl(String experimentId, String procesId) {
		this.experimentId = experimentId;
		this.processId = procesId;
	}

	@Override
	public void process(WatchedEvent watchedEvent) throws Exception {
		String path = watchedEvent.getPath();
		Watcher.Event.EventType eventType = watchedEvent.getType();
		log.info("Redelivery request came for zk path {} event type {} ", path, eventType.name());
		CuratorFramework curatorClient = Factory.getCuratorClient();
		switch (eventType) {
			case NodeDataChanged:
				byte[] bytes = curatorClient.getData().forPath(path);
				String serverName = new String(bytes);
				if (ServerSettings.getGFacServerName().trim().equals(serverName)) {
					curatorClient.getData().usingWatcher(this).forPath(path);
					log.info("processId: {},event type {}, change data with same server name : {}", processId,
							eventType, serverName);
				} else {
					ProcessContext processContext = Factory.getGfacContext().getProcess(processId);
					if (processContext != null) {
						processContext.setHandOver(true);
						log.info("processId : {}, event type {}, handing over to new server instance : {}", processId,
								eventType, serverName);
					} else {
						log.info("Redelivery request came for processId {}, with event type {}, but couldn't find " +
								"process context", processId, eventType.name());
					}
				}
				break;
			case NodeDeleted:
				//end of experiment execution, ignore this event
				log.info("Redelivery watcher trigger for process {} with event type {}", processId, eventType.name());
				break;
			case NodeCreated:
			case NodeChildrenChanged:
			case None:
				if (path != null) {
					curatorClient.getData().usingWatcher(this).forPath(path);
					log.info("Redelivery watcher trigger for process {} with event type {}", processId, eventType.name());
				}
				break;
				// not yet implemented
			default:
				if (path != null) {
					curatorClient.getData().usingWatcher(this).forPath(path);
					log.info("Redelivery watcher trigger for process {} with event type {}", processId, eventType.name());
				}
				break;
		}
	}
}
