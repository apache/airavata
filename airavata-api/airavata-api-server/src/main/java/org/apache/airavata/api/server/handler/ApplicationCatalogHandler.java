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

package org.apache.airavata.api.server.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.airavata.api.appcatalog.ApplicationCatalogAPI.Iface;
import org.apache.airavata.api.appcatalog.applicationCatalogAPIConstants;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.model.appcatalog.ApplicationDeployment;
import org.apache.airavata.model.appcatalog.ApplicationDescriptor;
import org.apache.airavata.model.appcatalog.ApplicationInterface;
import org.apache.airavata.model.appcatalog.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.GSISSHJobSubmission;
import org.apache.airavata.model.appcatalog.GlobusJobSubmission;
import org.apache.airavata.model.appcatalog.GridFTPDataMovement;
import org.apache.airavata.model.appcatalog.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.SCPDataMovement;
import org.apache.airavata.model.appcatalog.SSHJobSubmission;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.ExportProperties;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.apache.thrift.TException;

public class ApplicationCatalogHandler implements Iface {

	private AiravataRegistry2 getRegistry() throws RegistryException, AiravataConfigurationException{
		return AiravataRegistryFactory.getRegistry(new Gateway("default"), new AiravataUser("admin"));
	}
	
	@Override
	public String GetAPIVersion() throws TException {
		return applicationCatalogAPIConstants.AIRAVATA_API_VERSION;
	}

	@Override
	public void addComputeResourceDescription(
			ComputeResourceDescription computeResourceDescription)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		HostDescription host = new HostDescription();
		host.getType().setHostName(generateId(computeResourceDescription.getHostName()));
		if (computeResourceDescription.getIpAddresses().size()>0){
			host.getType().setHostAddress(computeResourceDescription.getIpAddresses().iterator().next());	
		}
		if (computeResourceDescription.getJobSubmissionProtocolsSize()>0){
			String jobSubmissionProtocolDataId=computeResourceDescription.getJobSubmissionProtocols().keySet().iterator().next();
			JobSubmissionProtocol jobSubmissionProtocol = computeResourceDescription.getJobSubmissionProtocols().get(jobSubmissionProtocolDataId);
			switch(jobSubmissionProtocol){
			case SSH:
				SSHJobSubmission sshJobSubmissionProtocol = getSSHJobSubmissionProtocol(jobSubmissionProtocolDataId);
				host.getType().changeType(SSHHostType.type);
				//TODO fill the data
				break;
			case GRAM:
				GlobusJobSubmission globusJobSubmissionProtocol = getGlobusJobSubmissionProtocol(jobSubmissionProtocolDataId);
				host.getType().changeType(GlobusHostType.type);
				//TODO fill the data
				break;
			case GSISSH:
				GSISSHJobSubmission gsisshJobSubmissionProtocol = getGSISSHJobSubmissionProtocol(jobSubmissionProtocolDataId);
				host.getType().changeType(GsisshHostType.type);
				//TODO fill the data
				break;
			}
		}
		try {
			getRegistry().addHostDescriptor(host);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}
	
