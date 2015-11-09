package org.apache.airavata.gfac.impl;

import com.jcraft.jsch.Channel;
import org.apache.airavata.gfac.core.cluster.CommandOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by syodage on 11/9/15.
 */
public class LocalCommandOutput implements CommandOutput {
    private Process process;

    @Override
    public void onOutput(Channel channel) {

    }

    public void readOutputs(Process process) {
        this.process = process;
    }

    public String getStandardOut() throws IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    public String getStandardErrorString() throws IOException {
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = stdError.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public OutputStream getStandardError() {
        return null;
    }

    @Override
    public void exitCode(int code) {

    }

    @Override
    public int getExitCode() {
        return 0;
    }
}
