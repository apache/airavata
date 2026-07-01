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
package org.apache.airavata.research.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.db.EntityManagerFactoryHolder;
import org.apache.airavata.research.model.ExperimentSetEntity;
import org.apache.airavata.research.model.ExperimentSetMemberEntity;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

@Tag("integration")
public class ExperimentSetRepositoryTest extends TestBase {

    private ExperimentSetRepository repository;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        repository = new JpaRepositoryFactory(EntityManagerFactoryHolder.getTestEntityManager())
                .getRepository(ExperimentSetRepository.class);
    }

    private ExperimentSetEntity buildSet(String name, String owner, String gateway, int memberCount) {
        ExperimentSetEntity set = new ExperimentSetEntity();
        set.setSetName(name);
        set.setOwner(owner);
        set.setGatewayId(gateway);
        set.setSweepSpecJson("{\"param\":\"value\"}");
        // AuditingEntityListener requires a Spring context; supply timestamps manually in tests.
        Instant now = Instant.now();
        set.setCreatedAt(now);
        set.setUpdatedAt(now);

        List<ExperimentSetMemberEntity> members = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            ExperimentSetMemberEntity member = new ExperimentSetMemberEntity();
            member.setExperimentId("EXP-" + i);
            member.setOrdinal(i);
            member.setExperimentSet(set);
            members.add(member);
        }
        set.setMembers(members);
        return set;
    }

    @Test
    @DisplayName("persist set with 3 members and retrieve by id with members eager-loaded")
    void persistAndFindById() {
        ExperimentSetEntity set = buildSet("sweep-1", "user1", "gw1", 3);
        ExperimentSetEntity saved = repository.saveAndFlush(set);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        // Clear context to force a DB read
        EntityManagerFactoryHolder.getTestEntityManager().clear();

        ExperimentSetEntity found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("sweep-1", found.getSetName());
        assertEquals("user1", found.getOwner());
        assertEquals("gw1", found.getGatewayId());
        assertEquals("{\"param\":\"value\"}", found.getSweepSpecJson());
        assertEquals(3, found.getMembers().size());
        // verify ordinals
        found.getMembers().stream().forEach(m -> {
            assertNotNull(m.getId());
            assertNotNull(m.getExperimentId());
            assertTrue(m.getOrdinal() >= 0 && m.getOrdinal() < 3);
        });
    }

    @Test
    @DisplayName("findByOwnerAndGatewayIdOrderByCreatedAtDesc returns the set")
    void findByOwnerAndGateway() {
        ExperimentSetEntity set = buildSet("sweep-2", "user2", "gw2", 2);
        repository.saveAndFlush(set);

        EntityManagerFactoryHolder.getTestEntityManager().clear();

        List<ExperimentSetEntity> results =
                repository.findByOwnerAndGatewayIdOrderByCreatedAtDesc("user2", "gw2");
        assertEquals(1, results.size());
        assertEquals("sweep-2", results.get(0).getSetName());
        assertEquals(2, results.get(0).getMembers().size());
    }

    @Test
    @DisplayName("deleting a set cascades to its members (member table becomes empty)")
    void deleteSetCascadesMembers() {
        ExperimentSetEntity set = buildSet("sweep-3", "user3", "gw3", 3);
        ExperimentSetEntity saved = repository.saveAndFlush(set);
        String setId = saved.getId();

        // Verify members exist
        EntityManagerFactoryHolder.getTestEntityManager().clear();
        assertEquals(3, repository.findById(setId).orElseThrow().getMembers().size());

        repository.deleteById(setId);
        repository.flush();

        EntityManagerFactoryHolder.getTestEntityManager().clear();

        // Set is gone
        assertFalse(repository.existsById(setId));

        // Members are gone (cascade removed them)
        long memberCount = EntityManagerFactoryHolder.getTestEntityManager()
                .createQuery(
                        "SELECT COUNT(m) FROM ExperimentSetMemberEntity m WHERE m.experimentSet.id = :setId",
                        Long.class)
                .setParameter("setId", setId)
                .getSingleResult();
        assertEquals(0L, memberCount);
    }
}
