package org.apache.airavata.xbaya.registrybrowser.model;

import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.schemas.gfac.ParameterType;


public class ServiceParameter {
	private Parameter parameter;
	private Object value;
	
	public ServiceParameter(Parameter parameter) {
		this(parameter,null);
	}
	
	public ServiceParameter(Parameter parameter, Object value) {
		setParameter(parameter);
		setValue(value);
	}
	public Parameter getParameter() {
		return parameter;
	}
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getName(){
		return getParameter().getName();
	}
	
	public String getDescription(){
		return getParameter().getDescription();
	}
	
	public ParameterType getType(){
		return getParameter().getParameterType().getType();
	}
}

