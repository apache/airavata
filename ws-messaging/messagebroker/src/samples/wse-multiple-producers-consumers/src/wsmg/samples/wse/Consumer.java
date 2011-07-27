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

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import com.sun.tools.doclets.internal.toolkit.MethodWriter;
import org.apache.airavata.wsmg.client.WseClientAPI;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class Consumer extends Thread {

	class NotificationMsgReciever implements ConsumerNotificationHandler {

		private BlockingQueue<SOAPEnvelope> queue = new LinkedBlockingQueue<SOAPEnvelope>();

		public void handleNotification(SOAPEnvelope msgEnvelope) {

			queue.add(msgEnvelope);
		}

		public BlockingQueue<SOAPEnvelope> getQueue() {
			return queue;
		}

	}

	private Properties configurations;
	private int consumerPort;

	public Consumer(String consumerName, int port, Properties config) {
		super(consumerName);
		consumerPort = port;
		configurations = config;
	}

	public void run() {

		String brokerLocation = configurations
				.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
		String xpathExpression = configurations
				.getProperty(ConfigKeys.TOPIC_XPATH);

		System.out.println("subscribing with xpath expression: "
				+ xpathExpression);

		NotificationMsgReciever msgReciever = new NotificationMsgReciever();

		String[] consumerEprs = null;

		String subscriptionId = null;

		WseMsgBrokerClient client = new WseMsgBrokerClient();
		client.init(brokerLocation);
		try {
			consumerEprs = client.startConsumerService(consumerPort,
					msgReciever);

		} catch (MsgBrokerClientException e) {

			e.printStackTrace();

			System.out.println("unable to start consumer service, exiting");
			return;
		}

		try {

			subscriptionId = client.subscribe(consumerEprs[0], null,
					xpathExpression);
			System.out.println(getName() + "got the subscription id :"
					+ subscriptionId);

		} catch (MsgBrokerClientException e) {

			e.printStackTrace();

			System.out
					.println("unable to subscribe for the xpath consumer exiting");
			return;
		}

		try {

			do {

				SOAPEnvelope env = msgReciever.getQueue().take();

				String msg;
				try {
					msg = env.getBody().getFirstElement().toStringWithConsume();
					System.out.println(String.format(
							"consumer [%s] recieved: %s", getName(), msg));

				} catch (Exception e) {
					System.err.print("invalid msg recieved");
				}

			} while (true);

		} catch (InterruptedException ie) {

			try {
				// unsubscribe from the topic.
				client.unSubscribe(subscriptionId);
				
			} catch (MsgBrokerClientException e) {

				e.printStackTrace();
				System.out.println("unable to unsubscribe, ignoring");
			}

			// shutdown the consumer service.
			client.shutdownConsumerService();

			System.out.println("interrupted");

		}

	}

}
