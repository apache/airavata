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
package org.apache.airavata.common.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Version {
	public String PROJECT_NAME;
	private Integer majorVersion=0;
	private Integer minorVersion=0;
	private Integer maintenanceVersion;
	private String versionData;
	private BuildType buildType;
	
	public static enum BuildType{
		ALPHA,
		BETA,
		RC
	}
	
	public Version() {
	}
	
	public Version(String PROJECT_NAME,Integer majorVersion,Integer minorVersion,Integer maintenanceVersion,String versionData,BuildType buildType) {
		this.PROJECT_NAME=PROJECT_NAME;
		this.majorVersion=majorVersion;
		this.minorVersion=minorVersion;
		this.maintenanceVersion=maintenanceVersion;
		this.versionData=versionData;
		this.buildType=buildType;
	}
	
	public Integer getMajorVersion() {
		return majorVersion;
	}

	public Integer getMinorVersion() {
		return minorVersion;
	}

	public Integer getMaintenanceVersion() {
		return maintenanceVersion;
	}

	public String getVersionData() {
		return versionData;
	}

	public BuildType getBuildType() {
		return buildType;
	}
	
	public String getVersion(){
		String version = getBaseVersion();
		version = attachVersionData(version);
		return version;
	}

	private String attachVersionData(String version) {
		if (getVersionData()!=null){
			version+="-"+getVersionData();
		}
		return version;
	}

	public String getBaseVersion() {
		String version=getMajorVersion().toString()+"."+getMinorVersion();
		return version;
	}
	
	public String getFullVersion(){
		String version = getBaseVersion();
		version = attachMaintainanceVersion(version);
		version = attachVersionData(version);
		version = attachBuildType(version);
		return version;
	}

	private String attachMaintainanceVersion(String version) {
		if (getMaintenanceVersion()!=null){
			version+="."+getMaintenanceVersion();
		}
		return version;
	}
	
	private String attachBuildType(String version) {
		if (getBuildType()!=null){
			version+="-"+getBuildType().name();
		}
		return version;
	}
	
	@Override
	public String toString() {
		return getVersion();
	}
}
