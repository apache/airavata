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

package org.apache.airavata.wsmg.performance_evaluator.rtt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.airavata.wsmg.performance_evaluator.rtt.util.ConfigKeys;
import org.apache.airavata.wsmg.performance_evaluator.rtt.util.LoadMsgPayload;
import org.apache.airavata.wsmg.performance_evaluator.rtt.util.LoadXpath;

class Stat {
    String name;
    Object value;

    public Stat(String k, Object v) {
        name = k;
        value = v;
    }

}

public class PerformanceTest {

    public static int NOTIFICATIONS_PUBLISHED_PER_TOPIC = 0;
    static String payload = null;
    static LinkedList<String> xpathList = null;
    public static long totalRoundTripTime = 0;
    private static long avgRountTripTime = 0;
    public static BufferedWriter out = null;
    public static Properties configurations = null;
    public static long avgPublishRTTime = 0l;
    private static long totalPublishRTT = 0l;
    private static int notifPerTopic = 0;
    private static int noTopicsPublished = 0;
    private static String protocol = "";
    private static int payLoadMultiplier = 1;
    private static int consumerPort = 3345;
    private static long testExpirationTime = 0l;
    private static int numberOfSubscriber = 0;
    private static int numMultiThreadsSupportPerSub = 0;
    private static String topicPrefix = "";

    public static void main(String[] args) throws Exception {
        loadConfigurationsFromFile();
        testPerformance();
    }

    public static void testPerformance() throws Exception {

        setConfigurationValues();
        File outfile = new File("performance.log");
        CountDownLatch publiserhStartSignal = new CountDownLatch(1);
        CountDownLatch publisherDoneSignal = new CountDownLatch(noTopicsPublished);
        NotificationManager notifManagerArray[] = new NotificationManager[numberOfSubscriber];
        StatCalculatorThread statCalcThread[] = new StatCalculatorThread[numberOfSubscriber];
        setPayload(payLoadMultiplier);

        for (int j = 0; j < numberOfSubscriber; j++) {
            notifManagerArray[j] = new NotificationManager(configurations.getProperty(ConfigKeys.BROKER_URL),
                    consumerPort + j, protocol, numMultiThreadsSupportPerSub);

        }

        // thread to calculate stats for notification manager
        // set the subscriptions depending on the topic or xpath based
        int arrayIndex = 0;
        int totalReceivers = 0;
        createSubscriberArray(noTopicsPublished, numberOfSubscriber, notifManagerArray, arrayIndex);
        System.out.println("subscribing to topics completed, creating publisher threads");

        // start publishers
        PublisherThread[] publisher = new PublisherThread[noTopicsPublished];
        createPublishers(noTopicsPublished, protocol, publiserhStartSignal, publisherDoneSignal, publisher);
        System.out.println("sending signal to start publishing...");
        long publisherStartTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        publiserhStartSignal.countDown(); // let all threads proceed

        for (int j = 0; j < numberOfSubscriber; j++) {
            statCalcThread[j] = new StatCalculatorThread(notifManagerArray[j], testExpirationTime);
            statCalcThread[j].start();
        }

        publisherDoneSignal.await(); // wait for all to finish

        for (int j = 0; j < noTopicsPublished; j++) {
            totalPublishRTT += publisher[j].getAvgPubTime();
        }

        avgPublishRTTime = totalPublishRTT / noTopicsPublished;
        long publishersRunningTime = System.currentTimeMillis() - publisherStartTime;
        System.out.println("finished publishing messgaes.");

        for (StatCalculatorThread stats : statCalcThread) {
            stats.join();
        }

        long stopTime = 0l;
        long totNumberOfMessagesReceived = 0;

        for (StatCalculatorThread stats : statCalcThread) {
            stopTime = stopTime < stats.getLastMsgReceivedTime() ? stats.getLastMsgReceivedTime() : stopTime;
            totalRoundTripTime += stats.getTotalTime();
            totNumberOfMessagesReceived += stats.getNumberOfMsgReceived();
        }

        for (NotificationManager notifMngr : notifManagerArray) {
            totalReceivers += notifMngr.getNoTopicsSubscribed();
        }

        avgRountTripTime = totalRoundTripTime / totNumberOfMessagesReceived;
        long executionTime = stopTime - startTime;
        double throughtput = (totNumberOfMessagesReceived * 1000) / (executionTime);

        List<Stat> statistics = new ArrayList<Stat>();

        statistics.add(new Stat("Payload size (bytes)", payload.getBytes("US-ASCII").length));
        statistics.add(new Stat("Protocol", protocol));
        statistics.add(new Stat("# total expected Msgs", totalReceivers * notifPerTopic));
        statistics.add(new Stat("# total msgs received", totNumberOfMessagesReceived));
        setStatList(notifPerTopic, noTopicsPublished, publishersRunningTime, executionTime, throughtput, statistics);
        printStatistics(statistics, outfile);

        for (NotificationManager notifMngr : notifManagerArray) {
            notifMngr.cleanup();
        }

        System.out.println("end of test");
        System.exit(0);
    }

