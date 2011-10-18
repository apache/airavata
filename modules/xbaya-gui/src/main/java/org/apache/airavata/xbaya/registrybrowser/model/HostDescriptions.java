package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;

public class HostDescriptions {
	private Registry registry;
	
	public HostDescriptions(Registry registry){
		setRegistry(registry);
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
	
	public List<HostDescription> getDescriptions() throws HostDescriptionRetrieveException{
		try {
			return getRegistry().searchHostDescription(".*");
		} catch (PathNotFoundException e) {
			//has no descriptions defined
			return new ArrayList<HostDescription>();
		} 
	}
}
