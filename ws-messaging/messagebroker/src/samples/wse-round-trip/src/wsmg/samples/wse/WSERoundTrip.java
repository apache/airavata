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
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class WSERoundTrip {

	private static Properties getDefaults() {

		Properties defaults = new Properties();
		defaults.setProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR,
				"http://localhost:8080/axis2/services/EventingService");
		defaults.setProperty(ConfigKeys.TOPIC_SIMPLE, "simpleSampleTopic");
		defaults.setProperty(ConfigKeys.CONSUMER_PORT, "2222");
		defaults.setProperty(ConfigKeys.PUBLISH_TIME_INTERVAL, "5");
		return defaults;
	}

	public static void main(String[] args) throws InterruptedException {

		Properties configurations = new Properties(getDefaults());
		try {
            InputStream ioStream = new FileInputStream("conf" + File.separator + ConfigKeys.CONFIG_FILE_NAME);
            configurations.load(ioStream);

		} catch (IOException ioe) {

			System.out
					.println("unable to load configuration file, default will be used.");
		}

		Producer producer = new Producer("simple topic", configurations);

		int consumerPort = Integer.parseInt(configurations
				.getProperty(ConfigKeys.CONSUMER_PORT));
		Consumer consumer = new Consumer("topic consumer", consumerPort,
				configurations);

		consumer.start();
		producer.start();

		consumer.join();
		producer.join();

	}
}
