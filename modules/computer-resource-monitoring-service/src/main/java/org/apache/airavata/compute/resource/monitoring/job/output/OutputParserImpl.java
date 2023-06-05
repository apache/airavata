package org.apache.airavata.compute.resource.monitoring.job.output;

import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.compute.resource.monitoring.utils.Constants;

import java.util.Scanner;

/**
 * This is parser output implementation
 */
public class OutputParserImpl implements OutputParser {


    @Override
    public boolean isComputeResourceAvailable(CommandOutput commandOutput, String type) {
        if (commandOutput.getStdOut() != null && !commandOutput.getStdOut().isEmpty()) {
            if (type.equals(Constants.JOB_SUBMISSION_PROTOCOL_SLURM)) {
                Scanner scanner = new Scanner(commandOutput.getStdOut());
                if (scanner.hasNextLine()) {
                    String firstLine = scanner.nextLine();
                }
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] splittedString = line.split(" ");
                    for (String splitted : splittedString) {
                        if (splitted.trim().equals("up")) {
                            return true;
                        }
                    }
                }

            }

        }
        return false;
    }

    @Override
    public int getNumberofJobs(CommandOutput commandOutput, String type) {
        if (commandOutput.getStdOut() != null && !commandOutput.getStdOut().isEmpty()) {
            if (type.equals(Constants.JOB_SUBMISSION_PROTOCOL_SLURM)) {
                return Integer.parseInt(commandOutput.getStdOut().trim());
            }
        }
        return 0;
    }


}
