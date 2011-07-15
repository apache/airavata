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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class MultipleProducersConsumers {

	private static Properties getDefaults() {

		Properties defaults = new Properties();
		defaults.setProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR,
				"http://localhost:8080/axis2/services/EventingService");
		defaults.setProperty(ConfigKeys.TOPIC_XPATH, "/msg/src");
		defaults.setProperty(ConfigKeys.CONSUMER_PORT_OFFSET, "2222");

		defaults.setProperty(ConfigKeys.PUBLISH_TIME_INTERVAL, "5");
		defaults.setProperty(ConfigKeys.PRODUCER_COUNT, "2");
		defaults.setProperty(ConfigKeys.CONSUMER_COUNT, "3");

		return defaults;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		Properties configurations = new Properties(getDefaults());
		try {

			InputStream ioStream = new FileInputStream("conf" + File.separator + ConfigKeys.CONFIG_FILE_NAME);
			configurations.load(ioStream);
		} catch (IOException ioe) {
			
			System.out.println("unable to load configuration file, default settings will be used");
		}

		int numberOfProducers = Integer.parseInt(configurations
				.getProperty(ConfigKeys.PRODUCER_COUNT));

		int numberOfConsumers = Integer.parseInt(configurations
				.getProperty(ConfigKeys.CONSUMER_COUNT));

		Consumer[] consumers = new Consumer[numberOfConsumers];
		int portOffset = Integer.parseInt(configurations
				.getProperty(ConfigKeys.CONSUMER_PORT_OFFSET));
		for (int i = 0; i < consumers.length; i++) {
			consumers[i] = new Consumer("consumer_" + i, portOffset + i,
					configurations);
			consumers[i].start();
		}

		Producer[] producers = new Producer[numberOfProducers];
		for (int i = 0; i < producers.length; i++) {
			producers[i] = new Producer("producer_" + i, configurations);
			producers[i].start();
		}

		for (Consumer c : consumers) {
			c.join();
		}

		for (Producer p : producers) {
			p.join();
		}

	}
}
