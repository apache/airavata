/**
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
package org.apache.airavata.service.cluster;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.accountprovisioning.SSHUtil;
import org.apache.airavata.common.model.ClusterInfo;
import org.apache.airavata.common.model.PartitionInfo;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnApiService;
import org.apache.airavata.registry.entities.appcatalog.CredentialClusterInfoEntity;
import org.apache.airavata.registry.repositories.appcatalog.CredentialClusterInfoRepository;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to fetch SLURM cluster information (partitions, accounts) on demand by executing
 * slurminfo.sh on the remote host via SSH using a credential.
 */
@Service
@ConditionalOnApiService
public class ClusterInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterInfoService.class);

    private static final String SLURMINFO_RESOURCE = "conf/bin/slurminfo.sh";
    private static final String HEADER_LINE = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts";

    private static final TypeReference<List<PartitionInfo>> PARTITION_LIST_TYPE = new TypeReference<>() {};

    private final CredentialStoreService credentialStoreService;
    private final CredentialClusterInfoRepository clusterInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClusterInfoService(
            CredentialStoreService credentialStoreService,
            CredentialClusterInfoRepository clusterInfoRepository) {
        this.credentialStoreService = credentialStoreService;
        this.clusterInfoRepository = clusterInfoRepository;
    }

    /**
     * Fetch cluster info by running slurminfo.sh on the given host using the specified credential.
     * Results are cached in the database.
     *
     * @param credentialToken   credential token (SSH)
     * @param gatewayId         gateway ID
     * @param computeResourceId compute resource ID (for cache key)
     * @param hostname          SSH hostname
     * @param port              SSH port (e.g. 22)
     * @return parsed cluster info (partitions, accounts, fetchedAt)
     */
    public ClusterInfo fetchClusterInfo(
            String credentialToken,
            String gatewayId,
            String computeResourceId,
            String hostname,
            int port,
            String loginUsername)
            throws CredentialStoreException {
        SSHCredential cred = credentialStoreService.getSSHCredential(credentialToken, gatewayId);
        if (cred == null) {
            throw new CredentialStoreException(
                    "SSH credential not found for token=" + credentialToken + ", gatewayId=" + gatewayId);
        }
        if (loginUsername == null || loginUsername.isBlank()) {
            throw new CredentialStoreException("Login username is required (pass loginUsername in request; it is set per resource in the access grant).");
        }
        String username = loginUsername;

        String scriptContent = loadSlurminfoScript();
        if (scriptContent == null || scriptContent.isBlank()) {
            throw new IllegalStateException("slurminfo.sh not found on classpath: " + SLURMINFO_RESOURCE);
        }

        String command = buildRemoteCommand(scriptContent);
        String output;
        try {
            output = SSHUtil.execute(hostname, port, username, cred, command);
        } catch (RuntimeException e) {
            logger.warn("SSH execution of slurminfo.sh failed: {}", e.getMessage());
            throw e;
        }

        ClusterInfo info = parseSlurminfoOutput(output);
        info.setFetchedAt(Instant.now());
        saveCached(gatewayId, credentialToken, computeResourceId, output, info);
        return info;
    }

    /**
     * Get cached cluster info for the given credential and compute resource, if any.
     */
    public Optional<ClusterInfo> getCached(String gatewayId, String credentialToken, String computeResourceId) {
        return clusterInfoRepository
                .findByGatewayIdAndCredentialTokenAndComputeResourceId(
                        gatewayId, credentialToken, computeResourceId)
                .map(this::entityToModel);
    }

    /**
     * Invalidate cached cluster info for the given credential and compute resource.
     */
    public void deleteCached(String gatewayId, String credentialToken, String computeResourceId) {
        clusterInfoRepository.deleteByGatewayIdAndCredentialTokenAndComputeResourceId(
                gatewayId, credentialToken, computeResourceId);
    }

    private void saveCached(
            String gatewayId, String credentialToken, String computeResourceId, String rawOutput, ClusterInfo info) {
        try {
            if (gatewayId == null || gatewayId.isBlank()) {
                logger.warn("Cannot cache cluster info without gatewayId; skipping");
                return;
            }
            CredentialClusterInfoEntity entity = new CredentialClusterInfoEntity();
            entity.setGatewayId(gatewayId);
            entity.setCredentialToken(credentialToken);
            entity.setComputeResourceId(computeResourceId);
            entity.setFetchedAt(Timestamp.from(info.getFetchedAt()));
            entity.setRawOutput(rawOutput);
            entity.setPartitionsJson(objectMapper.writeValueAsString(info.getPartitions()));
            entity.setAccountsJson(objectMapper.writeValueAsString(info.getAccountsList()));
            clusterInfoRepository.save(entity);
        } catch (Exception e) {
            logger.warn("Failed to cache cluster info: {}", e.getMessage());
        }
    }

    private ClusterInfo entityToModel(CredentialClusterInfoEntity entity) {
        ClusterInfo info = new ClusterInfo();
        info.setFetchedAt(entity.getFetchedAt() != null ? entity.getFetchedAt().toInstant() : null);
        try {
            if (entity.getPartitionsJson() != null && !entity.getPartitionsJson().isBlank()) {
                List<PartitionInfo> parts = objectMapper.readValue(entity.getPartitionsJson(), PARTITION_LIST_TYPE);
                info.setPartitions(parts);
            }
            if (entity.getAccountsJson() != null && !entity.getAccountsJson().isBlank()) {
                List<String> accts = objectMapper.readValue(
                        entity.getAccountsJson(), new TypeReference<List<String>>() {});
                info.setAccounts(new LinkedHashSet<>(accts));
            }
        } catch (Exception e) {
            logger.warn("Failed to deserialize cached cluster info: {}", e.getMessage());
        }
        return info;
    }

    private String loadSlurminfoScript() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(SLURMINFO_RESOURCE)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Failed to load slurminfo.sh from classpath: {}", e.getMessage());
            return null;
        }
    }

    private static String buildRemoteCommand(String scriptContent) {
        String base64 = Base64.getEncoder().encodeToString(scriptContent.getBytes(StandardCharsets.UTF_8));
        return "echo " + base64 + " | base64 -d | sh";
    }

    /**
     * Parse pipe-delimited output from slurminfo.sh.
     * Format: partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts
     * First line is header; subsequent lines are data.
     */
    static ClusterInfo parseSlurminfoOutput(String output) {
        ClusterInfo info = new ClusterInfo();
        List<PartitionInfo> partitions = new ArrayList<>();
        Set<String> allAccounts = new LinkedHashSet<>();

        if (output == null || output.isBlank()) {
            info.setPartitions(partitions);
            info.setAccounts(allAccounts);
            return info;
        }

        String[] lines = output.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (HEADER_LINE.equals(line)) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length < 5) {
                continue;
            }
            PartitionInfo p = new PartitionInfo();
            p.setPartitionName(parts[0].trim());
            p.setNodeCount(parseInt(parts[1], 0));
            p.setMaxCpusPerNode(parseInt(parts[2], 0));
            p.setMaxGpusPerNode(parseInt(parts[3], 0));
            String accountsCsv = parts[4].trim();
            p.setAccountsFromCsv(accountsCsv);
            if (!accountsCsv.isBlank()) {
                for (String a : accountsCsv.split(",\\s*")) {
                    String t = a.trim();
                    if (!t.isEmpty()) {
                        allAccounts.add(t);
                    }
                }
            }
            partitions.add(p);
        }

        info.setPartitions(partitions);
        info.setAccounts(allAccounts);
        return info;
    }

    private static int parseInt(String s, int defaultValue) {
        if (s == null || s.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
