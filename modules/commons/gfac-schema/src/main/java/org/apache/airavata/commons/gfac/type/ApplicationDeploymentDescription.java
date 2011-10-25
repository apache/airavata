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

package org.apache.airavata.commons.gfac.type;

import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;

public class ApplicationDeploymentDescription implements Type {

	private ApplicationDeploymentDescriptionType appDeploymentDescType;

	public ApplicationDeploymentDescription() {
		this.appDeploymentDescType = ApplicationDeploymentDescriptionType.Factory
				.newInstance();
	}

	public ApplicationDeploymentDescription(
			ApplicationDeploymentDescriptionType addt) {
		this.appDeploymentDescType = addt;
	}

	public String getId() {
		return appDeploymentDescType.getName();
	}

	public void setId(String id) {
		this.appDeploymentDescType.setName(id);
	}

	public String getTmpDir() {
		return appDeploymentDescType.getTmpDir();
	}

	public void setTmpDir(String tmpDir) {
		this.appDeploymentDescType.setTmpDir(tmpDir);
	}

	public String getWorkingDir() {
		return appDeploymentDescType.getWorkingDir();
	}

	public void setWorkingDir(String workingDir) {
		this.appDeploymentDescType.setWorkingDir(workingDir);
	}

	public String getInputDir() {
		return appDeploymentDescType.getInputDir();
	}

	public void setInputDir(String inputDir) {
		this.appDeploymentDescType.setInputDir(inputDir);
	}

	public String getOutputDir() {
		return appDeploymentDescType.getOutputDir();
	}

	public void setOutputDir(String outputDir) {
		this.appDeploymentDescType.setOutputDir(outputDir);
	}
}
