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

package org.apache.airavata.wsmg.messenger.util;

import javax.servlet.ServletConfig;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public final class MessengerLogFactory {

    private static final String WSMG_MESSENGER_LOGGER = "org.apache.airavata.wsmg.messenger.logger";

    private static final String LOG4J_PROPERTIES_NAME = "log4jconfigpath";

    private Logger log = Logger.getLogger(WSMG_MESSENGER_LOGGER);

    private static MessengerLogFactory instance = new MessengerLogFactory();

    private MessengerLogFactory() {

    }

    public static MessengerLogFactory getIntance() {
        return instance;

    }

    public Logger getLogger() {
        return log;
    }

    public void init(ServletConfig config) {

        System.out.println("configuring log4j");

        String prefix = config.getServletContext().getRealPath("/");
        String file = config.getInitParameter(LOG4J_PROPERTIES_NAME);

        if (file != null) {
            System.out.println("log4j config file: " + file);
            if (file.endsWith(".xml")) {
                System.out.println("configuring dom");
                DOMConfigurator.configure(prefix + file);
            } else {
                PropertyConfigurator.configure(prefix + file);
            }
        } else {
            System.out.println("Warning :- logging configuration doesn't exist -" + " using basic configuration");
            BasicConfigurator.configure();
        }

    }
}
