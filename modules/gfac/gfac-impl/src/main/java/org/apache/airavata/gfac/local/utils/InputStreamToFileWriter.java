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
package org.apache.airavata.gfac.local.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class InputStreamToFileWriter extends Thread{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private BufferedReader in;
    private BufferedWriter out;

    public InputStreamToFileWriter(InputStream in, String out) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new BufferedWriter(new FileWriter(out));
    }

    public void run() {
        try {
            String line = null;
            while ((line = in.readLine()) != null) {
                if (log.isDebugEnabled()) {
                    log.debug(line);
                }
                out.write(line);
                out.newLine();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
