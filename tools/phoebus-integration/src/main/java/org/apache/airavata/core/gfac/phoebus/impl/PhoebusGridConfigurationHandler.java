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
package org.apache.airavata.core.gfac.phoebus.impl;

import org.apache.airavata.common.utils.Version;
import org.apache.airavata.common.utils.Version.BuildType;
import org.apache.airavata.core.gfac.utils.PhoebusUtils;
import org.apache.airavata.gfac.utils.GridConfigurationHandler;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
//
public class PhoebusGridConfigurationHandler implements GridConfigurationHandler{
	private static String HANDLER_NAME="PHOEBUS";
	
    public void handleFileTransferFTPClientConfigurations(GridFTPClient source, GridFTPClient destination) throws GridConfigurationHandlerException {
        try {
			if (source!=null && PhoebusUtils.isPhoebusDriverConfigurationsDefined(source.getHost())) {
			    source.setDataChannelAuthentication(DataChannelAuthentication.NONE);
			    source.site("SITE SETNETSTACK phoebus:" + PhoebusUtils.getPhoebusDataChannelXIODriverParameters(source.getHost()));
			}
		} catch (Exception e) {
			throw new GridConfigurationHandlerException("Error configuring for Phoebus handler: "+e.getLocalizedMessage(),e);
		}
    }

    public void handleMakeDirFTPClientConfigurations(GridFTPClient client, String dirPath)
            throws GridConfigurationHandlerException {
    	//nothing to do
    }

	@Override
	public void handleListDirFTPClientConfigurations(GridFTPClient client, String dirPath)
			throws GridConfigurationHandlerException {
    	//nothing to do
	}

	@Override
	public void handleFTPClientConfigurations(GridFTPClient client,
			String taskDescription) throws GridConfigurationHandlerException {
    	//nothing to do
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

	@Override
	public Version getHandlerVersion() {
		return new Version(HANDLER_NAME, 1, 0, 0, null, BuildType.RC);
	}

}
