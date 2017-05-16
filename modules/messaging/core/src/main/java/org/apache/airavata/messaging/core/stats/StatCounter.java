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
package org.apache.airavata.messaging.core.stats;


import org.apache.airavata.model.messaging.event.Message;

import java.io.*;
import java.util.*;

public class StatCounter {
    private static StatCounter ourInstance = new StatCounter();
    private long msgCount;
    private long period = 10*1000;
    private long msgCountForPeriod;

    private long bucketStartTime = 0;
    private File file1;
    private File file2;

    private List<Long> messageContPer10S = new ArrayList<Long>();
    private Map<String, Long> messageTimeStamp = new HashMap<String, Long>();

    public static StatCounter getInstance() {
        return ourInstance;
    }

    public long getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(long msgCount) {
        this.msgCount = msgCount;
    }

    public List<Long> getMessageContPer10S() {
        return messageContPer10S;
    }

    public void setMessageContPer10S(List<Long> messageContPer10S) {
        this.messageContPer10S = messageContPer10S;
    }

    private StatCounter() {
        file1 = new File("/tmp/results");
        file2 = new File("/tmp/latency");
        Timer counterTimer = new Timer();
        Timer latencyTimer = new Timer();
        CountWriterTask writerTask = new CountWriterTask();
        writerTask.setFile(file1);
        LatencyWriterTask latencyWriterTask = new LatencyWriterTask();
        latencyWriterTask.setFile(file2);
        counterTimer.scheduleAtFixedRate(writerTask, 0, 60 * 1000);
        latencyTimer.scheduleAtFixedRate(latencyWriterTask, 0, 60 * 1000);

    }

    public void add (Message message) {
        messageTimeStamp.put(message.getMessageId(), System.currentTimeMillis());
        if (System.currentTimeMillis() - bucketStartTime < period) {
            msgCountForPeriod++;
        } else {
            messageContPer10S.add(msgCountForPeriod);
            bucketStartTime = System.currentTimeMillis();
            msgCountForPeriod = 1;
        }
        msgCount++;
    }

    public long getMsgCountForPeriod() {
        return msgCountForPeriod;
    }

    public void setMsgCountForPeriod(long msgCountForPeriod) {
        this.msgCountForPeriod = msgCountForPeriod;
    }

    public Map<String, Long> getMessageTimeStamp() {
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(Map<String, Long> messageTimeStamp) {
        this.messageTimeStamp = messageTimeStamp;
    }
}
