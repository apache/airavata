/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ClusterInfo;
import org.apache.airavata.common.model.PartitionInfo;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ClusterInfoService, especially parseSlurminfoOutput (slurminfo.sh output parsing).
 */
class ClusterInfoServiceTest {

    @Test
    void parseSlurminfoOutput_emptyOrNull_returnsEmptyInfo() {
        ClusterInfo empty = ClusterInfoService.parseSlurminfoOutput("");
        assertNotNull(empty);
        assertTrue(empty.getPartitions() == null || empty.getPartitions().isEmpty());
        assertTrue(empty.getAccountsList() == null || empty.getAccountsList().isEmpty());

        ClusterInfo nullOut = ClusterInfoService.parseSlurminfoOutput(null);
        assertNotNull(nullOut);
        assertTrue(nullOut.getPartitions() == null || nullOut.getPartitions().isEmpty());
    }

    @Test
    void parseSlurminfoOutput_headerOnly_returnsEmptyPartitions() {
        String output = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts";
        ClusterInfo info = ClusterInfoService.parseSlurminfoOutput(output);
        assertNotNull(info);
        assertTrue(info.getPartitions().isEmpty());
        assertTrue(info.getAccountsList().isEmpty());
    }

    @Test
    void parseSlurminfoOutput_singlePartition_parsesCorrectly() {
        String output = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts\n"
                + "normal|10|24|0|myaccount";
        ClusterInfo info = ClusterInfoService.parseSlurminfoOutput(output);
        assertNotNull(info);
        List<PartitionInfo> parts = info.getPartitions();
        assertEquals(1, parts.size());
        PartitionInfo p = parts.get(0);
        assertEquals("normal", p.getPartitionName());
        assertEquals(10, p.getNodeCount());
        assertEquals(24, p.getMaxCpusPerNode());
        assertEquals(0, p.getMaxGpusPerNode());
        assertEquals(1, p.getAccounts().size());
        assertEquals("myaccount", p.getAccounts().get(0));
        assertTrue(info.getAccountsList().contains("myaccount"));
    }

    @Test
    void parseSlurminfoOutput_multiplePartitions_parsesAll() {
        String output = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts\n"
                + "small|2|8|0|\n"
                + "gpu|4|16|2|gpu-acct,other";
        ClusterInfo info = ClusterInfoService.parseSlurminfoOutput(output);
        assertNotNull(info);
        List<PartitionInfo> parts = info.getPartitions();
        assertEquals(2, parts.size());
        assertEquals("small", parts.get(0).getPartitionName());
        assertEquals(2, parts.get(0).getNodeCount());
        assertEquals(8, parts.get(0).getMaxCpusPerNode());
        assertEquals(0, parts.get(0).getMaxGpusPerNode());
        assertEquals("gpu", parts.get(1).getPartitionName());
        assertEquals(4, parts.get(1).getNodeCount());
        assertEquals(16, parts.get(1).getMaxCpusPerNode());
        assertEquals(2, parts.get(1).getMaxGpusPerNode());
        assertEquals(2, parts.get(1).getAccounts().size());
        assertTrue(info.getAccountsList().contains("gpu-acct"));
        assertTrue(info.getAccountsList().contains("other"));
    }

    @Test
    void parseSlurminfoOutput_skipsShortLines() {
        String output = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts\n"
                + "a|1\n"
                + "ok|2|4|0|acc";
        ClusterInfo info = ClusterInfoService.parseSlurminfoOutput(output);
        assertNotNull(info);
        assertEquals(1, info.getPartitions().size());
        assertEquals("ok", info.getPartitions().get(0).getPartitionName());
    }

    @Test
    void parseSlurminfoOutput_handlesWindowsLineEndings() {
        String output = "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts\r\n"
                + "p1|5|12|0|a1";
        ClusterInfo info = ClusterInfoService.parseSlurminfoOutput(output);
        assertNotNull(info);
        assertEquals(1, info.getPartitions().size());
        assertEquals("p1", info.getPartitions().get(0).getPartitionName());
        assertEquals(5, info.getPartitions().get(0).getNodeCount());
    }
}
