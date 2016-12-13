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

import org.apache.airavata.cloud.aurora.client.sdk.AuroraSchedulerManager;
import org.apache.airavata.cloud.aurora.client.sdk.ReadOnlyScheduler;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating AuroraSchedulerClient objects.
 */
public class AuroraSchedulerClientFactory {
	
	/** The Constant logger. */
	private final static Logger logger = LoggerFactory.getLogger(AuroraSchedulerClientFactory.class);
	
	/**
	 * Creates a new AuroraSchedulerClient object.
	 *
	 * @param connectionUrl the connection url
	 * @return the client
	 * @throws Exception the exception
	 */
	public static ReadOnlyScheduler.Client createReadOnlySchedulerClient(String connectionUrl) throws Exception {
		try {
			TTransport transport = new THttpClient(connectionUrl);
			transport.open();
			TProtocol protocol = new TJSONProtocol(transport);
			return new ReadOnlyScheduler.Client(protocol);
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}
	
	public static AuroraSchedulerManager.Client createSchedulerManagerClient(String connectionUrl) throws Exception {
		try {
			TTransport transport = new THttpClient(connectionUrl);
			transport.open();
			TProtocol protocol = new TJSONProtocol(transport);
			return new AuroraSchedulerManager.Client(protocol);
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}
}
