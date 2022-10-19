package org.apache.airavata.metascheduler.process.scheduling.engine.output;

import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.metascheduler.core.adaptor.output.OutputParser;

/**
 * This is parser output implementation
 */
public class OutputParserImpl implements OutputParser {


    @Override
    public boolean isComputeResourceAvailable(CommandOutput commandOutput) {
        return false;
    }


}