    private static void setConfigurationValues() {
        notifPerTopic = Integer.parseInt(configurations.getProperty(ConfigKeys.NOTIFICATIONS_PUBLISHED_PER_TOPIC));
        noTopicsPublished = Integer.parseInt(configurations.getProperty(ConfigKeys.NUMBER_OF_TOPICS_PUBLISHED));
        protocol = configurations.getProperty(ConfigKeys.PROTOCOL);
        payLoadMultiplier = Integer.parseInt(configurations.getProperty(ConfigKeys.PAYLOAD_MULTIPLYER));
        consumerPort = Integer.parseInt(configurations.getProperty(ConfigKeys.CONSUMER_PORT));
        testExpirationTime = Math.max(20000,
                Long.parseLong(configurations.getProperty(ConfigKeys.PERFORMANCE_TEST_TIMEOUT, "20000")));
        numberOfSubscriber = Integer.parseInt(configurations.getProperty(ConfigKeys.NUMBER_OF_SUBSCRIBERS));
        numMultiThreadsSupportPerSub = Integer.parseInt(configurations.getProperty(ConfigKeys.MULTI_THREAD_PER_SUB));
        topicPrefix = "topic" + configurations.getProperty(ConfigKeys.TOPIC_SIMPLE);
        NOTIFICATIONS_PUBLISHED_PER_TOPIC = notifPerTopic;
    }

    private static void setStatList(int notifPerTopic, int noTopicsPublished, long publishersRunningTime,
            long executionTime, double throughtput, List<Stat> statistics) {

        statistics.add(new Stat("# topics published", noTopicsPublished));
        statistics.add(new Stat("Total RTT (millis)", totalRoundTripTime));
        statistics.add(new Stat("Average RTT (millis)", avgRountTripTime));
        statistics.add(new Stat("Total published to receive time (millis)", executionTime));
        statistics.add(new Stat("Throughput (messages per second)", throughtput));
        statistics.add(new Stat("Total publish RTT (millis)", totalPublishRTT));
        statistics.add(new Stat("Average publish RTT (millis)", avgPublishRTTime));
        statistics.add(new Stat("publisher duration (millis)", publishersRunningTime));
        statistics.add(new Stat("Publisher throughput (messages per second)", noTopicsPublished * notifPerTopic * 1000
                / publishersRunningTime));
    }

    private static void setPayload(int payLoadMultiplier) {
        String tempPayload = "";
        try {
            tempPayload = LoadMsgPayload.getInstance().getMessage("payload.txt");
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= payLoadMultiplier; i++) {
            payload += tempPayload;
        }
    }

    private static void createPublishers(int noTopicsPublished, String protocol, CountDownLatch publiserhStartSignal,
            CountDownLatch publisherDoneSignal, PublisherThread[] publisher) {
        int threadId = 0;
        for (int j = 0; j < noTopicsPublished; j++) {
            threadId++;
            publisher[j] = new PublisherThread(protocol, configurations.getProperty(ConfigKeys.BROKER_URL), topicPrefix
                    + j, payload, publiserhStartSignal, publisherDoneSignal, threadId);
            publisher[j].start();
        }
    }

