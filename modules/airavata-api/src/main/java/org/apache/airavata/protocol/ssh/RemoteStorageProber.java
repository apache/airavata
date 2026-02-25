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
package org.apache.airavata.protocol.ssh;

import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.storage.resource.model.StorageDirectoryInfo;
import org.apache.airavata.storage.resource.model.StorageVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Probes remote storage resources over SSH by executing {@code df} and {@code du} commands
 * through the supplied {@link AgentAdapter} and parsing their output.
 *
 * <p>This class is intentionally not a Spring component. It is instantiated on demand by
 * {@link SSHJAgentAdapter} and delegates all remote command execution back to the adapter.
 */
public class RemoteStorageProber {

    private static final Logger logger = LoggerFactory.getLogger(RemoteStorageProber.class);

    private final AgentAdapter adapter;

    public RemoteStorageProber(AgentAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Returns disk-volume statistics for the filesystem that contains {@code location}.
     *
     * <p>When {@code location} is {@code null} or blank the remote user's {@code $HOME}
     * directory is used as the probe target.
     *
     * @param location absolute path on the remote host, or {@code null} / empty for home dir
     * @return populated {@link StorageVolumeInfo}
     * @throws AgentException if the remote commands fail or their output cannot be parsed
     */
    public StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException {
        try {
            var targetLocation = location;
            if (targetLocation == null || targetLocation.trim().isEmpty()) {
                var homeOutput = adapter.executeCommand("echo $HOME", null);

                if (homeOutput.getExitCode() != 0
                        || homeOutput.getStdOut() == null
                        || homeOutput.getStdOut().trim().isEmpty()) {
                    logger.error("Failed to determine user's home directory: {}", homeOutput.getStdError());
                    throw new AgentException("Failed to determine user's home directory: " + homeOutput.getStdError());
                }
                targetLocation = homeOutput.getStdOut().trim();
            }

            // Escape location to prevent command injection and handle spaces
            var escapedLocation = targetLocation.replace("'", "'\"'\"'");
            var dfCommand = "df -P -T -h '" + escapedLocation + "'";
            var dfBytesCommand = "df -P -T '" + escapedLocation + "'";

            var dfHumanOutput = adapter.executeCommand(dfCommand, null);
            var dfBytesOutput = adapter.executeCommand(dfBytesCommand, null);

            if (dfHumanOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute df command for location {}: {}",
                        targetLocation,
                        dfHumanOutput.getStdError());
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfHumanOutput.getStdError());
            }

            if (dfBytesOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute df command for location {}: {}",
                        targetLocation,
                        dfBytesOutput.getStdError());
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfBytesOutput.getStdError());
            }

            return parseDfOutput(dfHumanOutput.getStdOut(), dfBytesOutput.getStdOut(), targetLocation);

        } catch (Exception e) {
            logger.error("Error while retrieving storage volume info for location " + location, e);
            throw new AgentException("Error while retrieving storage volume info for location " + location, e);
        }
    }

    /**
     * Returns the total disk usage for the directory tree rooted at {@code location}.
     *
     * <p>When {@code location} is {@code null} or blank the remote user's {@code $HOME}
     * directory is used as the probe target.
     *
     * @param location absolute path on the remote host, or {@code null} / empty for home dir
     * @return populated {@link StorageDirectoryInfo}
     * @throws AgentException if the remote command fails or its output cannot be parsed
     */
    public StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
        try {
            var targetLocation = location;
            if (targetLocation == null || targetLocation.trim().isEmpty()) {
                var homeOutput = adapter.executeCommand("echo $HOME", null);

                if (homeOutput.getExitCode() != 0
                        || homeOutput.getStdOut() == null
                        || homeOutput.getStdOut().trim().isEmpty()) {
                    logger.error("Failed to determine user's home directory: {}", homeOutput.getStdError());
                    throw new AgentException("Failed to determine user's home directory: " + homeOutput.getStdError());
                }
                targetLocation = homeOutput.getStdOut().trim();
            }

            // Escape location to prevent command injection and handle spaces
            var escapedLocation = targetLocation.replace("'", "'\"'\"'");
            var duKBytesCommand = "du -sk '" + escapedLocation + "'";

            var duKBytesOutput = adapter.executeCommand(duKBytesCommand, null);

            if (duKBytesOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute du -sk command for location {}: {}",
                        targetLocation,
                        duKBytesOutput.getStdError());
                throw new AgentException("Failed to execute du -sk command for location " + targetLocation + ": "
                        + duKBytesOutput.getStdError());
            }

            var outputKbStr = duKBytesOutput.getStdOut().trim();
            logger.info("OutputKbStr: for du -ku {} is {}", location, outputKbStr);
            var numberOfKBytesStr = outputKbStr.split(" ")[0];

            long numberOfKBytes = Long.parseLong(numberOfKBytesStr);

            var storageDirectoryInfo = new StorageDirectoryInfo();
            storageDirectoryInfo.setTotalSizeBytes(numberOfKBytes * 1024);
            storageDirectoryInfo.setTotalSize(numberOfKBytes + "kb");
            return storageDirectoryInfo;

        } catch (Exception e) {
            logger.error("Error while retrieving storage directory info for location " + location, e);
            throw new AgentException("Error while retrieving storage directory info for location " + location, e);
        }
    }

    /**
     * Parses the combined output of {@code df -P -T -h} (human-readable) and
     * {@code df -P -T} (block-count) for the same {@code targetLocation}.
     *
     * @param dfHumanOutput  stdout of {@code df -P -T -h}
     * @param dfBytesOutput  stdout of {@code df -P -T}
     * @param targetLocation the probed path (used only for error messages)
     * @return populated {@link StorageVolumeInfo}
     * @throws AgentException if the output cannot be parsed
     */
    private StorageVolumeInfo parseDfOutput(String dfHumanOutput, String dfBytesOutput, String targetLocation)
            throws AgentException {
        try {
            // Parse df -P -T -h output (POSIX format with filesystem type)
            var humanLines = dfHumanOutput.split("\n");
            var bytesLines = dfBytesOutput.split("\n");

            if (humanLines.length < 2 || bytesLines.length < 2) {
                logger.error(
                        "Unexpected df output format while parsing storage volume info for location {}",
                        targetLocation);
                throw new AgentException(
                        "Unexpected df output format while parsing storage volume info for location " + targetLocation);
            }

            // Skip the header line and get the data line
            var humanDataLine = humanLines[1].trim();
            var bytesDataLine = bytesLines[1].trim();

            // Split by whitespace. POSIX format uses fixed width columns separated by spaces
            var humanFields = humanDataLine.split("\\s+");
            var bytesFields = bytesDataLine.split("\\s+");

            if (humanFields.length < 7 || bytesFields.length < 7) {
                logger.error(
                        "Unexpected df output format - insufficient fields while parsing storage volume info for location {}",
                        targetLocation);
                throw new AgentException(
                        "Unexpected df output format - insufficient fields while parsing storage volume info for location "
                                + targetLocation);
            }

            var filesystemType = humanFields[1]; // ext4, xfs, etc.
            var totalSizeHuman = humanFields[2];
            var usedSizeHuman = humanFields[3];
            var availableSizeHuman = humanFields[4];
            var capacityStr = humanFields[5].replace("%", "");

            // If mount point contains spaces
            var mountPointBuilder = new StringBuilder();
            for (int i = 6; i < humanFields.length; i++) {
                if (i > 6) {
                    mountPointBuilder.append(" ");
                }
                mountPointBuilder.append(humanFields[i]);
            }
            var mountPoint = mountPointBuilder.toString();

            // Parse bytes output. Same format but in 1024-byte blocks
            var totalSizeBlocks = Long.parseLong(bytesFields[2]);
            var usedSizeBlocks = Long.parseLong(bytesFields[3]);
            var availableSizeBlocks = Long.parseLong(bytesFields[4]);

            // Convert 1024-byte blocks to bytes
            long totalSizeBytes = totalSizeBlocks * 1024L;
            long usedSizeBytes = usedSizeBlocks * 1024L;
            long availableSizeBytes = availableSizeBlocks * 1024L;

            double percentageUsed = Double.parseDouble(capacityStr);

            var volumeInfo = new StorageVolumeInfo();
            volumeInfo.setTotalSize(totalSizeHuman);
            volumeInfo.setUsedSize(usedSizeHuman);
            volumeInfo.setAvailableSize(availableSizeHuman);
            volumeInfo.setTotalSizeBytes(totalSizeBytes);
            volumeInfo.setUsedSizeBytes(usedSizeBytes);
            volumeInfo.setAvailableSizeBytes(availableSizeBytes);
            volumeInfo.setPercentageUsed(percentageUsed);
            volumeInfo.setMountPoint(mountPoint);
            volumeInfo.setFilesystemType(filesystemType);

            return volumeInfo;

        } catch (Exception e) {
            logger.error("Error parsing df output: {} for location {}", e.getMessage(), targetLocation, e);
            throw new AgentException(
                    "Error parsing df output: " + e.getMessage() + " for location " + targetLocation, e);
        }
    }
}
