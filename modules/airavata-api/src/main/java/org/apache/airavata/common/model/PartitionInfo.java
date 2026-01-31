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
package org.apache.airavata.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SLURM partition information as returned by slurminfo.sh.
 */
public class PartitionInfo {
    private String partitionName;
    private int nodeCount;
    private int maxCpusPerNode;
    private int maxGpusPerNode;
    private List<String> accounts = new ArrayList<>();

    public PartitionInfo() {}

    public PartitionInfo(String partitionName, int nodeCount, int maxCpusPerNode, int maxGpusPerNode, List<String> accounts) {
        this.partitionName = partitionName;
        this.nodeCount = nodeCount;
        this.maxCpusPerNode = maxCpusPerNode;
        this.maxGpusPerNode = maxGpusPerNode;
        this.accounts = accounts != null ? new ArrayList<>(accounts) : new ArrayList<>();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getMaxCpusPerNode() {
        return maxCpusPerNode;
    }

    public void setMaxCpusPerNode(int maxCpusPerNode) {
        this.maxCpusPerNode = maxCpusPerNode;
    }

    public int getMaxGpusPerNode() {
        return maxGpusPerNode;
    }

    public void setMaxGpusPerNode(int maxGpusPerNode) {
        this.maxGpusPerNode = maxGpusPerNode;
    }

    public List<String> getAccounts() {
        return accounts == null ? Collections.emptyList() : Collections.unmodifiableList(accounts);
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts != null ? new ArrayList<>(accounts) : new ArrayList<>();
    }

    /**
     * Parse accounts from comma-separated string (as in slurminfo.sh output).
     */
    public void setAccountsFromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            this.accounts = new ArrayList<>();
            return;
        }
        this.accounts = Arrays.asList(csv.split(",\\s*"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartitionInfo that = (PartitionInfo) o;
        return nodeCount == that.nodeCount
                && maxCpusPerNode == that.maxCpusPerNode
                && maxGpusPerNode == that.maxGpusPerNode
                && Objects.equals(partitionName, that.partitionName)
                && Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionName, nodeCount, maxCpusPerNode, maxGpusPerNode, accounts);
    }
}
