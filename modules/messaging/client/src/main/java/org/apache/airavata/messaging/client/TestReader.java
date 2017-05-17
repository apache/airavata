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
package org.apache.airavata.messaging.client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
//import java.util.stream.Stream;

public class TestReader {
//    public static void main(String[] args) throws IOException {
//        String fileName = "/Users/chathuri/dev/airavata/docs/messaging_framework/gw111/results_350";
//        File file = new File("/Users/chathuri/dev/airavata/docs/messaging_framework/gw111/processed/results_350");
//        BufferedReader br = null;
//        List<Long> count = new ArrayList<Long>();
//        FileOutputStream fos;
//        BufferedWriter bw;
//        try {
//            br = new BufferedReader(new FileReader(fileName));
////            String line = br.readLine();
//            Stream<String> lines = br.lines();
//            Object[] objects = lines.toArray();
//            for (int i = 0; i < objects.length; i++){
//                String line = (String)objects[i];
//                if (line.contains(":")){
//                    String[] split = line.split(":");
//                    count.add(Long.valueOf(split[1]));
//                }
//            }
//            fos = new FileOutputStream(file, false);
//            bw = new BufferedWriter(new OutputStreamWriter(fos));
//            long allCount = 0;
//            for (int i = 0; i < count.size(); i++) {
//                if (i % 10 != 9){
//                    allCount += count.get(i);
//                }else {
//                    bw.write(String.valueOf(i + 1) + " :" + String.valueOf(allCount));
//                    bw.newLine();
//                    allCount = 0;
//                }
//            }
//            bw.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            br.close();
//        }
//    }

}
