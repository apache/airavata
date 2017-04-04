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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.messaging.core;
//
//import org.apache.airavata.common.exception.AiravataException;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class PublisherFactory {
//    private static Logger log = LoggerFactory.getLogger(PublisherFactory.class);
//
//    public static Publisher createActivityPublisher() throws AiravataException {
//        String activityPublisher = ServerSettings.getStatusPublisher();
//
//        if (activityPublisher == null) {
//            String s = "Activity publisher is not specified";
//            log.error(s);
//            throw new AiravataException(s);
//        }
//
//        try {
//            Class<? extends Publisher> aPublisher = Class.forName(activityPublisher).asSubclass(Publisher.class);
//            return aPublisher.newInstance();
//        } catch (Exception e) {
//            String msg = "Failed to load the publisher from the publisher class property: " + activityPublisher;
//            log.error(msg, e);
//            throw new AiravataException(msg, e);
//        }
//    }
//
//    public static Publisher createTaskLaunchPublisher() throws AiravataException {
//        String taskLaunchPublisher = ServerSettings.getTaskLaunchPublisher();
//
//        if (taskLaunchPublisher == null) {
//            String s = "Task launch publisher is not specified";
//            log.error(s);
//            throw new AiravataException(s);
//        }
//
//        try {
//            Class<? extends Publisher> aPublisher = Class.forName(taskLaunchPublisher).asSubclass(Publisher.class);
//            return aPublisher.newInstance();
//        } catch (Exception e) {
//            String msg = "Failed to load the publisher from the publisher class property: " + taskLaunchPublisher;
//            log.error(msg, e);
//            throw new AiravataException(msg, e);
//        }
//    }
//}
