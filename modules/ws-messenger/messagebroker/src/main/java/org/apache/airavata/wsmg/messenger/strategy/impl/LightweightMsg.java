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

import org.apache.airavata.wsmg.broker.AdditionalMessageContent;
import org.apache.airavata.wsmg.broker.ConsumerInfo;

class LightweightMsg {
    private ConsumerInfo consumerInfo;
    private String payload;
    private AdditionalMessageContent header;

    public LightweightMsg(ConsumerInfo c, String pld, AdditionalMessageContent h) {
        consumerInfo = c;
        payload = pld;
        header = h;
    }

    public String getPayLoad() {
        return payload;
    }

    public ConsumerInfo getConsumerInfo() {
        return consumerInfo;
    }

    public AdditionalMessageContent getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("header: %s, consumer: %s, pld: %s", header, consumerInfo.getConsumerEprStr(), payload);
    }

}
