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
package org.apache.airavata.orchestration.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.credential.service.CredentialStoreService;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.credential.store.proto.CredentialSummary;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.apache.airavata.model.credential.store.proto.SummaryType;
import org.apache.airavata.orchestration.service.RegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Cold-start initializer that registers the dev SLURM compute resource (the docker-slurm cluster) plus a
 * group resource profile and group compute resource preference, so experiments can be scheduled against
 * it out of the box. Mirrors {@link DevStorageInitializer}.
 *
 * <p>Transport is always SSH; the resource carries the SSH port directly and a SLURM
 * {@link ResourceJobManager} (sbatch/squeue/scancel). The SSH credential is the same dev keypair
 * {@code DevStorageInitializer} uses (conf/sftp/), reused by description if already registered.
 *
 * <p>Idempotent: skips if a compute resource with host {@code slurm} already exists.
 */
@Component
public class DevComputeInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DevComputeInitializer.class);

    // The server reaches the docker-slurm submit host by its compose service name, not "localhost".
    private static final String COMPUTE_HOST = "slurm";
    private static final String COMPUTE_DESCRIPTION = "Dev SLURM cluster (docker)";
    private static final String LOGIN_USER = "airavata";
    private static final String SCRATCH_LOCATION = "/home/airavata";
    private static final String JOB_MANAGER_BIN_PATH = "/usr/bin";
    private static final String GROUP_PROFILE_NAME = "Dev SLURM Group Resource Profile";
    // Matches the credential description DevStorageInitializer registers, so both share one dev credential.
    private static final String CREDENTIAL_DESCRIPTION = "Dev SFTP";

    @Value("${dev.sftp.private-key-path:conf/sftp/id_rsa}")
    private String privateKeyPath;

    @Value("${dev.sftp.public-key-path:conf/sftp/id_rsa.pub}")
    private String publicKeyPath;

    @Autowired
    private RegistryServerHandler registryHandler;

    @Autowired
    private CredentialStoreService credentialStoreService;

    // Run after the default gateway exists (same ordering rationale as DevStorageInitializer).
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            String gatewayId = ServerSettings.getDefaultUserGateway();

            // Idempotency: skip if a compute resource for this host already exists.
            for (var entry : registryHandler.getAllComputeResourceNames().entrySet()) {
                if (COMPUTE_HOST.equals(entry.getValue())) {
                    logger.info("Dev compute resource already exists: {} ({})", entry.getValue(), entry.getKey());
                    return;
                }
            }

            // 1. SLURM resource job manager: sbatch / squeue / scancel.
            ResourceJobManager resourceJobManager = ResourceJobManager.newBuilder()
                    .setResourceJobManagerType(ResourceJobManagerType.SLURM)
                    .setJobManagerBinPath(JOB_MANAGER_BIN_PATH)
                    .putJobManagerCommands(JobManagerCommand.SUBMISSION_VALUE, "sbatch")
                    .putJobManagerCommands(JobManagerCommand.JOB_MONITORING_VALUE, "squeue")
                    .putJobManagerCommands(JobManagerCommand.DELETION_VALUE, "scancel")
                    .build();

            // 2. Compute resource: host slurm, SSH port 22, SLURM job manager.
            ComputeResourceDescription computeResource = ComputeResourceDescription.newBuilder()
                    .setHostName(COMPUTE_HOST)
                    .setResourceDescription(COMPUTE_DESCRIPTION)
                    .setEnabled(true)
                    .setSshPort(22)
                    .setResourceJobManager(resourceJobManager)
                    .build();
            String computeResourceId = registryHandler.registerComputeResource(computeResource);
            logger.info("Registered dev compute resource: {} ({})", COMPUTE_HOST, computeResourceId);

            // 3. Dev SSH credential — reuse the one DevStorageInitializer registered (by description), else mint.
            String credentialToken = findExistingDevCredentialToken(gatewayId);
            if (credentialToken == null) {
                credentialToken = registerDevCredential(gatewayId);
            }
            if (credentialToken == null) {
                logger.warn("No dev SSH credential available; group compute preference will lack a credential token");
                credentialToken = "";
            }

            // 4. Group resource profile with an embedded group compute resource preference (SLURM, login airavata).
            GroupComputeResourcePreference computePreference = GroupComputeResourcePreference.newBuilder()
                    .setComputeResourceId(computeResourceId)
                    .setResourceType(ResourceType.SLURM)
                    .setLoginUserName(LOGIN_USER)
                    .setScratchLocation(SCRATCH_LOCATION)
                    .setResourceSpecificCredentialStoreToken(credentialToken)
                    .build();

            String groupResourceProfileId = findOrCreateGroupResourceProfile(gatewayId, computePreference);
            logger.info(
                    "Dev group resource profile {} now references compute resource {}",
                    groupResourceProfileId,
                    computeResourceId);

            logger.info("Dev compute cold-start initialization complete");

        } catch (Exception e) {
            logger.warn("Dev compute initialization failed (non-fatal): {}", e.getMessage());
            logger.debug("Dev compute init error details:", e);
        }
    }

    /** Reuse an existing dev SSH credential (by description) so storage + compute share one token. */
    private String findExistingDevCredentialToken(String gatewayId) {
        try {
            List<CredentialSummary> summaries =
                    credentialStoreService.getAllCredentialSummaryForGateway(SummaryType.SSH, gatewayId);
            for (CredentialSummary summary : summaries) {
                if (CREDENTIAL_DESCRIPTION.equals(summary.getDescription())) {
                    logger.info("Reusing existing dev SSH credential token {}", summary.getToken());
                    return summary.getToken();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to look up existing dev SSH credentials: {}", e.getMessage());
        }
        return null;
    }

    /** Register a dev SSH credential from the Tiltfile-generated keypair. */
    private String registerDevCredential(String gatewayId) {
        String privateKey = readKeyFile(privateKeyPath);
        String publicKey = readKeyFile(publicKeyPath);
        if (privateKey == null || publicKey == null) {
            logger.warn("SSH keypair not found at {} / {} — run 'tilt up' to generate", privateKeyPath, publicKeyPath);
            return null;
        }
        try {
            String defaultUser = ServerSettings.getDefaultUser();
            SSHCredential sshCredential = SSHCredential.newBuilder()
                    .setUsername(defaultUser)
                    .setGatewayId(gatewayId)
                    .setDescription(CREDENTIAL_DESCRIPTION)
                    .setPrivateKey(privateKey)
                    .setPublicKey(publicKey)
                    .build();
            String token = credentialStoreService.addSSHCredential(sshCredential);
            logger.info("Registered SSH credential from {} for dev compute: {}", privateKeyPath, token);
            return token;
        } catch (Exception e) {
            logger.warn("Failed to register dev SSH credential: {}", e.getMessage());
            return null;
        }
    }

    /** Reuse the gateway's existing group resource profile (adding the SLURM preference), else create one. */
    private String findOrCreateGroupResourceProfile(String gatewayId, GroupComputeResourcePreference computePreference)
            throws Exception {
        List<GroupResourceProfile> existing = registryHandler.getGroupResourceList(gatewayId, List.of());
        if (existing != null && !existing.isEmpty()) {
            GroupResourceProfile profile = existing.get(0);
            GroupComputeResourcePreference pref = computePreference.toBuilder()
                    .setGroupResourceProfileId(profile.getGroupResourceProfileId())
                    .build();
            GroupResourceProfile updated =
                    profile.toBuilder().addComputePreferences(pref).build();
            registryHandler.updateGroupResourceProfile(updated);
            return profile.getGroupResourceProfileId();
        }
        GroupResourceProfile profile = GroupResourceProfile.newBuilder()
                .setGatewayId(gatewayId)
                .setGroupResourceProfileName(GROUP_PROFILE_NAME)
                .addComputePreferences(computePreference)
                .build();
        return registryHandler.createGroupResourceProfile(profile);
    }

    private String readKeyFile(String path) {
        try {
            Path keyPath = Path.of(path);
            if (Files.exists(keyPath)) {
                return Files.readString(keyPath).trim();
            }
        } catch (IOException e) {
            logger.warn("Failed to read key file {}: {}", path, e.getMessage());
        }
        return null;
    }
}
