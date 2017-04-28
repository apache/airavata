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

import org.apache.airavata.common.utils.ZkConstants;
import org.apache.airavata.gfac.core.GFacException;
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
	private final String processId;
	private final String experimentId;
	private final int max_retry = 3;

	public CancelRequestWatcherImpl(String experimentId, String processId) {
		this.experimentId = experimentId;
		this.processId = processId;
	}

	@Override
	public void process(WatchedEvent watchedEvent) throws Exception {
		// this watcher change data in cancel listener node in the experiment node
		String path = watchedEvent.getPath();
		Watcher.Event.EventType type = watchedEvent.getType();
		CuratorFramework curatorClient = Factory.getCuratorClient();
		log.info("cancel watcher triggered process id {}.", processId);
		switch (type) {
			case NodeDataChanged:
				byte[] bytes = curatorClient.getData().forPath(path);
				String action = new String(bytes);
				if (action.equalsIgnoreCase(ZkConstants.ZOOKEEPER_CANCEL_REQEUST)) {
					cancelProcess(0);
				} else {
					curatorClient.getData().usingWatcher(this).forPath(path);
				}
				break;
			case NodeDeleted:
				//end of experiment execution, ignore this event
				log.info("expId: {}, cancel watcher trigger for process {} with event type {}", experimentId,
						processId, type.name());
				break;
			case NodeCreated:
			case NodeChildrenChanged:
			case None:
				log.info("expId: {}, Cancel watcher trigger for process {} with event type {}", experimentId,
						processId, type.name());
				if (path != null) {
					curatorClient.getData().usingWatcher(this).forPath(path);
				}
				break;
			default:
				log.info("expId: {}, Cancel watcher trigger for process {} with event type {}", experimentId,
						processId, type.name());
				if (path != null) {
					curatorClient.getData().usingWatcher(this).forPath(path);
				}
				break;
		}
	}

	private void cancelProcess(int retryAttempt) throws GFacException {
		ProcessContext processContext = Factory.getGfacContext().getProcess(processId);
		if (processContext != null) {
            processContext.setCancel(true);
			log.info("expId {}, processId : {}, Cancelling process", experimentId, processId);
			Factory.getGFacEngine().cancelProcess(processContext);
        } else {
			if (retryAttempt < max_retry) {
				log.info("expId: {}, Cancel request came for processId {} but couldn't find process context. " +
						"retry in {} s ", experimentId, processId, retryAttempt*3);
				try {
					Thread.sleep(retryAttempt++*3000);
				} catch (InterruptedException e) {
					// ignore we don't care this exception.
				}
				cancelProcess(retryAttempt);
			} else {
				log.info("expId: {}, Cancel request came for processId {} but couldn't find process context.",
						experimentId, processId);
			}
		}
	}
}