    private static void createSubscriberArray(int noTopicsPublished, int numberOfSubscriber,
            NotificationManager[] notifManagerArray, int arrayIndex) throws Exception, IOException {
        if ("false".equalsIgnoreCase(configurations.getProperty(ConfigKeys.IS_XPATH_ENABLED))) {
            if (numberOfSubscriber <= noTopicsPublished) {
                for (int i = 0; i < noTopicsPublished; ++i) {
                    notifManagerArray[arrayIndex].createTopicSubscription(topicPrefix + i);
                    notifManagerArray[arrayIndex++].incNoTopicsSubscribed();
                    if (arrayIndex >= numberOfSubscriber) {
                        arrayIndex = 0;
                    }
                }
            } else {
                int topicIndex = 0;
                for (int i = 0; i < numberOfSubscriber; ++i) {
                    notifManagerArray[i].createTopicSubscription(topicPrefix + topicIndex++);
                    notifManagerArray[i].incNoTopicsSubscribed();
                    if (topicIndex >= noTopicsPublished) {
                        topicIndex = 0;
                    }
                }
            }
        } else {
            xpathList = LoadXpath.getInstace().getXpathList("xpath.list");
            if (numberOfSubscriber <= noTopicsPublished) {
                Iterator<String> ite = xpathList.iterator();
                for (int i = 0; i < noTopicsPublished; ++i) {
                    if (!ite.hasNext())
                        ite = xpathList.iterator();

                    notifManagerArray[arrayIndex].createXpathSubscription(topicPrefix + i, ite.next());
                    notifManagerArray[arrayIndex++].incNoTopicsSubscribed();
                    if (arrayIndex >= numberOfSubscriber) {
                        arrayIndex = 0;
                    }
                }
            } else {
                int topicIndex = 0;
                for (int i = 0; i < numberOfSubscriber; ++i) {
                    notifManagerArray[i].incNoTopicsSubscribed();
                    if (topicIndex >= noTopicsPublished) {
                        topicIndex = 0;
                    }
                }
            }
        }
    }

