package org.apache.airavata.services.registry.rest.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.airavata.rest.mappings.utils.RestServicesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for webapp operations.
 */
public class WebAppUtil {

    protected static Logger log = LoggerFactory.getLogger(WebAppUtil.class);

//    public static Properties getAiravataProperties(ServletContext servletContext) throws IOException {
//
//        URL url = WebAppUtil.class.getClassLoader().
//                getResource(RestServicesConstants.AIRAVATA_SERVER_PROPERTIES);
//        Properties properties = new Properties();
//        try {
//            properties.load(url.openStream());
//        } catch (IOException e) {
//            log.error("Error reading Airavata properties");
//            throw e;
//        }
//
//        return properties;
//    }
}
