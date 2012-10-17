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

package org.apache.airavata.wsmg.commons;

public class WsmgCommonConstants {

    public static final String AXIS_MODULE_NAME_ADDRESSING = "addressing";

    public final static String VERSION = WsmgVersion.getSpecVersion(); // "0.14";

    public final static String UTF8 = "UTF-8";

    public final static String TOPIC_PREFIX = "topic/";

    public final static String PREFIX = "" + System.currentTimeMillis();

    public final static String SUBSCRIPTION_POLICY = "SubscriptionPolicy";

    public final static String SUBSCRIPTION_ID = "Identifier";

    public final static String WILDCARD_TOPIC = ">";

    public final static String TOPIC_EXPRESSION_SIMPLE_DIALECT = "http://www.ibm.com/xmlns/stdwip/web-services/WS-Topics/TopicExpression/simple";
    public final static String XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public final static String TOPIC_AND_XPATH_DIALECT = "http://docs.oasis-open.org/wsn";

    public static final String WSMG_PLAIN_TEXT_WRAPPER = "plainTextWrapper";

    public static final int WSRM_POLICY_TRUE = 1;

    public static final int WSRM_POLICY_FALSE = 0;

    public static final String BROKER_SERVICE_LOCATION = "broker.service.location";

    public static final String BROKER_WSMGCONFIG = "broker.wsmgconfig";

    public static final String BROKER_INITED = "broker.inited";

    public static final String EPR_SOURCE_HELPER_TO_OM_GET_EPR_PROPERTIES = "http://www.w3.org/2005/08/addressing";

    public final static String WSMG_PUBLISH_SOAP_ACTION = "http://org.apache.airavata/WseNotification";

    public static final String STORAGE_TYPE_IN_MEMORY = "memory";
    public static final String STORAGE_TYPE_PERSISTANT = "persistent";

    public static final String DELIVERY_METHOD_THREAD_CREW = "pcrew";
    public static final String DELIVERY_METHOD_PARALLEL = "parallel";
    public static final String DELIVERY_METHOD_SERIAL = "serial";

    public static final int DEFAULT_SENDING_BATCH_SIZE = 10;
    public static final int DEFAULT_SENDING_THREAD_POOL_SIZE = 4;

    public static final long DEFAULT_CLIENT_SOCKET_TIME_OUT_MILLIES = 300000L;
    public static final int DEFAULT_SUBSCRIPTION_EXPIRATION_TIME = 1000 * 60 * 60 * 72; // 72 hours

    public static final String CONFIGURATION_FILE_NAME = "configuration.file.name";
    public static final String CONFIG_JDBC_URL = "broker.jdbc.url";
    public static final String CONFIG_JDBC_DRIVER = "broker.jdbc.driver";
    public static final String CONFIG_START_DELIVERY_THREADS = "broker.start.delivery.thread";
    public static final String CONFIG_DELIVERY_METHOD = "broker.delivery.method";
    public static final String CONFIG_STORAGE_TYPE = "broker.storage.type";
    public static final String CONFIG_SOCKET_TIME_OUT = "broker.socket.timeout";
    public static final String CONFIG_MAX_MESSAGE_DELIVER_RETRIES = "broker.msg.delivery.retries";
    public static final String CONFIG_AXIS2_REPO = "axis2.repository";
    public static final String CONFIG_CONSUMER_URL_EXPIRATION_TIME_GAP = "consumer.expiration.time.gap";

    public static final String CONFIG_SENDING_BATCH_SIZE = "sending.batch.size";
    public static final String CONFIG_SENDING_THREAD_POOL_SIZE = "sending.thread.pool.size";

    public static final String BROKER_CONFIGURATION_FILE_NAME = "airavata-server.properties";

    public static final String DELIVERY_PROTOCOL = "broker.delivery.protocol";

}