    private static Properties getDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty(ConfigKeys.BROKER_URL, "http://localhost:8080/axis2/services/EventingService");
        defaults.setProperty(ConfigKeys.TOPIC_SIMPLE, "simpleSampleTopic");
        defaults.setProperty(ConfigKeys.CONSUMER_PORT, "6666");
        defaults.setProperty(ConfigKeys.NOTIFICATIONS_PUBLISHED_PER_TOPIC, "5");
        defaults.setProperty(ConfigKeys.NUMBER_OF_TOPICS_PUBLISHED, "5");
        defaults.setProperty(ConfigKeys.IS_XPATH_ENABLED, "false");
        defaults.setProperty(ConfigKeys.XPATH, "/c/b/a");
        defaults.setProperty(ConfigKeys.PAYLOAD_MULTIPLYER, "1");
        defaults.setProperty(ConfigKeys.PROTOCOL, "wse");
        defaults.setProperty(ConfigKeys.PUBLISH_TIME_INTERVAL, "10000");
        defaults.setProperty(ConfigKeys.PERFORMANCE_TEST_TIMEOUT, "5000000");
        defaults.setProperty(ConfigKeys.NUMBER_OF_SUBSCRIBERS, "1");
        defaults.setProperty(ConfigKeys.MULTI_THREAD_PER_SUB, "50");
        return defaults;
    }

    private static void printStatistics(List<Stat> stats, File aFile) throws IOException {
        int maxLen = 0;
        Writer output = new BufferedWriter(new FileWriter(aFile, true));

        for (Stat stat : stats) {
            maxLen = Math.max(maxLen, stat.name.length());
        }

        char[] fillchars = null;

        for (Stat stat : stats) {
            fillchars = new char[maxLen - stat.name.length() + 1];
            Arrays.fill(fillchars, ' ');
            String formattedStr = String.format("%s%s : %s", stat.name, new String(fillchars), stat.value.toString());
            output.write(formattedStr + "\n");
            System.out.println(formattedStr);
        }

        fillchars = new char[maxLen];
        Arrays.fill(fillchars, '-');
        String fillingString = new String(fillchars);
        output.write(fillingString + "\n");
        System.out.println(fillingString);
        output.close();
    }

    public static void loadConfigurationsFromFile() {
        configurations = new Properties(getDefaults());

        try {
            URL url = ClassLoader.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);
            if (url == null) {
                throw new IOException("configuration file not found");
            }
            configurations.load(url.openStream());
        } catch (IOException ioe) {
            System.out.println("unable to load configuration file, default settings will be used");
        }
    }

    // Not used, If required to run as a test case call it from main
    public static void loadConfigurationsFromSystemEnv() {

        configurations = new Properties(getDefaults());

        Properties envConfigs = System.getProperties();
        String brokerUrl = envConfigs.getProperty(ConfigKeys.BROKER_URL, null);
        String consumerUrl = envConfigs.getProperty(ConfigKeys.CONSUMER_EPR, null);
        String consumerPort = envConfigs.getProperty(ConfigKeys.CONSUMER_PORT, null);
        String isXpathEnabled = envConfigs.getProperty(ConfigKeys.IS_XPATH_ENABLED, null);
        String notifPerTopic = envConfigs.getProperty(ConfigKeys.NOTIFICATIONS_PUBLISHED_PER_TOPIC, null);
        String subsPerTopic = envConfigs.getProperty(ConfigKeys.NUMBER_OF_SUBS_PERTOPIC, null);
        String noTopicsPublished = envConfigs.getProperty(ConfigKeys.NUMBER_OF_TOPICS_PUBLISHED, null);
        String payLoadMultiplier = envConfigs.getProperty(ConfigKeys.PAYLOAD_MULTIPLYER, null);
        String protocol = envConfigs.getProperty(ConfigKeys.PROTOCOL, null);
        String topicSimple = envConfigs.getProperty(ConfigKeys.TOPIC_SIMPLE, null);
        String topicXpath = envConfigs.getProperty(ConfigKeys.XPATH, null);

        if (brokerUrl == null) {
            System.err.println(ConfigKeys.BROKER_URL + " not given");
            System.exit(1);
        }
        if (consumerUrl == null) {
            System.err.println(ConfigKeys.CONSUMER_EPR + " not given");
            System.exit(1);
        }
        if (consumerPort == null) {
            System.err.println(ConfigKeys.CONSUMER_PORT + " not given");
            System.exit(1);
        }
        if (isXpathEnabled == null) {
            System.err.println(ConfigKeys.IS_XPATH_ENABLED + " not given");
            System.exit(1);
        }
        if (notifPerTopic == null) {
            System.err.println(ConfigKeys.NOTIFICATIONS_PUBLISHED_PER_TOPIC + " not given");
            System.exit(1);
        }
        if (subsPerTopic == null) {
            System.err.println(ConfigKeys.NUMBER_OF_SUBS_PERTOPIC + " not given");
            System.exit(1);
        }
        if (noTopicsPublished == null) {
            System.err.println(ConfigKeys.NUMBER_OF_TOPICS_PUBLISHED + " not given");
            System.exit(1);
        }
        if (payLoadMultiplier == null) {
            System.err.println(ConfigKeys.PAYLOAD_MULTIPLYER + " not given");
            System.exit(1);
        }
        if (protocol == null) {
            System.err.println(ConfigKeys.PROTOCOL + " not given");
            System.exit(1);
        }
        if (topicSimple == null) {
            System.err.println(ConfigKeys.TOPIC_SIMPLE + " not given");
            System.exit(1);
        }
        if (topicXpath == null) {
            System.err.println(ConfigKeys.XPATH + " not given");
            System.exit(1);
        }

        configurations.put(ConfigKeys.BROKER_URL, brokerUrl);
        configurations.put(ConfigKeys.CONSUMER_EPR, consumerUrl);
        configurations.put(ConfigKeys.CONSUMER_PORT, consumerPort);
        configurations.put(ConfigKeys.IS_XPATH_ENABLED, isXpathEnabled);
        configurations.put(ConfigKeys.NOTIFICATIONS_PUBLISHED_PER_TOPIC, notifPerTopic);
        configurations.put(ConfigKeys.NUMBER_OF_SUBS_PERTOPIC, subsPerTopic);
        configurations.put(ConfigKeys.NUMBER_OF_TOPICS_PUBLISHED, noTopicsPublished);
        configurations.put(ConfigKeys.PAYLOAD_MULTIPLYER, payLoadMultiplier);
        configurations.put(ConfigKeys.PROTOCOL, protocol);
        configurations.put(ConfigKeys.TOPIC_SIMPLE, topicSimple);
        configurations.put(ConfigKeys.XPATH, topicXpath);
    }
}
