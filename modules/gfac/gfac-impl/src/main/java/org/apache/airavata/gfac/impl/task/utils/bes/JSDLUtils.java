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
package org.apache.airavata.gfac.impl.task.utils.bes;


import org.apache.commons.httpclient.URIException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.*;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.EnvironmentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdlPosix.POSIXApplicationType;
import org.ggf.schemas.jsdl.x2006.x07.jsdlHpcpa.HPCProfileApplicationDocument;
import org.ggf.schemas.jsdl.x2006.x07.jsdlHpcpa.HPCProfileApplicationType;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.SPMDApplicationDocument;
import org.ogf.schemas.jsdl.x2007.x02.jsdlSpmd.SPMDApplicationType;

import javax.xml.namespace.QName;


/**
 * 
 * @author shahbaz memon, bastian demuth
 *
 */
public class JSDLUtils
{

	public static final int FLAG_OVERWRITE = 1;
	public static final int FLAG_APPEND = 2;
	public static final int FLAG_DELETE_ON_TERMINATE = 32;

	public static final QName POSIX_APPLICATION=POSIXApplicationDocument.type.getDocumentElementName();
	
	public static final QName HPC_PROFILE_APPLICATION=HPCProfileApplicationDocument.type.getDocumentElementName();
	
	public static final QName SPMD_APPLICATION=SPMDApplicationDocument.type.getDocumentElementName();
	
	public static final String PROCESSESPERHOST = "ProcessesPerHost"; 
	public static final String NUMBEROFPROCESSES = "NumberOfProcesses";
	public static final String THREADSPERHOST = "ThreadsPerHost";


	
	public static EnvironmentType addEnvVariable(JobDefinitionType def,String name, String value) {
		POSIXApplicationType posixApp = getOrCreatePOSIXApplication(def);
		EnvironmentType newEnv = posixApp.addNewEnvironment();
		newEnv.setName(name);
		newEnv.setStringValue(value);
		return newEnv;
	}

	public static void setApplicationName(JobDefinitionType value, String applicationName) {
		getOrCreateApplication(value).setApplicationName(applicationName);
	}

	public static void setApplicationVersion(JobDefinitionType value, String applicationVersion) {
		getOrCreateApplication(value).setApplicationVersion(applicationVersion);
	}

	public static void addProjectName(JobDefinitionType value, String projectName) {
		getOrCreateJobIdentification(value).addNewJobProject().setStringValue(projectName);
	}
	
	public static void addMultipleProjectNames(JobDefinitionType value, String[] projectNames) {
		for (String name : projectNames) {
			getOrCreateJobIdentification(value).addNewJobProject().setStringValue(name);
		}
	}

	public static void addCandidateHost(JobDefinitionType value, String host) {
		getOrCreateCandidateHosts(value).addHostName(host);

	}
	public static void addDataStagingTargetElement(JobDefinitionType value, String fileSystem, String file, String uri) {
		addDataStagingTargetElement(value,fileSystem, file, uri, 1);
	}

	public static void addDataStagingTargetElement(JobDefinitionType value, String fileSystem, String file, String uri, int flags) {
		JobDescriptionType jobDescr = getOrCreateJobDescription(value);
		DataStagingType newDS = jobDescr.addNewDataStaging();
		CreationFlagEnumeration.Enum creationFlag = CreationFlagEnumeration.DONT_OVERWRITE;
		if((flags & FLAG_OVERWRITE) != 0) creationFlag = CreationFlagEnumeration.OVERWRITE;
		if((flags & FLAG_APPEND) != 0) creationFlag = CreationFlagEnumeration.APPEND;
		boolean deleteOnTerminate = (flags & FLAG_DELETE_ON_TERMINATE) != 0;
		newDS.setCreationFlag(creationFlag);
		newDS.setDeleteOnTermination(deleteOnTerminate);
		SourceTargetType target = newDS.addNewTarget();

		try {
			if (uri != null) { 
				URIUtils.encodeAll(uri);
				target.setURI(uri);
			}
		} catch (URIException e) {
		}
		newDS.setFileName(file);
		if (fileSystem != null && !fileSystem.equals("Work")) {  //$NON-NLS-1$
			newDS.setFilesystemName(fileSystem);
		}
	}

	public static void addDataStagingSourceElement(JobDefinitionType value, String uri, String fileSystem, String file) {
		addDataStagingSourceElement(value, uri, fileSystem, file, 1);
	}

