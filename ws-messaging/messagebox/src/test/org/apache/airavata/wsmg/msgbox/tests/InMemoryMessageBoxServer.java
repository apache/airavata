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

package org.apache.airavata.wsmg.msgbox.tests;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.msgbox.MsgBoxServiceLifeCycle;
import org.apache.airavata.wsmg.msgbox.MsgBoxServiceMessageReceiverInOut;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class InMemoryMessageBoxServer {
    private static int count = 0;

    private static SimpleHTTPServer receiver;

    public static final int TESTING_PORT = 5555;

    public static final String FAILURE_MESSAGE = "Intentional Failure";

    public static synchronized void deployService(AxisService service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().addService(service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().removeService(service.getLocalPart());
    }

    public static synchronized void unDeployClientService() throws AxisFault {
        if (receiver.getConfigurationContext().getAxisConfiguration() != null) {
            receiver.getConfigurationContext().getAxisConfiguration().removeService("AnonymousService");
        }
    }

    public static synchronized void start() throws Exception {
        start(prefixBaseDirectory(Constants.TESTING_REPOSITORY));
    }

    public static synchronized void start(String repository) throws Exception {
        if (count == 0) {
            ConfigurationContext er = getNewConfigurationContext(repository);

            receiver = new SimpleHTTPServer(er, TESTING_PORT);

            try {
                receiver.start();
                ListenerManager listenerManager = er.getListenerManager();
                TransportInDescription trsIn = new TransportInDescription(Constants.TRANSPORT_HTTP);
                trsIn.setReceiver(receiver);
                if (listenerManager == null) {
                    listenerManager = new ListenerManager();
                    listenerManager.init(er);
                }
                listenerManager.addListener(trsIn, true);
                System.out.print("Server started on port " + TESTING_PORT + ".....");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            throw new AxisFault("Thread interuptted", e1);
        }

        count++;
    }

    public static synchronized void start(String repository, String axis2xml) throws Exception {
        if (count == 0) {
            ConfigurationContext er = getNewConfigurationContext(repository, axis2xml);

            receiver = new SimpleHTTPServer(er, TESTING_PORT);

            try {
                receiver.start();
                System.out.print("Server started on port " + TESTING_PORT + ".....");
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }
            startMessageBox();
        }
        count++;
    }

    public static void startMessageBox() throws Exception {

        AxisService axisService = new AxisService("MsgBoxService");
        axisService.addParameter("configuration.file.name", "wsmg.broker.properties");
        axisService.setServiceLifeCycle(new MsgBoxServiceLifeCycle());

        createOperation(axisService, "storeMessages", new MsgBoxServiceMessageReceiverInOut(),
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/storeMessages",
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/MsgBoxPT/storeMessagesResponse");
        createOperation(axisService, "destroyMsgBox", new MsgBoxServiceMessageReceiverInOut(),
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/destroyMsgBox",
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/MsgBoxPT/destroyMsgBoxResponse");
        createOperation(axisService, "takeMessages", new MsgBoxServiceMessageReceiverInOut(),
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/takeMessages",
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/MsgBoxPT/takeMessagesResponse");
        createOperation(axisService, "createMsgBox", new MsgBoxServiceMessageReceiverInOut(),
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/createMsgBox",
                "http://www.extreme.indiana.edu/xgws/msgbox/2004/MsgBoxPT/createMsgBoxResponse");
        axisService.addParameter("configuration.file.name", "msgBox.properties");
        axisService.addParameter("ServiceClass", "edu.indiana.extreme.www.xgws.msgbox.MsgBoxServiceSkeleton");

        InMemoryMessageBoxServer.deployService(axisService);

        new MsgBoxServiceLifeCycle().startUp(InMemoryMessageBoxServer.getConfigurationContext(), axisService);

    }

    public static void createOperation(AxisService axisService, String name, MessageReceiver messageReceiver,
            String inputAction, String outputAction) {
        InOutAxisOperation operation1 = new InOutAxisOperation(new QName(name));
        operation1.setMessageReceiver(messageReceiver);
        operation1.setOutputAction(outputAction);
        axisService.addOperation(operation1);
        if (inputAction != null) {
            axisService.mapActionToOperation(inputAction, operation1);
        }
    }

    public static ConfigurationContext getNewConfigurationContext(String repository) throws Exception {
        if (repository != null) {
            return ConfigurationContextFactory.createConfigurationContextFromFileSystem(repository, repository
                    + "/conf/axis2.xml");
        } else {
            return ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        }
    }

    public static ConfigurationContext getNewConfigurationContext(String repository, String axis2xml) throws Exception {
        // File file = new File(prefixBaseDirectory(repository));
        // if (!file.exists()) {
        // throw new Exception("repository directory "
        // + file.getAbsolutePath() + " does not exists");
        // }
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(repository, axis2xml);
    }

    public static synchronized void stop() throws AxisFault {
        if (count == 1) {
            receiver.stop();
            while (receiver.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // nothing to do here
                }
            }
            count = 0;
            // tp.doStop();
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
        receiver.getConfigurationContext().terminate();
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getConfigurationContext();
    }

    public static ServiceContext createAdressedEnabledClientSide(AxisService service) throws AxisFault {
        File file = getAddressingMARFile();
        TestCase.assertTrue(file.exists());
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                prefixBaseDirectory("target/test-resources/integrationRepo"), null);
        AxisModule axisModule = DeploymentEngine.buildModule(file, configContext.getAxisConfiguration());
        configContext.getAxisConfiguration().addModule(axisModule);

        configContext.getAxisConfiguration().addService(service);

        ServiceGroupContext serviceGroupContext = configContext
                .createServiceGroupContext(service.getAxisServiceGroup());
        return serviceGroupContext.getServiceContext(service);
    }

    static class AddressingFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.startsWith("addressing") && name.endsWith(".mar");
        }
    }

    private static File getAddressingMARFile() {
        File dir = new File(prefixBaseDirectory(Constants.TESTING_REPOSITORY + "/modules"));
        File[] files = dir.listFiles(new AddressingFilter());
        TestCase.assertTrue(files.length == 1);
        File file = files[0];
        TestCase.assertTrue(file.exists());
        return file;
    }

    public static ConfigurationContext createClientConfigurationContext() throws AxisFault {
        File file = getAddressingMARFile();
        TestCase.assertTrue(file.exists());

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                prefixBaseDirectory(Constants.TESTING_PATH + "/integrationRepo"),
                prefixBaseDirectory(Constants.TESTING_PATH + "/integrationRepo/conf/axis2.xml"));
        AxisModule axisModule = DeploymentEngine.buildModule(file, configContext.getAxisConfiguration());
        configContext.getAxisConfiguration().addModule(axisModule);
        return configContext;
    }

    public static ConfigurationContext createClientConfigurationContext(String repo) throws AxisFault {
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo, repo + "/conf/axis2.xml");
    }

    public static ServiceContext createAdressedEnabledClientSide(AxisService service, String clientHome)
            throws AxisFault {
        File file = getAddressingMARFile();
        TestCase.assertTrue(file.exists());

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                clientHome, null);
        AxisModule axisModule = DeploymentEngine.buildModule(file, configContext.getAxisConfiguration());

        configContext.getAxisConfiguration().addModule(axisModule);
        // sysContext.getAxisConfiguration().engageModule(moduleDesc.getName());

        configContext.getAxisConfiguration().addService(service);
        ServiceGroupContext serviceGroupContext = configContext
                .createServiceGroupContext(service.getAxisServiceGroup());
        return serviceGroupContext.getServiceContext(service);
    }

    public static String prefixBaseDirectory(String path) {
        // String baseDir;
        // try {
        // baseDir = new File(System.getProperty("basedir", ".")).getCanonicalPath();
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // }
        // return baseDir + "/" + path;
        return path;
    }

}
