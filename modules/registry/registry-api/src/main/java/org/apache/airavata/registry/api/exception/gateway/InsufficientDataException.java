package org.apache.airavata.registry.api.exception.gateway;

import org.apache.airavata.registry.api.exception.RegistryException;

public class InsufficientDataException extends RegistryException {

	private static final long serialVersionUID = 7706410845538952164L;

	public InsufficientDataException(String message) {
		super(message);
	}

}
