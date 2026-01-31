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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Cluster information (partitions, accounts) as returned by slurminfo.sh and cached per credential.
 *
 * <p>Partition and account data are always specific to a (Slurm cluster, credential) pair.
 * Use this when creating groups from "projects available for accounts as found through each credential".
 */
public class ClusterInfo {
    private List<PartitionInfo> partitions = new ArrayList<>();
    private Instant fetchedAt;
    private Set<String> accounts = new LinkedHashSet<>();

    public ClusterInfo() {}

    public ClusterInfo(List<PartitionInfo> partitions, Instant fetchedAt, Set<String> accounts) {
        this.partitions = partitions != null ? new ArrayList<>(partitions) : new ArrayList<>();
        this.fetchedAt = fetchedAt;
        this.accounts = accounts != null ? new LinkedHashSet<>(accounts) : new LinkedHashSet<>();
    }

    public List<PartitionInfo> getPartitions() {
        return partitions == null ? Collections.emptyList() : Collections.unmodifiableList(partitions);
    }

    public void setPartitions(List<PartitionInfo> partitions) {
        this.partitions = partitions != null ? new ArrayList<>(partitions) : new ArrayList<>();
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public Set<String> getAccounts() {
        return accounts == null ? Collections.emptySet() : Collections.unmodifiableSet(accounts);
    }

    public void setAccounts(Set<String> accounts) {
        this.accounts = accounts != null ? new LinkedHashSet<>(accounts) : new LinkedHashSet<>();
    }

    public List<String> getAccountsList() {
        return new ArrayList<>(accounts != null ? accounts : Set.of());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterInfo that = (ClusterInfo) o;
        return Objects.equals(partitions, that.partitions)
                && Objects.equals(fetchedAt, that.fetchedAt)
                && Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitions, fetchedAt, accounts);
    }
}
