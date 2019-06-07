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
package org.apache.airavata.common.utils;

import org.apache.airavata.model.dbevent.EntityType;

/**
 * Created by Ajinkya on 3/22/17.
 */
public class DBEventManagerConstants {

    private final static String QUEUE_SUFFIX = ".queue";
    public final static String DB_EVENT_EXCHANGE_NAME = "db.event.exchange";
    public final static String ROUTING_KEY_SEPARATOR = ".";

    /**
     * Get the queue-name of the service, given service-name as enum
     * @param dBEventService
     * @return
     */
    public static String getQueueName(DBEventService dBEventService) {
        return dBEventService.toString() + QUEUE_SUFFIX;
    }

    /**
     * Return routing key which is capable of consuming any message published with serviceName in it.
     * For example: let' say serviceName is 'hello' function will return #.hello.#
     * This queue can consume message with any of these routing keys 'q1.12.hello.q3', 'q1.hello', 'hello.q2' or just 'hello'
     * It just need to have 'hello' in it.
     * @param serviceName
     * @return
     */
    public static String getRoutingKey(String serviceName) {
        if(serviceName.equals(DBEventService.DB_EVENT.toString())) {
            return serviceName;
        }
        return "#" + ROUTING_KEY_SEPARATOR + serviceName + ROUTING_KEY_SEPARATOR + "#";
    }

    /**
     * Get the queue-name of the service, given service-name as string
     * @param dbEventService
     * @return
     */
    public static String getQueueName(String dbEventService) {
        return getQueueName(getDBEventService(dbEventService));
    }

    /**
     * Get the service as enum, given the service-name as string
     * @param dbEventService
     * @return
     */
    private static DBEventService getDBEventService(String dbEventService) {
        for (DBEventService service : DBEventService.values()) {
            if (service.toString().equals(dbEventService)) {
                return service;
            }
        }
        return null;
    }

//    public static void main(String[] args) {
//        System.out.println(DBEventManagerConstants.getDbEventServiceName(EntityType.USER_PROFILE));
//        System.out.println(DBEventService.REGISTRY);
//    }
}
