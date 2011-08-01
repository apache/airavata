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

import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class Producer extends Thread {

	private Properties configurations;

	private XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

	public Producer(String producerId, Properties config) {
		super(producerId);
		configurations = config;
	}

	private OMElement buildMsg(String msg) {

		XMLStreamReader inflow = null;
		try {
			inflow = xmlFactory.createXMLStreamReader(new StringReader(msg));
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}

		StAXOMBuilder builder = new StAXOMBuilder(inflow);
		OMElement omElement = builder.getDocumentElement();
		return omElement;
	}

	public void run() {

		System.out.println(String
				.format("producer [%s] starting...", getName()));

		String brokerLocation = configurations
				.getProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR);
		String topicExpression = configurations
				.getProperty(ConfigKeys.TOPIC_SIMPLE);

		int timeInterval = Integer.parseInt(configurations
				.getProperty(ConfigKeys.PUBLISH_TIME_INTERVAL));

		WseMsgBrokerClient client = new WseMsgBrokerClient();
		client.init(brokerLocation);

		String msgFormat = "<msg><seq>%d</seq><src>%s</src><uuid>%s</uuid></msg>";

		try {

			int count = 0;
			while (true) {
				UUID uuid = UUID.randomUUID();
				count++;
				String msg = String.format(msgFormat, count, getName(), uuid
						.toString());
				System.out.println(String.format(
						"producer [%s] sending msg: %s", getName(), msg));
				client.publish(topicExpression, buildMsg(msg));
				TimeUnit.SECONDS.sleep(timeInterval);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("interruped");
		} catch (MsgBrokerClientException f) {
			f.printStackTrace();
			System.out
					.println("unable to publish messages - producer will stop.");

		}
	}

}
