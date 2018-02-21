package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.Channel;
import org.apache.airavata.agents.api.CommandOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class StandardOutReader implements CommandOutput {

    // Todo improve this. We need to direct access of std out and exit code

    String stdOutputString = null;
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    private int exitCode;

    public void onOutput(Channel channel) {
        try {
            StringBuffer pbsOutput = new StringBuffer("");
            InputStream inputStream =  channel.getInputStream();
            byte[] tmp = new byte[1024];
            do {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0) break;
                    pbsOutput.append(new String(tmp, 0, i));
                }
            } while (!channel.isClosed()) ;
            String output = pbsOutput.toString();
            this.setStdOutputString(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exitCode(int code) {
        System.out.println("Program exit code - " + code);
        this.exitCode = code;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStdOutputString() {
        return stdOutputString;
    }

    public void setStdOutputString(String stdOutputString) {
        this.stdOutputString = stdOutputString;
    }

    public String getStdErrorString() {
        return errorStream.toString();
    }

    public OutputStream getStandardError() {
        return errorStream;
    }

    @Override
    public String getStdOut() {
        return null;
    }

    @Override
    public String getStdError() {
        return null;
    }

    @Override
    public String getExitCommand() {
        return null;
    }
}
