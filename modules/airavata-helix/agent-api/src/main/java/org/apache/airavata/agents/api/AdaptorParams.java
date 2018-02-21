package org.apache.airavata.agents.api;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdaptorParams {

    public Object loadFromFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, this.getClass());
    }

    public void writeToFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, this);
    }
}
