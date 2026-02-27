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
package org.apache.airavata.compute.resource.submission;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.StatusModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OutputParser implementation for Airavata custom command output parsing.
 *
 * <p>This parser handles output from direct bash script execution on cloud VMs.
 * The jobID is typically a process ID (PID), and status is determined from `ps` command output.
 *
 * <p>Used by CloudJobManagerSpec for cloud VM job execution.
 */
public class CustomCommandOutputParser implements JobOutputParser {
    private static final Logger logger = LoggerFactory.getLogger(CustomCommandOutputParser.class);

    /**
     * Parse job ID (PID) from bash script execution output.
     * The output may contain the PID directly, or we may need to extract it from process info.
     *
     * @param rawOutput Output from bash script execution
     * @return Job ID (PID) as string, or empty string if not found
     */
    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
        logger.debug("Parsing job submission output: {}", rawOutput);
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            return "";
        }

        // Try to extract PID from output - common patterns:
        // 1. Just the PID number
        // 2. "PID: <number>"
        // 3. Process started message with PID
        Pattern pidPattern = Pattern.compile("(?:PID|pid|process)[\\s:]+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pidPattern.matcher(rawOutput);
        if (matcher.find()) {
            String pid = matcher.group(1);
            logger.info("Extracted PID from submission output: {}", pid);
            return pid;
        }

        // Try to match just a number (likely the PID)
        Pattern numberPattern = Pattern.compile("^\\s*(\\d+)\\s*$");
        matcher = numberPattern.matcher(rawOutput.trim());
        if (matcher.matches()) {
            String pid = matcher.group(1);
            logger.info("Extracted PID (number only): {}", pid);
            return pid;
        }

        logger.warn("Could not extract PID from submission output: {}", rawOutput);
        return "";
    }

    /**
     * Check if job submission failed by looking for error indicators.
     *
     * @param rawOutput Output from bash script execution
     * @return true if submission failed, false otherwise
     */
    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        if (rawOutput == null) {
            return false;
        }

        // Check for common error indicators
        String lowerOutput = rawOutput.toLowerCase();
        return lowerOutput.contains("error")
                || lowerOutput.contains("failed")
                || lowerOutput.contains("cannot")
                || lowerOutput.contains("permission denied")
                || lowerOutput.contains("no such file")
                || lowerOutput.contains("command not found");
    }

    /**
     * Parse job status from `ps -p <PID> -o stat=` output.
     * The output is a single character representing process state:
     * R=running, S=sleeping, T=stopped, Z=zombie, D=uninterruptible sleep, etc.
     *
     * @param jobID Process ID (PID)
     * @param rawOutput Output from `ps -p <PID> -o stat=` command
     * @return StatusModel<JobState> with parsed state, or UNKNOWN if process not found
     */
    @Override
    public StatusModel<JobState> parseJobStatus(String jobID, String rawOutput) throws Exception {
        logger.debug("Parsing job status for PID {}: {}", jobID, rawOutput);
        StatusModel<JobState> jobStatus = new StatusModel<>();

        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            // Empty output means process doesn't exist (completed or never started)
            jobStatus.setState(JobState.UNKNOWN);
            return jobStatus;
        }

        // ps -o stat= returns just the status character(s)
        // Common states: R (running), S (sleeping), T (stopped), Z (zombie), D (uninterruptible)
        String status = rawOutput.trim();
        if (status.isEmpty()) {
            jobStatus.setState(JobState.UNKNOWN);
            return jobStatus;
        }

        // Map ps status to JobState
        char firstChar = status.charAt(0);
        switch (firstChar) {
            case 'R': // Running
                jobStatus.setState(JobState.ACTIVE);
                break;
            case 'S': // Sleeping (interruptible)
            case 'D': // Uninterruptible sleep (usually I/O)
                jobStatus.setState(JobState.ACTIVE);
                break;
            case 'T': // Stopped
                jobStatus.setState(JobState.SUSPENDED);
                break;
            case 'Z': // Zombie (terminated but not reaped)
                // Zombie processes are essentially completed but not cleaned up
                jobStatus.setState(JobState.COMPLETED);
                break;
            default:
                // Unknown status character
                logger.warn("Unknown ps status character: {} for PID {}", firstChar, jobID);
                jobStatus.setState(JobState.UNKNOWN);
        }

        return jobStatus;
    }

    /**
     * Parse multiple job statuses from `ps -u <user>` output.
     * Parses the ps output format: PID STAT TIME COMMAND
     *
     * @param userName Username
     * @param statusMap Map of jobID -> StatusModel<JobState> to populate
     * @param rawOutput Output from `ps -u <user>` command
     */
    @Override
    public void parseJobStatuses(String userName, Map<String, StatusModel<JobState>> statusMap, String rawOutput)
            throws Exception {
        logger.debug("Parsing job statuses for user {}: {}", userName, rawOutput);
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            logger.info("No processes found for user {}", userName);
            return;
        }

        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Parse ps output: PID STAT TIME COMMAND
            // Format: "12345 R 0:00 /bin/bash script.sh"
            String[] parts = line.split("\\s+");
            if (parts.length < 2) {
                continue;
            }

            try {
                String pid = parts[0];
                String stat = parts[1];

                // Check if this PID is in our status map
                if (statusMap.containsKey(pid)) {
                    StatusModel<JobState> jobStatus = new StatusModel<>();
                    char firstChar = stat.charAt(0);
                    switch (firstChar) {
                        case 'R':
                        case 'S':
                        case 'D':
                            jobStatus.setState(JobState.ACTIVE);
                            break;
                        case 'T':
                            jobStatus.setState(JobState.SUSPENDED);
                            break;
                        case 'Z':
                            jobStatus.setState(JobState.COMPLETED);
                            break;
                        default:
                            jobStatus.setState(JobState.UNKNOWN);
                    }
                    statusMap.put(pid, jobStatus);
                    logger.debug("Updated status for PID {}: {}", pid, jobStatus.getState());
                }
            } catch (Exception e) {
                logger.warn("Error parsing ps line: {}", line, e);
            }
        }

        // Mark any PIDs not found in ps output as UNKNOWN (process completed or doesn't exist)
        for (String jobID : statusMap.keySet()) {
            if (statusMap.get(jobID) == null || statusMap.get(jobID).getState() == null) {
                StatusModel<JobState> jobStatus = new StatusModel<>();
                jobStatus.setState(JobState.UNKNOWN);
                statusMap.put(jobID, jobStatus);
            }
        }
    }

    /**
     * Parse job ID from `pgrep -u <user> -f <jobName>` output.
     * pgrep returns just the PID(s), one per line.
     *
     * @param jobName Job name/pattern to search for
     * @param rawOutput Output from pgrep command
     * @return First PID found, or null if not found
     */
    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        logger.debug("Parsing job ID for job name {}: {}", jobName, rawOutput);
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            return null;
        }

        // pgrep returns PID(s), one per line
        // Take the first PID found
        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                // Validate it's a number (PID)
                try {
                    Long.parseLong(line);
                    logger.info("Extracted PID from pgrep output: {}", line);
                    return line;
                } catch (NumberFormatException e) {
                    logger.warn("Invalid PID format in pgrep output: {}", line);
                }
            }
        }

        logger.warn("No valid PID found in pgrep output for job name: {}", jobName);
        return null;
    }
}
