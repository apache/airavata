package org.apache.airavata.registry.api.exception;

public class ServiceDescriptionRetrieveException extends Exception {
	private final static String ERROR_MESSAGE="Error occured while attempting to retrieve existing service descriptions";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2849422320139467602L;
	
	public ServiceDescriptionRetrieveException(Exception e) {
		super(ERROR_MESSAGE,e);
	}

}
