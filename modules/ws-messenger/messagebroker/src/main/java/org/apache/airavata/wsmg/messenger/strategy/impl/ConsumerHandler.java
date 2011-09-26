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

package org.apache.airavata.wsmg.messenger.strategy.impl;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConsumerHandler implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(FixedParallelSender.class);
    
    protected LinkedBlockingQueue<LightweightMsg> queue = new LinkedBlockingQueue<LightweightMsg>();

    private String consumerUrl;

    private Deliverable deliverable;

    public ConsumerHandler(String url, Deliverable deliverable) {
        this.consumerUrl = url;
        this.deliverable = deliverable;
    }
    
    public String getConsumerUrl() {
        return consumerUrl;
    }   

    public void submitMessage(LightweightMsg msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            log.error("Interrupted when trying to add message");
        }
    }

    protected void send(List<LightweightMsg> list) {
        for (LightweightMsg m : list) {
            try {
                OMElement messgae2Send = CommonRoutines.reader2OMElement(new StringReader(m.getPayLoad()));
                deliverable.send(m.getConsumerInfo(), messgae2Send, m.getHeader());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
