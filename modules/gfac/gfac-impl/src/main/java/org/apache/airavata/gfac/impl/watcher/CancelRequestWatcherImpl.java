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
package org.apache.airavata.gfac.impl.watcher;

import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.watcher.CancelRequestWatcher;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelRequestWatcherImpl implements CancelRequestWatcher {
	private static final Logger log = LoggerFactory.getLogger(CancelRequestWatcherImpl.class);

	@Override
	public void process(WatchedEvent watchedEvent) throws Exception {
		// this watcher change data in cancel listener node in the experiment node
		String path = watchedEvent.getPath();
		Watcher.Event.EventType type = watchedEvent.getType();
		CuratorFramework curatorClient = Factory.getCuratorClient();
		switch (type) {
			case NodeDataChanged:
				byte[] bytes = curatorClient.getData().forPath(path);
				String processId = path.substring(path.lastIndexOf("/") + 1);
				String action = new String(bytes);
				if (action.equalsIgnoreCase("CANCEL")) {
					ProcessContext processContext = Factory.getGfacContext().getProcess(processId);
					if (processContext != null) {
						processContext.setCancel(true);
						log.info("procesId : {}, Cancelling process", processId);
					} else {
						log.info("Cancel request came for processId {} but couldn't find process context");
					}
				} else {
					curatorClient.getData().usingWatcher(this).forPath(path);
				}
				break;
			case NodeCreated:
			case NodeChildrenChanged:
			case NodeDeleted:
				curatorClient.getData().usingWatcher(this).forPath(path);
				break;
			default:
				curatorClient.getData().usingWatcher(this).forPath(path);
				break;
		}
	}
}
