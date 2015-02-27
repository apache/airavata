package org.apache.airavata.messaging.client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by chathuri on 2/20/15.
 */
public class TestReader {
    public static void main(String[] args) throws IOException {
        String fileName = "/Users/chathuri/dev/airavata/docs/messaging_framework/gw111/results_350";
        File file = new File("/Users/chathuri/dev/airavata/docs/messaging_framework/gw111/processed/results_350");
        BufferedReader br = null;
        List<Long> count = new ArrayList<Long>();
        FileOutputStream fos;
        BufferedWriter bw;
        try {
            br = new BufferedReader(new FileReader(fileName));
//            String line = br.readLine();
            Stream<String> lines = br.lines();
            Object[] objects = lines.toArray();
            for (int i = 0; i < objects.length; i++){
                String line = (String)objects[i];
                if (line.contains(":")){
                    String[] split = line.split(":");
                    count.add(Long.valueOf(split[1]));
                }
            }
            fos = new FileOutputStream(file, false);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            long allCount = 0;
            for (int i = 0; i < count.size(); i++) {
                if (i % 10 != 9){
                    allCount += count.get(i);
                }else {
                    bw.write(String.valueOf(i + 1) + " :" + String.valueOf(allCount));
                    bw.newLine();
                    allCount = 0;
                }
            }
            bw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
    }




}
