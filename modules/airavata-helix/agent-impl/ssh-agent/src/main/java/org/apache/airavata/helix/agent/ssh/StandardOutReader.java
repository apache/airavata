package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.Channel;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class StandardOutReader implements CommandOutput {

    private String stdOut;
    private String stdError;
    private Integer exitCode;

    @Override
    public String getStdOut() {
        return this.stdOut;
    }

    @Override
    public String getStdError() {
        return this.stdError;
    }

    @Override
    public Integer getExitCode() {
        return this.exitCode;
    }

    public void readStdOutFromStream(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        this.stdOut = writer.toString();
    }

    public void readStdErrFromStream(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        this.stdError = writer.toString();
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }
}
