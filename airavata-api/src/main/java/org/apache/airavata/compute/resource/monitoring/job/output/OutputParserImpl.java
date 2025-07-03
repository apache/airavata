/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.compute.resource.monitoring.job.output;

import java.util.Scanner;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.compute.resource.monitoring.utils.Constants;

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
