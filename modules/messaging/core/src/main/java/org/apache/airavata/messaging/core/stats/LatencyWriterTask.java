package org.apache.airavata.messaging.core.stats;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class LatencyWriterTask extends TimerTask {

    private File file;
    private FileOutputStream fos;
    private BufferedWriter bw;

    public void setFile(File file) {
        this.file = file;

    }

    @Override
    public void run() {
        try {
            System.out.println("########### Latency Write Task ############");
            StatCounter statCounter = StatCounter.getInstance();
            Map<String, Long> messageTimeStamp = statCounter.getMessageTimeStamp();
            fos = new FileOutputStream(file, false);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String msgId : messageTimeStamp.keySet()){
                bw.write(msgId + " :" + String.valueOf(messageTimeStamp.get(msgId)));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
