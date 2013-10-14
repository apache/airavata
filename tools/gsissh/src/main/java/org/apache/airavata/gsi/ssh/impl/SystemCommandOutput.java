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

package org.apache.airavata.gsi.ssh.impl;

import com.jcraft.jsch.Channel;
import org.apache.airavata.gsi.ssh.api.CommandOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/15/13
 * Time: 10:44 AM
 */

public class SystemCommandOutput implements CommandOutput {

    public void onOutput(Channel channel) {
        try {
            InputStream inputStream = channel.getInputStream();
            byte[] tmp = new byte[1024];
            while (true) {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public OutputStream getStandardError() {
        return System.err;
    }

    public void exitCode(int code) {
        System.out.println("Program exit code - " + code);
    }


}
