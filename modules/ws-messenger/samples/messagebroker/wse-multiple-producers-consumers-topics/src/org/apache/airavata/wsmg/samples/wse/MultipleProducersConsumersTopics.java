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

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.airavata.wsmg.samples.util.ConfigKeys;

public class MultipleProducersConsumersTopics {

	static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static Properties getDefaults() {

		Properties defaults = new Properties();
		defaults.setProperty(ConfigKeys.BROKER_EVENTING_SERVICE_EPR,
				"http://localhost:8080/axis2/services/EventingService");
		defaults.setProperty(ConfigKeys.CONSUMER_PORT_OFFSET, "2222");

		defaults.setProperty(ConfigKeys.PUBLISH_TIME_INTERVAL, "5");
		defaults.setProperty(ConfigKeys.PRODUCER_COUNT_PER_TOPIC, "2");
		defaults.setProperty(ConfigKeys.CONSUMER_COUNT_PER_TOPIC, "3");
		defaults.setProperty(ConfigKeys.NUMBER_OF_TOPICS, "5");
		defaults.setProperty(ConfigKeys.TOPIC_PREFIX, "topic_prefix_");
		defaults.setProperty(ConfigKeys.LOG_FILE_PATH, "stats.log");

		return defaults;
	}

	public static void main(String[] args) throws InterruptedException {

		Properties configurations = new Properties(getDefaults());
		try {
			InputStream ioStream = new FileInputStream(ConfigKeys.CONFIG_FILE_NAME);
			configurations.load(ioStream);
		} catch (IOException ioe) {

			System.out.println("unable to load configuration file, "
					+ "default settings will be used");
		}

		int numberOfProducers = Integer.parseInt(configurations
				.getProperty(ConfigKeys.PRODUCER_COUNT_PER_TOPIC));

		int numberOfConsumers = Integer.parseInt(configurations
				.getProperty(ConfigKeys.CONSUMER_COUNT_PER_TOPIC));

		int numberOfTopics = Integer.parseInt(configurations
				.getProperty(ConfigKeys.NUMBER_OF_TOPICS));

		String topicPrefix = configurations
				.getProperty(ConfigKeys.TOPIC_PREFIX);

		int portOffset = Integer.parseInt(configurations
				.getProperty(ConfigKeys.CONSUMER_PORT_OFFSET));

		List<List<Consumer>> consumers = new ArrayList<List<Consumer>>();
		List<List<Producer>> producers = new ArrayList<List<Producer>>();

		for (int i = 0; i < numberOfTopics; i++) {
			String topic = topicPrefix + i;
			consumers.add(createConsumers(numberOfConsumers, portOffset
					+ (i * numberOfConsumers), topic, configurations));

			TimeUnit.SECONDS.sleep(1);

			producers.add(createProducers(numberOfProducers, topic,
					configurations));
		}

		PrintStream printStream = null;
		try {
			FileOutputStream outputStream = new FileOutputStream(configurations
					.getProperty(ConfigKeys.LOG_FILE_PATH), true);

			printStream = new PrintStream(outputStream, true);

		} catch (FileNotFoundException e) {
			System.out
					.println("unable to open the file - stats will be printed to console");
			printStream = System.out;
		}

		while (true) {

			Date date = new Date();
			printStream.println("---- statistics at : ["
					+ dateFormat.format(date) + "]------");
			for (List<Consumer> l : consumers) {

				for (Consumer c : l) {
					printStream.println(c.getName() + " latest seq: "
							+ c.getLatestSeq());

				}
			}

			TimeUnit.SECONDS.sleep(5);
		}

	}

	private static List<Consumer> createConsumers(int number, int portOffset,
			String topic, Properties config) {

		List<Consumer> ret = new ArrayList<Consumer>();

		for (int i = 0; i < number; i++) {

			int port = portOffset + i;
			Consumer c = new Consumer("consumer_" + port, port, topic, config);
			c.start();
			ret.add(c);
		}

		return ret;
	}

	private static List<Producer> createProducers(int number, String topic,
			Properties config) {

		List<Producer> ret = new ArrayList<Producer>();

		for (int i = 0; i < number; i++) {
			Producer p = new Producer(
					String.format("producer_%s_%d", topic, i), topic, config);
			p.start();
			ret.add(p);
		}

		return ret;
	}

}
