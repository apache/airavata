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

package org.apache.airavata.client.impl;

import org.apache.airavata.client.api.OutputDataSettings;

public class ApplicationOutputDataSettingsImpl implements OutputDataSettings {
	private String outputDataDirectory;
	private String dataRegistry;
	private Boolean dataPersistent;

	public ApplicationOutputDataSettingsImpl() {
	}
	
	public ApplicationOutputDataSettingsImpl(String outputDataDirectory, String dataRegistryUrl, Boolean dataPersistent) {
		setOutputDataDirectory(outputDataDirectory);
		setDataRegistryUrl(dataRegistryUrl);
		setDataPersistent(dataPersistent);
	}
	
	@Override
	public String getOutputDataDirectory() {
		return outputDataDirectory;
	}

	@Override
	public String getDataRegistryUrl() {
		return dataRegistry;
	}

	@Override
	public Boolean isDataPersistent() {
		return dataPersistent;
	}

	@Override
	public void setOutputDataDirectory(String outputDataDirectory) {
		this.outputDataDirectory=outputDataDirectory;
	}

	@Override
	public void setDataRegistryUrl(String dataRegistryUrl) {
		this.dataRegistry=dataRegistryUrl;
	}

	@Override
	public void setDataPersistent(boolean isDataPersistance) {
		this.dataPersistent=isDataPersistance;
	}

	@Override
	public void resetOutputDataDirectory() {
		this.outputDataDirectory=null;
	}

	@Override
	public void resetDataRegistryUrl() {
		this.dataRegistry=null;
	}

	@Override
	public void resetDataPersistent() {
		this.dataPersistent=null;
	}

}
