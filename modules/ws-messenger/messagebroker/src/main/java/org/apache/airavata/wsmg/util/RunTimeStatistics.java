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

package org.apache.airavata.wsmg.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.WsmgVersion;

public class RunTimeStatistics {
    public static long totalMessageSize = 0;
    public static long totalReceivedNotification = 0;
    public static long totalSentOutNotification = 0;
    public static long totalFailedNotification = 0;
    public static long totalSubscriptions = 0;
    public static long totalSubscriptionsAtStartUp = 0;
    public static long totalUnSubscriptions = 0;
    public static long minMessageSize = Long.MAX_VALUE;
    public static long maxMessageSize = 0;
    public static String startUpTime = "";
    public static long totalSuccessfulDeliveryTime = 0;
    public static long totalFailedDeliveryTime = 0;
    public static long minSuccessfulDeliveryTime = Long.MAX_VALUE;
    public static long maxSuccessfulDeliveryTime = 0;
    public static long minFailedDeliveryTime = Long.MAX_VALUE;
    public static long maxFailedDeliveryTime = 0;
    public static HashMap<String, Integer> failConsumerList = new HashMap<String, Integer>();

    // public static TreeSet currentBlackList=new TreeSet();
    // public static TreeSet previousBlackList=new TreeSet();

    private static long startUpTimeInMillis;

    public static synchronized void addNewNotificationMessageSize(int size) {
        if (size < minMessageSize) {
            minMessageSize = size;
        }
        if (size > maxMessageSize) {
            maxMessageSize = size;
        }
        totalMessageSize += size;
        totalReceivedNotification++;
    }

    public static synchronized void addNewSuccessfulDeliverTime(long deliveryTime) {
        if (deliveryTime < minSuccessfulDeliveryTime) {
            minSuccessfulDeliveryTime = deliveryTime;
        }
        if (deliveryTime > maxSuccessfulDeliveryTime) {
            maxSuccessfulDeliveryTime = deliveryTime;
        }
        totalSuccessfulDeliveryTime += deliveryTime;
        totalSentOutNotification++;
    }

    public static synchronized void addNewFailedDeliverTime(long deliveryTime) {
        if (deliveryTime < minFailedDeliveryTime) {
            minFailedDeliveryTime = deliveryTime;
        }
        if (deliveryTime > maxFailedDeliveryTime) {
            maxFailedDeliveryTime = deliveryTime;
        }
        totalFailedDeliveryTime += deliveryTime;
        totalFailedNotification++;
    }

    public static synchronized void addFailedConsumerURL(String url) {
        Integer previousCount = failConsumerList.get(url);
        if (previousCount == null) {
            failConsumerList.put(url, 1);
        } else {
            previousCount++;
            failConsumerList.put(url, previousCount);
        }
    }

    public static void setStartUpTime() {
        Date currentDate = new Date(); // Current date
        startUpTime = CommonRoutines.getXsdDateTime(currentDate);
        startUpTimeInMillis = currentDate.getTime();
    }

    public static String getHtmlString() {
        String htmlString = "";

        htmlString += "<p>Total incoming message number: <span class=\"xml-requests-count\">"
                + totalReceivedNotification + "</span><br />\n";
        htmlString += "Total successful outgoing message number: " + totalSentOutNotification + "<br>\n";
        htmlString += "Total unreachable outgoing message number: " + totalFailedNotification + "<br>\n";
        htmlString += "Total subscriptions requested: " + totalSubscriptions + "(+" + totalSubscriptionsAtStartUp
                + " startUp)<br>\n";
        htmlString += "Total Unsubscriptions requested: " + totalUnSubscriptions + "<br>\n";
        htmlString += "</p>\n";
        int averageMessageSize = 0;
        if (totalReceivedNotification != 0) {
            averageMessageSize = (int) (totalMessageSize / totalReceivedNotification);
        }
        htmlString += "<p>Average message size: " + averageMessageSize + " bytes<br>\n";
        htmlString += "Max message size: " + maxMessageSize + " bytes<br>\n";
        htmlString += "Min message size: " + minMessageSize + " bytes<br>\n";
        htmlString += "</p>\n";
        long averageSuccessfulDeliveryTime = 0;
        if (totalSuccessfulDeliveryTime != 0) {
            averageSuccessfulDeliveryTime = (totalSuccessfulDeliveryTime / totalSentOutNotification);
        }
        htmlString += "<p>Average Successful Delivery Time: " + averageSuccessfulDeliveryTime + " ms<br>\n";
        htmlString += "Max Successful Delivery Time: " + maxSuccessfulDeliveryTime + " ms<br>\n";
        htmlString += "Min Successful Delivery Time: " + minSuccessfulDeliveryTime + " ms<br>\n";
        htmlString += "</p>\n";
        long averageFailedDeliveryTime = 0;
        if (totalFailedDeliveryTime != 0) {
            averageFailedDeliveryTime = (totalFailedDeliveryTime / totalFailedNotification);
        }
        htmlString += "<p>Average Unreachable Delivery Time: " + averageFailedDeliveryTime + " ms<br>\n";
        htmlString += "Max Unreachable Delivery Time: " + maxFailedDeliveryTime + " ms<br>\n";
        htmlString += "Min Unreachable Delivery Time: " + minFailedDeliveryTime + " ms<br>\n";
        htmlString += "</p>\n";
        htmlString += "<p>Service started at: " + startUpTime + " <span class=\"starttime-seconds\">"
                + startUpTimeInMillis + "</span> [seconds] since UNIX epoch)" + "<br />\n";

        htmlString += "Version: <span class=\"service-name\">" + WsmgVersion.getImplementationVersion()
                + "</span></p>\n";

        htmlString += "<p>Total unreachable consumerUrl: " + failConsumerList.size() + " <br>\n";
        TreeSet<String> consumerUrlList = new TreeSet<String>(failConsumerList.keySet());
        Iterator<String> iter = consumerUrlList.iterator();
        while (iter.hasNext()) {
            String url = iter.next();
            int failedCount = failConsumerList.get(url);
            htmlString += "  " + url + " -->" + failedCount + " <br>\n";
        }
        htmlString += "</p>\n";
        return htmlString;
    }
}
