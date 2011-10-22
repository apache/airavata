package org.apache.airavata.registry.api.exception;

public class DeploymentDescriptionRetrieveException extends Exception {
    private final static String ERROR_MESSAGE = "Error occured while attempting to retrieve existing deployment descriptions";
    /**
	 * 
	 */
    private static final long serialVersionUID = -2849422320139467602L;

    public DeploymentDescriptionRetrieveException(Exception e) {
        super(ERROR_MESSAGE, e);
    }

}
