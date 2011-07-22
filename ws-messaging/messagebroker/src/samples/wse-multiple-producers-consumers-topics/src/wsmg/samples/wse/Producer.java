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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.axiom.om.OMElement;

import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class Producer extends Thread {

	private Properties configurations;
	private String topicExpression;

	private Random random;

	public Producer(String producerId, String topic, Properties config) {
		super(producerId);
		configurations = config;
		topicExpression = topic;
		random = new Random();
	}

	public void run() {

		System.out.println(String
				.format("producer [%s] starting...", getName()));

		String brokerLocation = configurations
				.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);

		int timeIntervalUpperLimit = Integer.parseInt(configurations
				.getProperty(ConfigKeys.PUBLISH_TIME_INTERVAL));

		WseMsgBrokerClient client = new WseMsgBrokerClient();
		client.init(brokerLocation);

		try {

			long count = 0;
			while (true) {
				UUID uuid = UUID.randomUUID();
				count++;
				OMElement omMsg = MsgUtil.createMsg(count, getName(), uuid
						.toString());
				//MsgUtil.print("seding msg", omMsg);
				client.publish(topicExpression, omMsg);

				TimeUnit.SECONDS
						.sleep(getRandomSleepTime(timeIntervalUpperLimit));
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("interruped");
		} catch (MsgBrokerClientException f) {
			f.printStackTrace();
			System.out.println("unable to publish messages - producer will stop.");
		}
	}

	private int getRandomSleepTime(int upperLimit) {

		int rnd = random.nextInt() % upperLimit;

		return rnd < 2 ? 2 : rnd;
	}

}
