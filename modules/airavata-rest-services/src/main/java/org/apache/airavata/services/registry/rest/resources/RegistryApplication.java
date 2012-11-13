package org.apache.airavata.services.registry.rest.resources;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RegistryApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resource
        classes.add(RegistryResource.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return super.getSingletons();
    }

}
