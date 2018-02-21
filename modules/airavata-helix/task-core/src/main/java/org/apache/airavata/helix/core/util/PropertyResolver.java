package org.apache.airavata.helix.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class PropertyResolver {
    private Properties properties = new Properties();

    public void loadFromFile(File propertyFile) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
    }

    public void loadInputStream(InputStream inputStream) throws IOException {
        properties = new Properties();
        properties.load(inputStream);
    }

    public String get(String key) {
        if (properties.containsKey(key)) {
            if (System.getenv(key.replace(".", "_")) != null) {
                return System.getenv(key.replace(".", "_"));
            } else {
                return properties.getProperty(key);
            }
        } else {
            return null;
        }
    }

    public String get(String key, String defaultValue) {
        return Optional.ofNullable(get(key)).orElse(defaultValue);
    }
}
