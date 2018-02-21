package org.apache.airavata.agents.api;

import java.io.OutputStream;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface CommandOutput {

       String getStdOut();
       String getStdError();
       Integer getExitCode();
}
