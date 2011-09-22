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

package org.apache.airavata.wsmg.messenger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgVersion;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.commons.storage.WsmgPersistantStorage;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.airavata.wsmg.messenger.strategy.impl.FixedParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.ParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.SerialSender;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessengerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MessengerServlet.class);

    private static final long serialVersionUID = -7175511030332798604L;

    private SendingStrategy sender = null;

    public MessengerServlet() {
    }

    public void init(ServletConfig config) throws ServletException {
        logger.info("Starting messenger servlet");
        System.out.println(String.format("Starting messenger: version: %s , %s",
                WsmgVersion.getImplementationVersion(), WSMGParameter.versionSetUpNote));
        RunTimeStatistics.setStartUpTime();
        ConfigurationManager brokerConfig = initConfigurations(config);
        initStorage(brokerConfig);
        initDiliveryMethod(brokerConfig);

    }

    private void initDiliveryMethod(ConfigurationManager config) {
        logger.info("starting dilivery thread");

        String deliveryMethod = config.getConfig(WsmgCommonConstants.CONFIG_DELIVERY_METHOD,
                WsmgCommonConstants.DELIVERY_METHOD_SERIAL);
        SendingStrategy method = null;

        ConsumerUrlManager urlManager = new ConsumerUrlManager(config);

        String initedmethod = null;

        if (WsmgCommonConstants.DELIVERY_METHOD_PARALLEL.equalsIgnoreCase(deliveryMethod)) {

            method = new ParallelSender(config, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_PARALLEL;

        } else if (WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW.equalsIgnoreCase(deliveryMethod)) {
            method = new FixedParallelSender(config, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW;

        } else {
            method = new SerialSender(config, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_SERIAL;
        }

        method.start();
        logger.info(initedmethod + " sending method inited");
    }

    public void destroy() {
        sender.shutdown();
        logger.info("stoping wsmg-messenger");
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public String getServletInfo() {
        return null;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendMessengerStats(response);

    }

    private void sendMessengerStats(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        out.write("<html><head><title>Delivery thread" + " of WS-Messenger</title></head>");
        out.write("<body bgcolor='white'><h1>" + "Delivery thread of WS-Messenger is running</h1>");
        out.write(RunTimeStatistics.getHtmlString());
        out.write("</body>");
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        sendMessengerStats(response);
    }

    private ConfigurationManager initConfigurations(ServletConfig config) {
        ConfigurationManager configMan = new ConfigurationManager(WsmgCommonConstants.BROKER_CONFIGURATION_FILE_NAME);
        return configMan;
    }

    private void initStorage(ConfigurationManager configMan) {

        String type = configMan.getConfig(WsmgCommonConstants.CONFIG_STORAGE_TYPE,
                WsmgCommonConstants.CONFIG_STORAGE_TYPE);

        if (WsmgCommonConstants.STORAGE_TYPE_IN_MEMORY.equalsIgnoreCase(type)) {
            logger.error("invalid storage type specified: " + type);
            throw new RuntimeException("invalid storage type specified: " + type);
        }

        String jdbcUrl = configMan.getConfig(WsmgCommonConstants.CONFIG_JDBC_URL);
        String jdbcDriver = configMan.getConfig(WsmgCommonConstants.CONFIG_JDBC_DRIVER);
        WSMGParameter.OUT_GOING_QUEUE = new WsmgPersistantStorage(jdbcUrl, jdbcDriver);
    }
}
