package org.apache.airavata.registry.api.exception;

public class HostDescriptionRetrieveException extends Exception {
    private final static String ERROR_MESSAGE = "Error occured while attempting to retrieve existing hosts";
    /**
	 * 
	 */
    private static final long serialVersionUID = -2849422320139467602L;

    public HostDescriptionRetrieveException(Exception e) {
        super(ERROR_MESSAGE, e);
    }

}