	private String generateId(
			String prefix) {
		return prefix+"_"+Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public List<String> listComputeResourceDescriptions()
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			List<HostDescription> hostDescriptors = getRegistry().getHostDescriptors();
			List<String> ids = new ArrayList<String>();
			for (HostDescription hostDescription : hostDescriptors) {
				ids.add(hostDescription.getType().getHostName());
			}
			return ids;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public ComputeResourceDescription getComputeResourceDescription(
			String computeResourceId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(computeResourceId);
			ComputeResourceDescription d = new ComputeResourceDescription();
			d.setResourceId(computeResourceId);
			d.getIpAddresses().add(hostDescriptor.getType().getHostAddress());
			if (hostDescriptor.getType() instanceof SSHHostType){
				d.getJobSubmissionProtocols().put(computeResourceId, JobSubmissionProtocol.SSH);
			} else if (hostDescriptor.getType() instanceof GsisshHostType){
				d.getJobSubmissionProtocols().put(computeResourceId, JobSubmissionProtocol.GSISSH);
			} else if (hostDescriptor.getType() instanceof GlobusHostType){
				d.getJobSubmissionProtocols().put(computeResourceId, JobSubmissionProtocol.GRAM);
			}
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
		
	}

	@Override
	public SSHJobSubmission getSSHJobSubmissionProtocol(
			String sshJobSubmissionProtocolResourceId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(sshJobSubmissionProtocolResourceId);
			SSHJobSubmission d = new SSHJobSubmission();
			d.setJobSubmissionDataID(sshJobSubmissionProtocolResourceId);
			if (hostDescriptor.getType() instanceof SSHHostType){
				d.setSshPort(22);
			} else { 
				throw new Exception("Saved job protocol is not SSH");
			}
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public GSISSHJobSubmission getGSISSHJobSubmissionProtocol(
			String gsisshJobSubmissionProtocolResourceId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(gsisshJobSubmissionProtocolResourceId);
			GSISSHJobSubmission d = new GSISSHJobSubmission();
			d.setJobSubmissionDataID(gsisshJobSubmissionProtocolResourceId);
			if (hostDescriptor.getType() instanceof GsisshHostType){
				GsisshHostType gsisshHostType = (GsisshHostType)hostDescriptor.getType();
				d.setInstalledPath(gsisshHostType.getInstalledPath());
				d.setMonitorMode(gsisshHostType.getMonitorMode());
				d.setPostJobCommands(Arrays.asList(gsisshHostType.getPostJobCommandsArray()));
				d.setPreJobCommands(Arrays.asList(gsisshHostType.getPreJobCommandsArray()));
				d.setSshPort(gsisshHostType.getPort());
			} else { 
				throw new Exception("Saved job protocol is not GSISSH");
			}
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public GlobusJobSubmission getGlobusJobSubmissionProtocol(
			String globusJobSubmissionProtocolResourceId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(globusJobSubmissionProtocolResourceId);
			GlobusJobSubmission d = new GlobusJobSubmission();
			d.setJobSubmissionDataID(globusJobSubmissionProtocolResourceId);
			if (hostDescriptor.getType() instanceof GlobusHostType){
				GlobusHostType globusHostType = (GlobusHostType)hostDescriptor.getType();
				d.setGlobusGateKeeperEndPoint(Arrays.asList(globusHostType.getGlobusGateKeeperEndPointArray()));
			} else { 
				throw new Exception("Saved job protocol is not Globus");
			}
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public SCPDataMovement getSCPDataMovementProtocol(
			String scpDataMovementResourceId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(scpDataMovementResourceId);
			SCPDataMovement d = new SCPDataMovement();
			d.setDataMovementDataID(scpDataMovementResourceId);
			if (hostDescriptor.getType() instanceof GlobusHostType){
				GlobusHostType globusHostType = (GlobusHostType)hostDescriptor.getType();
				d.setSshPort(22);
			} else { 
				throw new Exception("Saved job protocol is not GSISSH");
			}
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public boolean isComputeResourceDescriptionRegistered(String hostName)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			return getRegistry().isHostDescriptorExists(hostName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public ComputeResourceDescription getComputeResourceDescriptionFromHostName(
			String hostName) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplicationInterface(
			ApplicationInterface applicationInterface)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			ServiceDescription serviceDescription = ServiceDescription.fromXML(applicationInterface.getApplicationInterfaceData());
			getRegistry().addServiceDescriptor(serviceDescription);
			List<ApplicationDeployment> applicationDeployments = applicationInterface.getApplicationDeployments();
			for (ApplicationDeployment deployment : applicationDeployments) {
				String hostId = deployment.getComputeResourceDescription().getResourceId();
				ApplicationDescriptor applicationDescriptor = deployment.getApplicationDescriptor();
				getRegistry().addApplicationDescriptor(serviceDescription.getType().getName(), hostId, ApplicationDescription.fromXML(applicationDescriptor.getApplicationDescriptorData()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
		
	}

	@Override
	public List<String> listApplicationInterfaceIds()
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			List<String> ids=new ArrayList<String>();
			List<ServiceDescription> serviceDescriptors = getRegistry().getServiceDescriptors();
			for (ServiceDescription serviceDescription : serviceDescriptors) {
				ids.add(serviceDescription.getType().getName());
			}
			return ids;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public ApplicationInterface getApplicationInterface(
			String applicationInterfaceId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			ServiceDescription serviceDescriptor = getRegistry().getServiceDescriptor(applicationInterfaceId);
			ApplicationInterface applicationInterface = new ApplicationInterface();
			applicationInterface.setApplicationInterfaceId(applicationInterfaceId);
			applicationInterface.setApplicationInterfaceData(serviceDescriptor.toXML());
			Map<String, ApplicationDescription> applicationDescriptors = getRegistry().getApplicationDescriptors(applicationInterfaceId);
			for (String hostId : applicationDescriptors.keySet()) {
				ApplicationDeployment applicationDeployment = new ApplicationDeployment();
				applicationDeployment.setComputeResourceDescription(getComputeResourceDescription(hostId));
				ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor();
				applicationDescriptor.setApplicationDescriptorId(applicationDescriptors.get(hostId).getType().getApplicationName().getStringValue());
				applicationDescriptor.setApplicationDescriptorData(applicationDescriptors.get(hostId).toXML());
				applicationDeployment.setApplicationDescriptor(applicationDescriptor);
				applicationInterface.addToApplicationDeployments(applicationDeployment);
			}
			return applicationInterface;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public void addApplicationDeployment(String applicationInterfaceId,
			ApplicationDeployment applicationDeployment)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			getRegistry().addApplicationDescriptor(applicationInterfaceId, applicationDeployment.getComputeResourceDescription().getResourceId(), ApplicationDescription.fromXML(applicationDeployment.getApplicationDescriptor().getApplicationDescriptorData()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public List<String> listApplicationDeploymentIds(String applicationInterfaceId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
			List<String> ids=new ArrayList<String>();
			Map<String, ApplicationDescription> applicationDescriptors = getRegistry().getApplicationDescriptors(applicationInterfaceId);
			for (String hostId : applicationDescriptors.keySet()) {
				ids.add(applicationDescriptors.get(hostId).getType().getApplicationName().getStringValue());
			}
			return ids;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public ApplicationDeployment getApplicationDeployment(String applicationInterfaceId, 
			String applicationDeploymentId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			Map<String, ApplicationDescription> applicationDescriptors = getRegistry().getApplicationDescriptors(applicationInterfaceId);
			for (String hostId : applicationDescriptors.keySet()) {
				if (applicationDescriptors.get(hostId).getType().getApplicationName().getStringValue().equals(applicationDeploymentId)){
					ApplicationDeployment applicationDeployment = new ApplicationDeployment();
					applicationDeployment.setDeploymentId(applicationDescriptors.get(hostId).getType().getApplicationName().getStringValue());
					ApplicationDescriptor applicationDescriptor=new ApplicationDescriptor();
					applicationDescriptor.setApplicationDescriptorId(applicationDescriptors.get(hostId).getType().getApplicationName().getStringValue());
					applicationDescriptor.setApplicationDescriptorData(applicationDescriptors.get(hostId).toXML());
					applicationDeployment.setApplicationDescriptor(applicationDescriptor);
					applicationDeployment.setComputeResourceDescription(getComputeResourceDescription(hostId));
					return applicationDeployment;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public void addSSHJobSubmissionProtocol(String computeResourceId,
			SSHJobSubmission jobSubmission) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(computeResourceId);
			hostDescriptor.getType().changeType(SSHHostType.type);
			SSHHostType s = (SSHHostType)hostDescriptor.getType();
			getRegistry().updateHostDescriptor(hostDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
		
	}

	@Override
	public void addGSISSHJobSubmissionProtocol(String computeResourceId,
			GSISSHJobSubmission jobSubmission) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(computeResourceId);
			hostDescriptor.getType().changeType(GsisshHostType.type);
			GsisshHostType s = (GsisshHostType)hostDescriptor.getType();
			s.setInstalledPath(jobSubmission.getInstalledPath());
			ExportProperties exports = s.addNewExports();
			for(String export: jobSubmission.getExports()){
				exports.addNewName().setValue(export);
			}
			s.setExports(exports);
			s.setJobManager(jobSubmission.getResourceJobManager().toString());
			s.setMonitorMode(jobSubmission.getMonitorMode());
			s.setPort(22);
			s.setPostJobCommandsArray(jobSubmission.getPostJobCommands().toArray(new String[]{}));
			s.setPreJobCommandsArray(jobSubmission.getPreJobCommands().toArray(new String[]{}));
			getRegistry().updateHostDescriptor(hostDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public void addGlobusJobSubmissionProtocol(String computeResourceId,
			GlobusJobSubmission jobSubmission) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(computeResourceId);
			hostDescriptor.getType().changeType(GlobusHostType.type);
			GlobusHostType s = (GlobusHostType)hostDescriptor.getType();
			s.setGlobusGateKeeperEndPointArray(jobSubmission.getGlobusGateKeeperEndPoint().toArray(new String[]{}));
			getRegistry().updateHostDescriptor(hostDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
	}

	@Override
	public void addSCPDataMovementProtocol(String computeResourceId,
			SCPDataMovement dataMovement) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		try {
			HostDescription hostDescriptor = getRegistry().getHostDescriptor(computeResourceId);
			hostDescriptor.getType().changeType(GlobusHostType.type);
			GlobusHostType s = (GlobusHostType)hostDescriptor.getType();
//			s.setGlobusGateKeeperEndPointArray(dataMovement.getGlobusGateKeeperEndPoint().toArray(new String[]{}));
			getRegistry().updateHostDescriptor(hostDescriptor);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AiravataSystemException();
		}
		
	}

	@Override
	public void addGridFTPDataMovementProtocol(String computeResourceId,
			GridFTPDataMovement dataMovement) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, TException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GridFTPDataMovement getGridFTPDataMovementProtocol(
			String gridFTPDataMovementResourceId)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	

}
