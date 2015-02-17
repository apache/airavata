package org.apache.airavata.messaging.core.stats;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class StatCounter {
    private static StatCounter ourInstance = new StatCounter();
    private long msgCount;
    private long period = 10*1000;
    private long msgCountForPeriod;

    private long bucketStartTime = 0;
    private File file;
    private FileOutputStream fos;
    private BufferedWriter bw;

    private List<Long> messageContPer10S = new ArrayList<Long>();

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
        file = new File("/tmp/results");
        Timer timer = new Timer();
        WriterTask writerTask = new WriterTask();
        writerTask.setFile(file);
        timer.scheduleAtFixedRate(writerTask, 0, 60 * 1000);

    }

    public void add () {
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
}
