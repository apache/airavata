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
