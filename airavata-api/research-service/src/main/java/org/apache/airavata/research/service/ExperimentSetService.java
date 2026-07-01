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

import com.google.protobuf.util.JsonFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.airavata.api.experiment.ExperimentSpec;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.api.experimentset.AggregateState;
import org.apache.airavata.api.experimentset.CreateExperimentSetRequest;
import org.apache.airavata.api.experimentset.ExperimentSet;
import org.apache.airavata.api.experimentset.ExperimentSetStatus;
import org.apache.airavata.api.experimentset.ExperimentStatusItem;
import org.apache.airavata.api.experimentset.SweepSpec;
import org.apache.airavata.api.experimentset.ValueList;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.research.model.ExperimentSetEntity;
import org.apache.airavata.research.model.ExperimentSetMemberEntity;
import org.apache.airavata.research.repository.ExperimentSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExperimentSetService {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentSetService.class);

    private final ExperimentService experimentService;
    private final ExperimentSetRepository experimentSetRepository;

    public ExperimentSetService(ExperimentService experimentService, ExperimentSetRepository experimentSetRepository) {
        this.experimentService = experimentService;
        this.experimentSetRepository = experimentSetRepository;
    }

    /**
     * Create an experiment set from a {@link CreateExperimentSetRequest}.
     *
     * <p>For {@code sweep} source: expands the axes (cartesian product), builds one {@link ExperimentSpec}
     * per combination with overridden inputs and a name of {@code name_prefix + "_" + index}, calls
     * {@link ExperimentService#createExperimentFromSpec} for each. If any call throws, all already-created
     * child experiments are deleted (best-effort) and the exception is rethrown without persisting the set.
     *
     * <p>For {@code existing} source: validates each experiment id via {@link ExperimentService#getExperiment},
     * then groups them into a set.
     */
    public ExperimentSet createExperimentSet(RequestContext ctx, CreateExperimentSetRequest req)
            throws ServiceException {
        return switch (req.getSourceCase()) {
            case SWEEP -> createFromSweep(ctx, req.getSetName(), req.getSweep());
            case EXISTING -> createFromExisting(ctx, req.getSetName(), req.getExisting().getExperimentIdsList());
            default -> throw new IllegalArgumentException("CreateExperimentSetRequest must set a source (sweep or existing)");
        };
    }

    private ExperimentSet createFromSweep(RequestContext ctx, String setName, SweepSpec sweep)
            throws ServiceException {

        // Build the base input map from the base spec
        Map<String, String> base = new LinkedHashMap<>(sweep.getBase().getInputsMap());

        // Build axis map (preserving proto map iteration order is best-effort; LinkedHashMap from proto is unspecified,
        // but the cartesian product is deterministic per run given the same axes order)
        Map<String, List<String>> axes = new LinkedHashMap<>();
        for (Map.Entry<String, ValueList> entry : sweep.getSweepAxesMap().entrySet()) {
            axes.put(entry.getKey(), entry.getValue().getValuesList());
        }

        List<Map<String, String>> combinations = SweepExpander.expand(base, axes);

        String namePrefix = sweep.getNamePrefix();
        List<String> createdIds = new ArrayList<>(combinations.size());

        try {
            for (int i = 0; i < combinations.size(); i++) {
                Map<String, String> combo = combinations.get(i);
                ExperimentSpec spec = buildSpecFromCombo(sweep.getBase(), combo, namePrefix + "_" + i);
                ExperimentWithAccess created = experimentService.createExperimentFromSpec(ctx, spec);
                createdIds.add(created.getExperiment().getExperimentId());
            }
        } catch (Exception e) {
            // Rollback: delete already-created experiments best-effort
            for (String id : createdIds) {
                try {
                    experimentService.deleteExperiment(ctx, id);
                } catch (Exception ex) {
                    logger.warn("Failed to delete experiment {} during sweep rollback: {}", id, ex.getMessage());
                }
            }
            if (e instanceof ServiceException se) {
                throw se;
            }
            throw new ServiceException("Sweep expansion failed mid-way: " + e.getMessage(), e);
        }

        // Serialize the sweep spec for auditing
        String sweepSpecJson = "";
        try {
            sweepSpecJson = JsonFormat.printer().print(sweep);
        } catch (Exception e) {
            logger.warn("Failed to serialize sweep spec to JSON: {}", e.getMessage());
        }

        ExperimentSetEntity entity = buildEntity(setName, ctx.getUserId(), ctx.getGatewayId(), createdIds, sweepSpecJson);
        ExperimentSetEntity saved = experimentSetRepository.save(entity);

        return toProto(saved, sweep);
    }

    private ExperimentSet createFromExisting(RequestContext ctx, String setName, List<String> experimentIds)
            throws ServiceException {
        // Validate each id exists and is accessible
        for (String id : experimentIds) {
            experimentService.getExperiment(ctx, id);
        }

        ExperimentSetEntity entity = buildEntity(setName, ctx.getUserId(), ctx.getGatewayId(), experimentIds, null);
        ExperimentSetEntity saved = experimentSetRepository.save(entity);

        return toProto(saved, null);
    }

    private ExperimentSpec buildSpecFromCombo(ExperimentSpec base, Map<String, String> combo, String name) {
        ExperimentSpec.Builder builder = base.toBuilder()
                .setExperimentName(name)
                .clearInputs();
        // Start with base inputs, then override with combo values
        Map<String, String> merged = new LinkedHashMap<>(base.getInputsMap());
        merged.putAll(combo);
        builder.putAllInputs(merged);
        return builder.build();
    }

    private ExperimentSetEntity buildEntity(
            String setName,
            String owner,
            String gatewayId,
            List<String> experimentIds,
            String sweepSpecJson) {
        ExperimentSetEntity entity = new ExperimentSetEntity();
        entity.setSetName(setName);
        entity.setOwner(owner);
        entity.setGatewayId(gatewayId);
        entity.setSweepSpecJson(sweepSpecJson);

        List<ExperimentSetMemberEntity> members = new ArrayList<>(experimentIds.size());
        for (int i = 0; i < experimentIds.size(); i++) {
            ExperimentSetMemberEntity member = new ExperimentSetMemberEntity();
            member.setExperimentId(experimentIds.get(i));
            member.setOrdinal(i);
            member.setExperimentSet(entity);
            members.add(member);
        }
        entity.setMembers(members);
        return entity;
    }

    /**
     * Launch all member experiments in the set (best-effort). A failure on one child is logged
     * and counted but does not abort the remaining launches or propagate to the caller.
     * Owner-scoped: throws {@link NoSuchElementException} (→ NOT_FOUND) when the set does not
     * exist or belongs to a different user.
     */
    public ExperimentSet launchExperimentSet(RequestContext ctx, String setId, String notificationEmail) {
        ExperimentSetEntity entity = loadOwned(ctx, setId);
        int failures = 0;
        for (ExperimentSetMemberEntity member : entity.getMembers()) {
            String experimentId = member.getExperimentId();
            try {
                experimentService.launchExperimentWithStorageSetup(ctx, experimentId, ctx.getGatewayId(), notificationEmail);
            } catch (Exception e) {
                failures++;
                logger.warn("Failed to launch experiment {} in set {}: {}", experimentId, setId, e.getMessage());
            }
        }
        if (failures > 0) {
            logger.info("Launched experiment set {} with {} child failure(s) out of {} members",
                    setId, failures, entity.getMembers().size());
        }
        return toProto(entity, null);
    }

    /** Owner-scoped fetch of a single experiment set. */
    public ExperimentSet getExperimentSet(RequestContext ctx, String setId) {
        return toProto(loadOwned(ctx, setId), null);
    }

    /**
     * List all experiment sets owned by the caller in the current gateway, newest first.
     * {@code limit <= 0} means no limit; {@code offset} skips that many leading results.
     */
    public List<ExperimentSet> listExperimentSets(RequestContext ctx, int limit, int offset) {
        List<ExperimentSetEntity> all = experimentSetRepository.findByOwnerAndGatewayIdOrderByCreatedAtDesc(
                ctx.getUserId(), ctx.getGatewayId());

        Stream<ExperimentSetEntity> stream = all.stream().skip(Math.max(0, offset));
        if (limit > 0) {
            stream = stream.limit(limit);
        }
        return stream.map(e -> toProto(e, null)).toList();
    }

    /**
     * Delete an experiment set and its member rows (cascade). Does NOT delete the child experiments
     * themselves — a set is a grouping, not an owner of the experiments.
     */
    public void deleteExperimentSet(RequestContext ctx, String setId) {
        loadOwned(ctx, setId);
        experimentSetRepository.deleteById(setId);
    }

    /**
     * Return per-experiment states and an aggregate for the set.
     * Owner-scoped. For each member, calls {@link ExperimentService#getExperiment} and reads the
     * latest {@link ExperimentStatus} (last element of the list) to get the current state name.
     */
    public ExperimentSetStatus getExperimentSetStatus(RequestContext ctx, String setId) throws ServiceException {
        ExperimentSetEntity entity = loadOwned(ctx, setId);

        List<String> stateNames = new ArrayList<>();
        Map<String, Integer> countsByState = new LinkedHashMap<>();
        List<ExperimentStatusItem> items = new ArrayList<>();

        for (ExperimentSetMemberEntity member : entity.getMembers()) {
            String experimentId = member.getExperimentId();
            ExperimentModel experiment = experimentService.getExperiment(ctx, experimentId);

            // Latest status is the last element; fall back to UNKNOWN when the experiment is
            // inaccessible (null) or has no statuses yet.
            String stateName;
            String processId = "";
            if (experiment == null) {
                stateName = "EXPERIMENT_STATE_UNKNOWN";
            } else {
                List<ExperimentStatus> statuses = experiment.getExperimentStatusList();
                if (statuses.isEmpty()) {
                    stateName = "EXPERIMENT_STATE_UNKNOWN";
                } else {
                    stateName = statuses.get(statuses.size() - 1).getState().name();
                }
                // First process id, if any
                if (experiment.getProcessesCount() > 0) {
                    processId = experiment.getProcesses(0).getProcessId();
                }
            }
            stateNames.add(stateName);
            countsByState.merge(stateName, 1, Integer::sum);

            items.add(ExperimentStatusItem.newBuilder()
                    .setExperimentId(experimentId)
                    .setState(stateName)
                    .setProcessId(processId)
                    .build());
        }

        return ExperimentSetStatus.newBuilder()
                .setExperimentSetId(setId)
                .setTotal(items.size())
                .putAllCountsByState(countsByState)
                .setAggregate(aggregate(stateNames))
                .addAllItems(items)
                .build();
    }

    /**
     * Compute an {@link AggregateState} from a list of {@code ExperimentState} enum name strings.
     *
     * <p>Bucket mapping (enum names from {@code org.apache.airavata.model.status.ExperimentState}):
     * <ul>
     *   <li>TERMINAL_COMPLETE : EXPERIMENT_STATE_COMPLETED
     *   <li>TERMINAL_FAILURE  : EXPERIMENT_STATE_FAILED, EXPERIMENT_STATE_CANCELED
     *   <li>RUNNING           : EXPERIMENT_STATE_EXECUTING, EXPERIMENT_STATE_LAUNCHED, EXPERIMENT_STATE_SCHEDULED
     *   <li>PRE_RUN           : EXPERIMENT_STATE_CREATED, EXPERIMENT_STATE_VALIDATED, EXPERIMENT_STATE_UNKNOWN, EXPERIMENT_STATE_CANCELING
     * </ul>
     * Rules (evaluated in order):
     * <ol>
     *   <li>empty list → QUEUED
     *   <li>all TERMINAL_COMPLETE → COMPLETED
     *   <li>all TERMINAL_FAILURE → FAILED
     *   <li>any RUNNING (and no TERMINAL_COMPLETE, any mix of PRE_RUN / TERMINAL_FAILURE tolerated) → RUNNING
     *   <li>all PRE_RUN (CREATED/VALIDATED/UNKNOWN) → QUEUED
     *   <li>otherwise → MIXED
     * </ol>
     */
    public static AggregateState aggregate(List<String> stateNames) {
        if (stateNames.isEmpty()) {
            return AggregateState.QUEUED;
        }

        // Terminal-complete bucket
        Set<String> terminalComplete = Set.of("EXPERIMENT_STATE_COMPLETED");
        // Terminal-failure bucket
        Set<String> terminalFailure = Set.of("EXPERIMENT_STATE_FAILED", "EXPERIMENT_STATE_CANCELED");
        // Running bucket (executing, launched, or scheduled)
        Set<String> running = Set.of(
                "EXPERIMENT_STATE_EXECUTING",
                "EXPERIMENT_STATE_LAUNCHED",
                "EXPERIMENT_STATE_SCHEDULED");

        boolean hasComplete = stateNames.stream().anyMatch(terminalComplete::contains);
        boolean hasFailure = stateNames.stream().anyMatch(terminalFailure::contains);
        boolean hasRunning = stateNames.stream().anyMatch(running::contains);
        boolean allComplete = stateNames.stream().allMatch(terminalComplete::contains);
        boolean allFailure = stateNames.stream().allMatch(terminalFailure::contains);
        boolean allPreRun = stateNames.stream().noneMatch(s ->
                terminalComplete.contains(s) || terminalFailure.contains(s) || running.contains(s));

        if (allComplete) return AggregateState.COMPLETED;
        if (allFailure) return AggregateState.FAILED;
        if (hasRunning && !hasComplete) return AggregateState.RUNNING;
        if (allPreRun) return AggregateState.QUEUED;
        return AggregateState.MIXED;
    }

    /** Load a set by id and assert the caller owns it within the same gateway; throws {@link NoSuchElementException} otherwise. */
    private ExperimentSetEntity loadOwned(RequestContext ctx, String setId) {
        ExperimentSetEntity entity = experimentSetRepository.findById(setId)
                .orElseThrow(() -> new NoSuchElementException("ExperimentSet not found: " + setId));
        if (!entity.getOwner().equals(ctx.getUserId()) || !entity.getGatewayId().equals(ctx.getGatewayId())) {
            throw new NoSuchElementException("ExperimentSet not found: " + setId);
        }
        return entity;
    }

    private ExperimentSet toProto(ExperimentSetEntity entity, SweepSpec sweep) {
        ExperimentSet.Builder builder = ExperimentSet.newBuilder()
                .setExperimentSetId(entity.getId() != null ? entity.getId() : "")
                .setSetName(entity.getSetName())
                .setOwner(entity.getOwner())
                .setGatewayId(entity.getGatewayId());

        // Add experiment ids in ordinal order
        List<ExperimentSetMemberEntity> members = entity.getMembers();
        if (members != null) {
            members.stream()
                    .sorted(java.util.Comparator.comparingInt(ExperimentSetMemberEntity::getOrdinal))
                    .forEach(m -> builder.addExperimentIds(m.getExperimentId()));
        }

        if (sweep != null) {
            builder.setSweep(sweep);
        }

        if (entity.getCreatedAt() != null) {
            builder.setCreationTime(entity.getCreatedAt().toEpochMilli());
        }
        if (entity.getUpdatedAt() != null) {
            builder.setUpdatedTime(entity.getUpdatedAt().toEpochMilli());
        }

        return builder.build();
    }
}
