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

package org.apache.airavata.wsmg.samples.util;

public interface ConfigKeys {

	String CONFIG_FILE_NAME = "configurations.properties";
	
	String BROKER_EVENTING_SERVICE_EPR = "broker.eventing.service.epr";
	String BROKER_NOTIFICATIONS_SERVICE_EPR = "broker.notification.service.epr";

	String CONSUMER_EPR = "consumer.location";
	String CONSUMER_PORT = "consumer.port";
	String TOPIC_SIMPLE = "topic.simple";
	String TOPIC_XPATH = "topic.xpath";
	String AXIS2_REPO = "axis2.repo";
	String PUBLISH_TIME_INTERVAL = "publish.time.interval";
	
}
