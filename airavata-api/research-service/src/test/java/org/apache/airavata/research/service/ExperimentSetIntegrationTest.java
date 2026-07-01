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
package org.apache.airavata.research.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.airavata.api.experiment.ExperimentSpec;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.api.experimentset.CreateExperimentSetRequest;
import org.apache.airavata.api.experimentset.ExperimentSet;
import org.apache.airavata.api.experimentset.SweepSpec;
import org.apache.airavata.api.experimentset.ValueList;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.db.EntityManagerFactoryHolder;
import org.apache.airavata.model.commons.proto.AccessFlags;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.research.model.ExperimentSetEntity;
import org.apache.airavata.research.repository.ExperimentSetRepository;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

@Tag("integration")
public class ExperimentSetIntegrationTest extends TestBase {

    private ExperimentSetRepository realRepository;
    private ExperimentSetRepository repositorySpy;
    private ExperimentService mockExperimentService;
    private ExperimentSetService service;
    private RequestContext ctx;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        realRepository = new JpaRepositoryFactory(EntityManagerFactoryHolder.getTestEntityManager())
                .getRepository(ExperimentSetRepository.class);

        // AuditingEntityListener does not fire in the bare-JPA test context.
        // Intercept save() to stamp createdAt/updatedAt before delegating to the real repository.
        repositorySpy = spy(realRepository);
        doAnswer(inv -> {
            ExperimentSetEntity entity = inv.getArgument(0);
            Instant now = Instant.now();
            if (entity.getCreatedAt() == null) entity.setCreatedAt(now);
            if (entity.getUpdatedAt() == null) entity.setUpdatedAt(now);
            return realRepository.save(entity);
        }).when(repositorySpy).save(any(ExperimentSetEntity.class));

        mockExperimentService = mock(ExperimentService.class);
        service = new ExperimentSetService(mockExperimentService, repositorySpy);

