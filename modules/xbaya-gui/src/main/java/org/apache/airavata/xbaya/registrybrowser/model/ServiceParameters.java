package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.schemas.gfac.Parameter;

public class ServiceParameters {
	private ServiceParameter[] parameters;
	
	public ServiceParameters(Parameter[] parameters) {
		List<ServiceParameter> serviceParaList=new ArrayList<ServiceParameter>();
		for (Parameter parameter : parameters) {
			serviceParaList.add(new ServiceParameter(parameter));
		}
		setParameters(serviceParaList.toArray(new ServiceParameter[]{}));
	}
	
	public ServiceParameters(ServiceParameter[] parameters) {
		setParameters(parameters);
	}
	public ServiceParameter[] getParameters() {
		return parameters;
	}
	public void setParameters(ServiceParameter[] parameters) {
		this.parameters = parameters;
	}
}
