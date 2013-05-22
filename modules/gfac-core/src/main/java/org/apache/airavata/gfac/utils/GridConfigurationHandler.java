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
package org.apache.airavata.gfac.utils;
import org.globus.ftp.GridFTPClient;

public interface GridConfigurationHandler {

	/**
	 * Gets triggered if any GridFTP tasks other than mentioned below if called. This is there to 
	 * support future extensions in GridFTP tasks so that the handlers need not be updated necessarily 
	 * @param client
	 * @param taskDescription - a description of the task that is being carried out.
	 * @throws Exception
	 */
	public void handleFTPClientConfigurations(GridFTPClient client, String taskDescription) throws Exception;

	/**
	 * Do the configurations required for the source GridFTPClient object
	 * @param source - <code>null</code> if the transfer is from the local file-system
	 * @param destination - <code>null</code> if the transfer is to the local file-system 
	 * @throws Exception
	 */
	public void handleFileTransferFTPClientConfigurations(GridFTPClient source, GridFTPClient destination) throws Exception;

	/**
	 * Do the configurations required for the GridFTPClient object which is going to create a directory
	 * @param client
	 * @throws Exception
	 */
	public void handleMakeDirFTPClientConfigurations(GridFTPClient client, String dirPath) throws Exception;
	
	/**
	 * Do the configurations required for the GridFTPClient object which is going to list a directory
	 * @param client
	 * @throws Exception
	 */
	public void handleListDirFTPClientConfigurations(GridFTPClient client) throws Exception;
}

