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

import java.io.*;
import java.util.List;
import java.util.TimerTask;

public class CountWriterTask extends TimerTask {

    private File file;
    private FileOutputStream fos;
    private BufferedWriter bw;

    public void setFile(File file) {
        this.file = file;

    }

    @Override
    public void run() {
        try {
            StatCounter statCounter = StatCounter.getInstance();
            List<Long> contPer10S = statCounter.getMessageContPer10S();
            fos = new FileOutputStream(file, false);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (int i = 0; i < contPer10S.size(); i++) {
                bw.write(String.valueOf(i+1) + " :" + String.valueOf(contPer10S.get(i)));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
