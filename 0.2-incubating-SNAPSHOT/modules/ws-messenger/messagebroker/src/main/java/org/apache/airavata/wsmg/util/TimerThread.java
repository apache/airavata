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

public class TimerThread implements Runnable {
    Counter counter;

    long counterValue = 0;

    long seqNum = 0;

    String comment = "";

    public TimerThread(Counter counter, String comment) {
        this.counter = counter;
        this.comment = comment;
    }

    public void run() {
        long currentTime = 0;
        long interval = 1000;
        long lastCounter = 0;
        long idleCount = 0;
        // wait for about 5 sec and start from 000 time so that other thread can
        // start together
        currentTime = System.currentTimeMillis();
        long launchTime = ((currentTime + 2000) / 1000) * 1000;
        long sleepTime = launchTime - currentTime;
        System.out.println("launchTime=" + launchTime + " SleepTime=" + sleepTime);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (true) {
            currentTime = System.currentTimeMillis();
            counterValue = counter.getCounterValue();
            long receivedCount = counterValue - lastCounter;
            lastCounter = counterValue;
            if (receivedCount == 0) {
                idleCount++;
            } else {
                idleCount = 0;
            }
            if (receivedCount > 0 || (receivedCount == 0 && idleCount < 3)) {
                // System.out.println("time="+currentTime+" counter="+
                // counter.getCounterValue()+"
                // received="+receivedCount+comment);
                System.out.println(seqNum + " " + counter.getCounterValue() + " " + receivedCount + comment
                        + counter.getOtherValueString());
            }
            seqNum++;
            launchTime = launchTime + interval;
            sleepTime = launchTime - currentTime;
            // System.out.println("launchTime="+launchTime+"
            // SleepTime="+sleepTime);
            if (sleepTime < 0)
                sleepTime = 0;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
