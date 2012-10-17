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

package org.apache.airavata.wsmg.samples.wse;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.io.*;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class XpathSubscribe {

	public static void main(String[] args) throws MsgBrokerClientException{

		Properties configurations = new Properties();

		try {
            InputStream ioStream = new FileInputStream(ConfigKeys.CONFIG_FILE_NAME);
            configurations.load(ioStream);
		} catch (IOException ioe) {

			System.out
					.println("unable to load configuration file, default will be used.");
		}

		String brokerLocation = configurations.getProperty(
				ConfigKeys.BROKER_EVENTING_SERVICE_EPR,
				"http://localhost:8080/axis2/services/EventingService");
		String xpathExpression = configurations.getProperty(
				ConfigKeys.TOPIC_XPATH, "/c/b/a");
		String consumerLocation = configurations.getProperty(
				ConfigKeys.CONSUMER_EPR,
				"http://localhost:2222/axis2/services/ConsumerService");

		WseMsgBrokerClient client = new WseMsgBrokerClient();
		client.init(brokerLocation);
		System.out.println("subscribing with xpath expression: "
				+ xpathExpression);

		// create a topic subscription.
		String subscriptionId = client.subscribe(consumerLocation, null,
				xpathExpression);

		System.out.println("xpath subscription id : " + subscriptionId);
	}
}
