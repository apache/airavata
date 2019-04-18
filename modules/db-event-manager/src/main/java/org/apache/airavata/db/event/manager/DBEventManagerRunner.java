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
package org.apache.airavata.db.event.manager;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.db.event.manager.messaging.DBEventManagerMessagingFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by Ajinkya on 3/29/17.
 */
public class DBEventManagerRunner implements IServer {

    private static final Logger log = LogManager.getLogger(DBEventManagerRunner.class);

    private static final String SERVER_NAME = "DB Event Manager";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    /**
     * Start required messaging utilities
     */
    private void startDBEventManagerRunner() {
        try{
            log.info("Starting DB Event manager publisher");

            DBEventManagerMessagingFactory.getDBEventPublisher();
            log.debug("DB Event manager publisher is running");

            log.info("Starting DB Event manager subscriber");

            DBEventManagerMessagingFactory.getDBEventSubscriber();
            log.debug("DB Event manager subscriber is listening");
        } catch (AiravataException e) {
            log.error("Error starting DB Event Manager.", e);
        }
    }


    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    DBEventManagerRunner dBEventManagerRunner = new DBEventManagerRunner();
                    dBEventManagerRunner.startDBEventManagerRunner();
                }
            };

            // start the worker thread
            log.info("Starting the DB Event Manager runner.");
            new Thread(runner).start();
        } catch (Exception ex) {
            log.error("Something went wrong with the DB Event Manager runner. Error: " + ex, ex);
        }
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    @Override
    public void start() throws Exception {

        try {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    DBEventManagerRunner dBEventManagerRunner = new DBEventManagerRunner();
                    dBEventManagerRunner.startDBEventManagerRunner();
                }
            };

            // start the worker thread
            log.info("Starting the DB Event Manager runner.");
            new Thread(runner).start();
            setStatus(ServerStatus.STARTED);
        } catch (Exception ex) {
            log.error("Something went wrong with the DB Event Manager runner. Error: " + ex, ex);
            setStatus(ServerStatus.FAILED);
        }
    }

    @Override
    public void stop() throws Exception {

        // TODO: implement stopping the DBEventManager
    }

    @Override
    public void restart() throws Exception {

        stop();
        start();
    }

    @Override
    public void configure() throws Exception {

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(ServerStatus stat){
        status=stat;
        status.updateTime();
    }
}
