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
 */
package org.apache.airavata.agents.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class VirtualInputStream extends InputStream {

    private BlockingQueue<Integer> queue;
    private long byteCount;
    private long streamLength;

    public VirtualInputStream(BlockingQueue<Integer> queue, long streamLength) {
        this.queue = queue;
        this.streamLength = streamLength;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (byteCount == streamLength) {
            return -1;
        }

        int c = read();

        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                if (byteCount == streamLength) {
                    break;
                }
                c = read();
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    @Override
    public int read() throws IOException {
        try {
            Integer cont =  queue.poll(10, TimeUnit.SECONDS);
            if (cont == null) {
                throw new IOException("Timed out reading from the queue");
            }
            byteCount++;
            return cont;
        } catch (InterruptedException e) {
            throw new IOException("Read was interrupted", e);
        }
    }
}