        ctx = new RequestContext("test-user", "test-gateway", "token", Map.of());
    }

    /** Returns a stub ExperimentWithAccess carrying the given experiment id. */
    private ExperimentWithAccess stubWith(String experimentId) {
        ExperimentModel model = ExperimentModel.newBuilder()
                .setExperimentId(experimentId)
                .setUserName("test-user")
                .setGatewayId("test-gateway")
                .build();
        return ExperimentWithAccess.newBuilder()
                .setExperiment(model)
                .setAccess(AccessFlags.newBuilder().setIsOwner(true).build())
                .build();
    }

    /** Builds a 2×2 sweep request (axes: alpha=[a1,a2], beta=[b1,b2] → 4 combinations). */
    private CreateExperimentSetRequest build2x2SweepRequest() {
        SweepSpec sweep = SweepSpec.newBuilder()
                .setBase(ExperimentSpec.newBuilder()
                        .setExperimentName("base")
                        .setProjectId("proj-1")
                        .setApplicationInterfaceId("echo-app")
                        .putInputs("alpha", "a1")
                        .putInputs("beta", "b1")
                        .build())
                .putSweepAxes("alpha", ValueList.newBuilder().addValues("a1").addValues("a2").build())
                .putSweepAxes("beta", ValueList.newBuilder().addValues("b1").addValues("b2").build())
                .setNamePrefix("sweep-run")
                .build();
        return CreateExperimentSetRequest.newBuilder()
                .setSetName("integration-sweep")
                .setSweep(sweep)
                .build();
    }

    @Test
    @DisplayName("createExperimentSet 2x2 sweep persists set with 4 members; getExperimentSet returns them")
    void createAndGet() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        when(mockExperimentService.createExperimentFromSpec(eq(ctx), any(ExperimentSpec.class)))
                .thenAnswer(inv -> stubWith("EXP-" + counter.getAndIncrement()));

        ExperimentSet created = service.createExperimentSet(ctx, build2x2SweepRequest());

        assertNotNull(created.getExperimentSetId(), "set id must be assigned");
        assertFalse(created.getExperimentSetId().isBlank(), "set id must not be blank");
        assertEquals("integration-sweep", created.getSetName());
        assertEquals("test-user", created.getOwner());
        assertEquals("test-gateway", created.getGatewayId());
        assertEquals(4, created.getExperimentIdsCount(), "2×2 sweep must yield 4 member experiments");

        // Flush to DB before clearing the L1 cache so subsequent em.find() sees the writes.
        EntityManagerFactoryHolder.getTestEntityManager().flush();
        EntityManagerFactoryHolder.getTestEntityManager().clear();
        ExperimentSet reloaded = service.getExperimentSet(ctx, created.getExperimentSetId());

        assertEquals(created.getExperimentSetId(), reloaded.getExperimentSetId());
        assertEquals("integration-sweep", reloaded.getSetName());
        assertEquals("test-user", reloaded.getOwner());
        assertEquals("test-gateway", reloaded.getGatewayId());
        assertEquals(4, reloaded.getExperimentIdsCount());
        assertTrue(reloaded.getExperimentIdsList().containsAll(List.of("EXP-0", "EXP-1", "EXP-2", "EXP-3")));

        // sweepSpecJson was persisted
        ExperimentSetEntity rawEntity = realRepository.findById(created.getExperimentSetId()).orElseThrow();
        assertNotNull(rawEntity.getSweepSpecJson());
        assertFalse(rawEntity.getSweepSpecJson().isBlank(), "sweepSpecJson must be non-empty");
    }

    @Test
    @DisplayName("listExperimentSets returns the created set for the same owner+gateway")
    void createAndList() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        when(mockExperimentService.createExperimentFromSpec(eq(ctx), any(ExperimentSpec.class)))
                .thenAnswer(inv -> stubWith("EXP-" + counter.getAndIncrement()));

        ExperimentSet created = service.createExperimentSet(ctx, build2x2SweepRequest());

        EntityManagerFactoryHolder.getTestEntityManager().flush();
        EntityManagerFactoryHolder.getTestEntityManager().clear();

        List<ExperimentSet> sets = service.listExperimentSets(ctx, 0, 0);
        assertEquals(1, sets.size());
        assertEquals(created.getExperimentSetId(), sets.get(0).getExperimentSetId());
        assertEquals(4, sets.get(0).getExperimentIdsCount());

        // Different owner sees nothing
        RequestContext otherCtx = new RequestContext("other-user", "test-gateway", "token", Map.of());
        List<ExperimentSet> otherSets = service.listExperimentSets(otherCtx, 0, 0);
        assertTrue(otherSets.isEmpty(), "different owner must not see the set");
    }

    @Test
    @DisplayName("deleteExperimentSet removes the set and cascades member rows; stubs experiments untouched")
    void deleteSet() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        when(mockExperimentService.createExperimentFromSpec(eq(ctx), any(ExperimentSpec.class)))
                .thenAnswer(inv -> stubWith("EXP-" + counter.getAndIncrement()));

        ExperimentSet created = service.createExperimentSet(ctx, build2x2SweepRequest());
        String setId = created.getExperimentSetId();

        // Flush to DB before clearing the L1 cache so the JPQL member count query sees the writes.
        EntityManagerFactoryHolder.getTestEntityManager().flush();
        EntityManagerFactoryHolder.getTestEntityManager().clear();

        // Confirm members exist before delete
        long membersBefore = EntityManagerFactoryHolder.getTestEntityManager()
                .createQuery(
                        "SELECT COUNT(m) FROM ExperimentSetMemberEntity m WHERE m.experimentSet.id = :setId",
                        Long.class)
                .setParameter("setId", setId)
                .getSingleResult();
        assertEquals(4L, membersBefore, "4 member rows must exist before delete");

        service.deleteExperimentSet(ctx, setId);
        realRepository.flush();
        EntityManagerFactoryHolder.getTestEntityManager().clear();

        // Set is gone
        assertFalse(realRepository.existsById(setId), "set must not exist after delete");

        // Member rows are gone (cascade)
        long membersAfter = EntityManagerFactoryHolder.getTestEntityManager()
                .createQuery(
                        "SELECT COUNT(m) FROM ExperimentSetMemberEntity m WHERE m.experimentSet.id = :setId",
                        Long.class)
                .setParameter("setId", setId)
                .getSingleResult();
        assertEquals(0L, membersAfter, "member rows must be cascaded on set delete");

        // deleteExperiment was never called — set is a grouping, not an owner
        verify(mockExperimentService, never()).deleteExperiment(any(), any());
    }
}
