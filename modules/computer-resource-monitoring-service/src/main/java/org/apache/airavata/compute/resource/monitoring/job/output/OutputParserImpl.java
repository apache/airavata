package org.apache.airavata.compute.resource.monitoring.job.output;

import org.apache.airavata.agents.api.CommandOutput;

/**
 * This is parser output implementation
 */
public class OutputParserImpl implements OutputParser {


    @Override
    public boolean isComputeResourceAvailable(CommandOutput commandOutput) {
        if (commandOutput.getExitCode() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int getNumberofJobs(CommandOutput commandOutput) {
        if (commandOutput.getStdOut() != null && !commandOutput.getStdOut().isEmpty()) {
            return Integer.parseInt(commandOutput.getStdOut());
        }
        return 0;
    }


}