	public static void addDataStagingSourceElement(JobDefinitionType value, String uri, String fileSystem, String file, int flags) {
		JobDescriptionType jobDescr = getOrCreateJobDescription(value);

		try {
			uri = (uri == null) ? null : URIUtils.encodeAll(uri);
		} catch (URIException e) {
		}
		DataStagingType newDS = jobDescr.addNewDataStaging();
		CreationFlagEnumeration.Enum creationFlag = CreationFlagEnumeration.DONT_OVERWRITE;
		if((flags & FLAG_OVERWRITE) != 0) creationFlag = CreationFlagEnumeration.OVERWRITE;
		if((flags & FLAG_APPEND) != 0) creationFlag = CreationFlagEnumeration.APPEND;
		boolean deleteOnTerminate = (flags & FLAG_DELETE_ON_TERMINATE) != 0;
		newDS.setCreationFlag(creationFlag);
		newDS.setDeleteOnTermination(deleteOnTerminate);
		SourceTargetType source = newDS.addNewSource();
		source.setURI(uri);
		newDS.setFileName(file);
		if (fileSystem != null && !fileSystem.equals("Work")) {  //$NON-NLS-1$
			newDS.setFilesystemName(fileSystem);
		}
	}


	public static ApplicationType getOrCreateApplication(JobDefinitionType value) {
		JobDescriptionType jobDescr = getOrCreateJobDescription(value);
		if (!jobDescr.isSetApplication()) {
			jobDescr.addNewApplication();
		}
		return jobDescr.getApplication();
	}

	public static CandidateHostsType getOrCreateCandidateHosts(JobDefinitionType value) {
		ResourcesType resources = getOrCreateResources(value);
		if (!resources.isSetCandidateHosts()) {
			resources.addNewCandidateHosts();
		}
		return resources.getCandidateHosts();
	}

	public static CPUArchitectureType getOrCreateCPUArchitecture(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if (!jobResources.isSetCPUArchitecture()) {
			jobResources.addNewCPUArchitecture();
		}
		return jobResources.getCPUArchitecture();
	}

	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateIndividualCPUCount(JobDefinitionType value) {        
		ResourcesType jobResources = getOrCreateResources(value);
		if (!jobResources.isSetIndividualCPUCount()) {
			jobResources.addNewIndividualCPUCount();
		}
		return jobResources.getIndividualCPUCount();
	}


	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateIndividualCPUSpeed(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if (!jobResources.isSetIndividualCPUSpeed()) {
			jobResources.addNewIndividualCPUSpeed();
		}
		return jobResources.getIndividualCPUSpeed();
	}

	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateIndividualCPUTime(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if ( !jobResources.isSetIndividualCPUTime() ) {
			jobResources.addNewIndividualCPUTime();
		}
		return jobResources.getIndividualCPUTime();
	}

	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateIndividualDiskSpace(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if (!jobResources.isSetIndividualDiskSpace()) {
			jobResources.addNewIndividualDiskSpace();
		}
		return jobResources.getIndividualDiskSpace();
	}

	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateIndividualPhysicalMemory(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if (!jobResources.isSetIndividualPhysicalMemory()) {
			jobResources.addNewIndividualPhysicalMemory();
		}
		return jobResources.getIndividualPhysicalMemory();
	}

	public static JobDescriptionType getOrCreateJobDescription(JobDefinitionType value) {
		if (value.getJobDescription() == null) {
			return value.addNewJobDescription();
		}
		return value.getJobDescription();
	}

	public static JobIdentificationType getOrCreateJobIdentification(JobDefinitionType value) {
		JobDescriptionType descr = getOrCreateJobDescription(value);
		if (descr.getJobIdentification() == null) {
			return descr.addNewJobIdentification();
		}
		return descr.getJobIdentification();
	}

	public static OperatingSystemType getOrCreateOperatingSystem(JobDefinitionType value)
	{
		ResourcesType jobResources = getOrCreateResources(value);        
		if(!jobResources.isSetOperatingSystem()) {
			jobResources.addNewOperatingSystem();
		}
		return jobResources.getOperatingSystem();
	}

	public static ResourcesType getOrCreateResources(JobDefinitionType value) {
		JobDescriptionType jobDescr = getOrCreateJobDescription(value);
		if (!jobDescr.isSetResources()) {
			jobDescr.addNewResources();
		}
		return jobDescr.getResources();
	}


	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateTotalCPUCount(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if ( !jobResources.isSetTotalCPUCount() ) {
			jobResources.addNewTotalCPUCount();
		}
		return jobResources.getTotalCPUCount();
	}


