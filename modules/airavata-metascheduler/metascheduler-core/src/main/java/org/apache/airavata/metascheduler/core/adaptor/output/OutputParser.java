package org.apache.airavata.metascheduler.core.adaptor.output;

import org.apache.airavata.agents.api.CommandOutput;

/**
 * This interface is responsible for parsing output of agent adaptors and derive decisions
 */
public interface OutputParser {


    boolean isComputeResourceAvailable(CommandOutput commandOutput);



}
