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

package org.apache.airavata.wsmg.broker;

import java.io.Serializable;

public class ConsumerInfo implements Serializable {
    static final long serialVersionUID = 2274650724788817903L;

    // TODO : change this to OM, EndpointReference.
    String consumerEprStr;

    String type; // Either "wsnt" or "wse"

    boolean useNotify;

    boolean paused = false;

    boolean wsrmEnabled;

    /**
     * @param consumerEprStr
     * @param type
     * @param useNotify
     * @param paused
     */
    public ConsumerInfo(String consumerEprStr, String type, boolean useNotify, boolean paused) {
        super();
        // TODO Auto-generated constructor stub
        this.consumerEprStr = consumerEprStr;
        this.type = type;
        this.useNotify = useNotify;
        this.paused = paused;
    }

    /**
     * @return Returns the consumerEprStr.
     */
    public String getConsumerEprStr() {
        return consumerEprStr;
    }

    /**
     * @param consumerEprStr
     *            The consumerEprStr to set.
     */
    public void setConsumerEprStr(String consumerEprStr) {
        this.consumerEprStr = consumerEprStr;
    }

    /**
     * @return Returns the paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @param paused
     *            The paused to set.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the useNotify.
     */
    public boolean isUseNotify() {
        return useNotify;
    }

    /**
     * @param useNotify
     *            The useNotify to set.
     */
    public void setUseNotify(boolean useNotify) {
        this.useNotify = useNotify;
    }

    public boolean isWsrmEnabled() {
        return wsrmEnabled;
    }

    public void setWsrmEnabled(boolean wsrmEnabled) {
        this.wsrmEnabled = wsrmEnabled;
    }

}
