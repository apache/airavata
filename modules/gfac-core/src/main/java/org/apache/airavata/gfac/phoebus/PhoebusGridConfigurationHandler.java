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
package org.apache.airavata.gfac.phoebus;

import org.apache.airavata.gfac.utils.GridConfigurationHandler;
import org.apache.airavata.gfac.utils.PhoebusUtils;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;

public class PhoebusGridConfigurationHandler implements GridConfigurationHandler{
    @Override
    public void handleSourceFTPClient(GridFTPClient client) throws Exception {
        if (PhoebusUtils.isPhoebusDriverConfigurationsDefined(client.getHost())) {
            client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            client.site("SITE SETNETSTACK phoebus:" + PhoebusUtils.getPhoebusDataChannelXIODriverParameters(client.getHost()));
        }
    }
    @Override
    public void handleDestinationFTPClient(GridFTPClient client)
            throws Exception {

    }
}
