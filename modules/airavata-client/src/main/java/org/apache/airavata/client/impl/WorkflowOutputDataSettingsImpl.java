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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.client.api.OutputDataSettings;
import org.apache.airavata.client.api.WorkflowOutputDataSettings;

public class WorkflowOutputDataSettingsImpl implements
		WorkflowOutputDataSettings {
	private List<OutputDataSettings> outputDataSettingsList;
	
	private List<OutputDataSettings> getOutputDataSettingsList(){
		if (outputDataSettingsList==null){
			outputDataSettingsList=new ArrayList<OutputDataSettings>();
		}
		return outputDataSettingsList;
	}
	
	@Override
	public OutputDataSettings[] getOutputDataSettings() {
		return getOutputDataSettingsList().toArray(new OutputDataSettings[]{});
	}

	@Override
	public OutputDataSettings addNewOutputDataSettings(
			String outputDataDirectory, String dataRegistryURL,
			boolean isDataPersistent) {
		getOutputDataSettingsList().add(new ApplicationOutputDataSettingsImpl(outputDataDirectory, dataRegistryURL, isDataPersistent));
		return getOutputDataSettingsList().get(getOutputDataSettingsList().size()-1);
	}

	@Override
	public void addNewOutputDataSettings(
			OutputDataSettings... outputDataSettings) {
		getOutputDataSettingsList().addAll(Arrays.asList(outputDataSettings));

	}

	@Override
	public void removeOutputDataSettings(OutputDataSettings outputDataSettings) {
		if (getOutputDataSettingsList().contains(outputDataSettings)){
			getOutputDataSettingsList().remove(outputDataSettings);
		}
	}

	@Override
	public void removeAllOutputDataSettings() {
		getOutputDataSettingsList().clear();
	}

}