	public static org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType getOrCreateTotalResourceCount(JobDefinitionType value) {

		ResourcesType jobResources = getOrCreateResources(value);
		if ( !jobResources.isSetTotalResourceCount())
		{
			jobResources.addNewTotalResourceCount();
		}
		return jobResources.getTotalResourceCount();
	}

	public static POSIXApplicationType getOrCreatePOSIXApplication(JobDefinitionType value) {
		
		ApplicationType application = getOrCreateApplication(value);
		
		if(getHPCProfileApplication(value) != null){
			//TODO handle: not creating POSIX element if HPCProfile already exists
			return getPOSIXApplication(value);
		}
		
		if (getPOSIXApplication(value) == null) {
			XmlCursor acursor = application.newCursor();
			acursor.toEndToken();
			acursor.insertElement(POSIX_APPLICATION);
			acursor.dispose();
		}
		return getPOSIXApplication(value);
	}

	
	public static SPMDApplicationType getOrCreateSPMDApplication(JobDefinitionType value) {
		
		ApplicationType application = getOrCreateApplication(value);
		
		if (getSPMDApplication(value) == null) {
			XmlCursor acursor = application.newCursor();
			acursor.toEndToken();
			acursor.insertElement(SPMD_APPLICATION);
			acursor.dispose();
		}
		return getSPMDApplication(value);
	}

	public static SPMDApplicationType getSPMDApplication(JobDefinitionType value) {
		if (value != null &&
				value.getJobDescription() != null && 
				value.getJobDescription().isSetApplication() ) {
			XmlCursor acursor = value.getJobDescription().getApplication().newCursor();
			if (acursor.toFirstChild()) {
				do {
					if(acursor.getName().equals(SPMD_APPLICATION)) {
						XmlObject result = acursor.getObject();
						acursor.dispose();
						return (SPMDApplicationType) result;
					}
				} while (acursor.toNextSibling());
				acursor.dispose();
				return null;
			} else {
				acursor.dispose();                               
				return null;
			}
		} else {
			return null;
		}
	}

	
	
	public static POSIXApplicationType getPOSIXApplication(JobDefinitionType value) {
		if (value != null &&
				value.getJobDescription() != null && 
				value.getJobDescription().isSetApplication() ) {
			XmlCursor acursor = value.getJobDescription().getApplication().newCursor();
			if (acursor.toFirstChild()) {
				do {
					if(acursor.getName().equals(POSIX_APPLICATION)) {
						XmlObject result = acursor.getObject();
						acursor.dispose();
						return (POSIXApplicationType) result;
					}
				} while (acursor.toNextSibling());
				acursor.dispose();
				return null;
			} else {
				acursor.dispose();                               
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	
	public static HPCProfileApplicationType getOrCreateHPCProfileApplication(JobDefinitionType value) {

		ApplicationType application = getOrCreateApplication(value);
		
		if(getPOSIXApplication(value) != null){
			//TODO handle: creating HPC element if POSIX already exists
			return getHPCProfileApplication(value);
		}
		
		if (getHPCProfileApplication(value) == null) {
			XmlCursor acursor = application.newCursor();
			acursor.toEndToken();
			acursor.insertElement(HPC_PROFILE_APPLICATION);
			acursor.dispose();
		}
		return getHPCProfileApplication(value);
	}

	
	public static HPCProfileApplicationType getHPCProfileApplication(JobDefinitionType value) {
		if (value != null &&
				value.getJobDescription() != null && 
				value.getJobDescription().isSetApplication() ) {
			XmlCursor acursor = value.getJobDescription().getApplication().newCursor();
			if (acursor.toFirstChild()) {
				do {
					if(acursor.getName().equals(HPC_PROFILE_APPLICATION)) {
						XmlObject result = acursor.getObject();
						acursor.dispose();
						return (HPCProfileApplicationType) result;
					}
				} while (acursor.toNextSibling());
				acursor.dispose();
				return null;
			} else {
				acursor.dispose();                               
				return null;
			}
		} else {
			return null;
		}
	}

	
	

	public static RangeValueType getTotalCPUCountRequirements(JobDefinitionType value) {
		if(value != null && value.getJobDescription() != null && value.getJobDescription().isSetResources() && 
				value.getJobDescription().getResources().isSetTotalCPUCount()){
			return toU6RangeValue(value.getJobDescription().getResources().getTotalCPUCount());
		}
		else
			return null;
	}

	public static RangeValueType getTotalResourceCountRequirements(JobDefinitionType value) {
		if(value != null && value.getJobDescription() != null && value.getJobDescription().isSetResources() && 
				value.getJobDescription().getResources().isSetTotalResourceCount()){
			return toU6RangeValue(value.getJobDescription().getResources().getTotalResourceCount());
		}
		else
			return null;
	}


	public static RangeValueType toU6RangeValue(org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType jsdlType) {
		RangeValueType result = new RangeValueType();
		if(jsdlType.getExactArray().length > 0){
			result.setExact(jsdlType.getExactArray(0).getDoubleValue());                
		}
		if(jsdlType.isSetLowerBoundedRange()){
			result.setLowerBound(jsdlType.getLowerBoundedRange().getDoubleValue());                
		}
		if(jsdlType.isSetUpperBoundedRange()){
			result.setUpperBound(jsdlType.getUpperBoundedRange().getDoubleValue());                
		}
		return result;
	}



	public static void setCPUArchitectureRequirements(JobDefinitionType value, ProcessorRequirement cpuArchitecture) { 
		if(cpuArchitecture == null || cpuArchitecture.getValue() == null) return;
		CPUArchitectureType cpuArch = getOrCreateCPUArchitecture(value);
		cpuArch.setCPUArchitectureName(ProcessorArchitectureEnumeration.Enum.forString(cpuArchitecture.getValue()));        
	}

	public static void setIndividualCPUCountRequirements(JobDefinitionType value, RangeValueType cpuCount) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType individualCPUCount = getOrCreateIndividualCPUCount(value);
		setRangeValue(cpuCount, individualCPUCount);
	}

	public static void setIndividualCPUSpeedRequirements(JobDefinitionType value, RangeValueType cpuSpeed) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType individualCPUSpeed = getOrCreateIndividualCPUSpeed(value);
		setRangeValue(cpuSpeed, individualCPUSpeed);
	}

	public static void setIndividualCPUTimeRequirements(JobDefinitionType value, RangeValueType cpuTime) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType cpuIndividualTime = getOrCreateIndividualCPUTime(value);       
		setRangeValue(cpuTime, cpuIndividualTime);
	}

