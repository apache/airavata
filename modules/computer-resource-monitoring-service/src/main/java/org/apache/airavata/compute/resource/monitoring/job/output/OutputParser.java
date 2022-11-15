package org.apache.airavata.compute.resource.monitoring.job.output;

import org.apache.airavata.agents.api.CommandOutput;

/**
 * This interface is responsible for parsing output of agent adaptors and derive decisions
 */
public interface OutputParser {


    boolean isComputeResourceAvailable(CommandOutput commandOutput);

    int getNumberofJobs(CommandOutput commandOutput);



}
