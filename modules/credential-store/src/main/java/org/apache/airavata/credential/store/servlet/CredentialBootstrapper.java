package org.apache.airavata.credential.store.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.loader.ClientBootstrapper;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Bootstrapper class for credential-store.
 */
public class CredentialBootstrapper extends ClientBootstrapper {

    public ConfigurationLoader getConfigurationLoader(ServletContext servletContext)
            throws Exception {

        File currentDirectory = new File(".");
        System.out.println("Current directory is - " + currentDirectory.getAbsolutePath());


        return super.getConfigurationLoader(servletContext);


    }


}
