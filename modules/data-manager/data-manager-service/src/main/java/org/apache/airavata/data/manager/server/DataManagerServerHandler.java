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
package org.apache.airavata.data.manager.server;

import org.apache.airavata.data.manager.cpi.DataManagerService;
import org.apache.airavata.data.manager.cpi.data_manager_cpiConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataManagerServerHandler implements DataManagerService.Iface {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerServerHandler.class);

    /**
     * Query DM server to fetch the CPI version
     */
    @Override
    public String getDMServiceVersion() throws TException {
        return data_manager_cpiConstants.DM_CPI_VERSION;
    }
}