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
package org.apache.airavata.gfac.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represent the data related to gfac instances
 * if orchestrator is running on non-embedded mode,
 * This information can be used to do better load balancing between
 * different gfac instances
 */
public class GFACInstance {
    private final static Logger logger = LoggerFactory.getLogger(GFACInstance.class);

    private String gfacURL;

    private int currentLoad;

    private int gfacPort;


    public GFACInstance(String gfacURL, int gfacPort) {
        this.gfacURL = gfacURL;
        this.gfacPort = gfacPort;
    }

    public String getGfacURL() {
        return gfacURL;
    }

    public void setGfacURL(String gfacURL) {
        this.gfacURL = gfacURL;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }
}
