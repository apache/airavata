package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.schemas.gfac.Parameter;

public class ServiceParameters {
	private List<ServiceParameter> parameters;
	
	public ServiceParameters(Parameter[] parameters) {
		if (parameters!=null) {
			List<ServiceParameter> serviceParaList = new ArrayList<ServiceParameter>();
			for (Parameter parameter : parameters) {
				serviceParaList.add(new ServiceParameter(parameter));
			}
			setParameters(serviceParaList);
		}
	}
	
	public ServiceParameters(ServiceParameter[] parameters) {
		if (parameters!=null) {
			setParameters(Arrays.asList(parameters));
		}
	}
	public List<ServiceParameter> getParameters() {
		if (parameters==null){
			parameters=new ArrayList<ServiceParameter>();
		}
		return parameters;
	}
	public void setParameters(List<ServiceParameter> parameters) {
		this.parameters = parameters;
	}
}
