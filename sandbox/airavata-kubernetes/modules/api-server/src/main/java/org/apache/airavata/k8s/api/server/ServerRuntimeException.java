package org.apache.airavata.k8s.api.server;

import org.apache.airavata.k8s.api.server.model.experiment.Experiment;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ServerRuntimeException extends RuntimeException {
    public ServerRuntimeException(Exception e) {
        super(e);
    }

    public ServerRuntimeException(String message) {
        super(message);
    }

    public ServerRuntimeException(String message, Exception e) {
        super(message, e);
    }
}
