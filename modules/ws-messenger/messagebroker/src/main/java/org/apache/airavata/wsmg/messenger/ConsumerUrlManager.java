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

import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumerUrlManager {

    private static final Log logger = LogFactory.getLog(ConsumerUrlManager.class);

    private ConcurrentHashMap<String, FailedConsumerInfo> failedConsumerUrls = new ConcurrentHashMap<String, FailedConsumerInfo>();

    private final int defaultMaxRetry;

    private long expireTimeGap; // milliseconds

    private Timer cleanupTimer;

    public ConsumerUrlManager(ConfigurationManager config) {

        defaultMaxRetry = config.getConfig(WsmgCommonConstants.CONFIG_MAX_MESSAGE_DELIVER_RETRIES, 2);

        // time is in milliseconds
        expireTimeGap = 1000 * 60 * config.getConfig(WsmgCommonConstants.CONFIG_CONSUMER_URL_EXPIRATION_TIME_GAP, 5l);

        // let minimum time to be 1 minute
        long timerThreadInterval = Math.max(expireTimeGap / 5, 1000 * 60);

        cleanupTimer = new Timer("Failed consumer url handler", true);
        cleanupTimer.scheduleAtFixedRate(new URLCleanUpTask(), 0, timerThreadInterval);
    }

    public void stop() {
        logger.info("Stop ConsumerUrlManager");
        if (this.cleanupTimer != null) {
            this.cleanupTimer.cancel();
        }
        logger.info("ConsumerUrlManager Stopped");
    }

    public void onFailedDelivery(EndpointReference consumerEndpointReference, long timeFinished, long timeTaken,
            AxisFault exception, AdditionalMessageContent headers) {
        String url = consumerEndpointReference.getAddress();

        RunTimeStatistics.addNewFailedDeliverTime(timeTaken);
        RunTimeStatistics.addFailedConsumerURL(url);

        if (isEligibleToBlackList(exception)) {

            synchronized (failedConsumerUrls) {
                FailedConsumerInfo info = failedConsumerUrls.get(url);
                if (info == null) {
                    info = new FailedConsumerInfo();
                    failedConsumerUrls.put(url, info);
                }
                info.incrementNumberOfTimesTried(timeFinished + expireTimeGap);
            }

        } else {

            String errorMsg = String.format("unable to deliver message: [%s] to consumer: [%s], " + "reason: [%s]",
                    headers.toString(), url, exception.getMessage());

            logger.error(errorMsg);
        }
    }

    public void onSucessfullDelivery(EndpointReference consumerEndpointReference, long timeTaken) {

        RunTimeStatistics.addNewSuccessfulDeliverTime(timeTaken);
        synchronized (failedConsumerUrls) {

            FailedConsumerInfo info = failedConsumerUrls.remove(consumerEndpointReference.getAddress());

            if (info != null) {
                logger.debug(String.format("message was delivered to " + "previously %d times failed url : %s",
                        info.getNumberOfTimesTried(), consumerEndpointReference.getAddress()));
            }
        }
    }

    public boolean isUnavailable(String url) {
        synchronized (failedConsumerUrls) {
            FailedConsumerInfo info = failedConsumerUrls.get(url);
            return (info != null && info.isMaxRetryCountReached());
        }
    }

    private boolean isEligibleToBlackList(AxisFault f) {

        Throwable cause = f.getCause();

        if (cause == null) {
            logger.error("unknown error occured", cause);
            return false;
        }

        /*
         * if timeout because of the set timeout in this class In windows, timeout cause ConnectException with
         * "Connection timed out" message
         */
        if (cause instanceof SocketTimeoutException || cause.getMessage().indexOf("timed out") > 0
                || cause instanceof NoRouteToHostException) {
            return true;
        }

        return false;
    }

    class FailedConsumerInfo {

        private int numberOfTimesTried;
        private long expiryTime;

        public void incrementNumberOfTimesTried(long expireTime) {
            numberOfTimesTried++;
            expiryTime = expireTime;
        }

        public void decrementNumberOfTimeTried() {
            numberOfTimesTried--;
        }

        public int getNumberOfTimesTried() {
            return numberOfTimesTried;
        }

        public boolean isMaxRetryCountReached() {
            return numberOfTimesTried >= defaultMaxRetry;
        }

        public long getLastAtteptExpiryTime() {
            return expiryTime;
        }

    }

    class URLCleanUpTask extends TimerTask {

        @Override
        public void run() {

            logger.debug("starting to clean up black listed consumer urls");
            long currentTime = System.currentTimeMillis();

            synchronized (failedConsumerUrls) {
                for (Entry<String, FailedConsumerInfo> entry : failedConsumerUrls.entrySet()) {
                    FailedConsumerInfo info = entry.getValue();

                    if (info.isMaxRetryCountReached() && info.getLastAtteptExpiryTime() >= currentTime) {

                        info.decrementNumberOfTimeTried();
                        logger.info("decrementing number of times" + " tried for consumer url: " + entry.getKey());

                    }
                }
            }

            logger.debug("finished cleaning black listed consumer urls");
        }
    }
}