	public static void setIndividualDiskSpaceRequirements(JobDefinitionType value, RangeValueType diskSpace) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType individualDiskSpace = getOrCreateIndividualDiskSpace(value);
		setRangeValue(diskSpace, individualDiskSpace);
	}

	public static void setIndividualPhysicalMemoryRequirements(JobDefinitionType value, RangeValueType physicalMemory) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType individualPhysicalMemory = getOrCreateIndividualPhysicalMemory(value);
		setRangeValue(physicalMemory, individualPhysicalMemory);
	}


	public static void setName(JobDefinitionType value, String name) {
		getOrCreateJobIdentification(value).setJobName(name);
	}


	public static void setRangeValue(RangeValueType u6Type, org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType jsdlType) {
		Double exact = u6Type.getExact();
		Double epsilon = u6Type.getEpsilon();
		Double lower = u6Type.getLowerBound();
		Double upper = u6Type.getUpperBound();


		if(lower.isNaN() && upper.isNaN())
		{
			ExactType exactType = jsdlType.getExactArray().length > 0 ? jsdlType.getExactArray(0) : jsdlType.addNewExact();
			exactType.setDoubleValue(exact);
			if(!epsilon.isNaN() && epsilon != 0)
			{
				exactType.setEpsilon(epsilon);
			}
		}
		else
		{
			if(!lower.isNaN())
			{
				BoundaryType lowerBound = jsdlType.isSetLowerBoundedRange() ? jsdlType.getLowerBoundedRange() : jsdlType.addNewLowerBoundedRange(); 
				lowerBound.setDoubleValue(lower);
				lowerBound.setExclusiveBound(!u6Type.isIncludeLowerBound());
			}

			if(!upper.isNaN())
			{
				BoundaryType upperBound = jsdlType.isSetUpperBoundedRange() ? jsdlType.getUpperBoundedRange() : jsdlType.addNewUpperBoundedRange();
				upperBound.setDoubleValue(upper);
				upperBound.setExclusiveBound(!u6Type.isIncludeUpperBound());
			}
		}
	}

	public static void setTotalCPUCountRequirements(JobDefinitionType value, RangeValueType cpuCount) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType cpuTotalCount = getOrCreateTotalCPUCount(value);        
		setRangeValue(cpuCount, cpuTotalCount);
	}

	public static void setTotalResourceCountRequirements(JobDefinitionType value, RangeValueType resourceCount) {
		org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType totalCount = getOrCreateTotalResourceCount(value);   
		setRangeValue(resourceCount, totalCount);
	}
}