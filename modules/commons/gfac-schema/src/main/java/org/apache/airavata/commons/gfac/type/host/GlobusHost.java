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

package org.apache.airavata.commons.gfac.type.host;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;

public class GlobusHost extends HostDescription {

    private GlobusHostType globusHostType;

    private GlobusHost() {
        this.globusHostType = GlobusHostType.Factory.newInstance();
    }

    private GlobusHost(GlobusHostType ght) {
        this.globusHostType = ght;
    }

    public String getGridFTPEndPoint() {
        return globusHostType.getGridFTPEndPoint();
    }

    public void setGridFTPEndPoint(String gridFTPEndPoint) {
        this.globusHostType.setGridFTPEndPoint(gridFTPEndPoint);
    }

    public String getGlobusGateKeeperEndPoint() {
        return globusHostType.getGlobusGateKeeperEndPoint();
    }

    public void setGlobusGateKeeperEndPoint(String globusGateKeeperEndPoint) {
        this.globusHostType.setGlobusGateKeeperEndPoint(globusGateKeeperEndPoint);
    }
}