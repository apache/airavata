import { actions, getters, mutations } from "@/web-components/store.js";
import { models } from "django-airavata-api";

/*
 * Test MUTATIONS
 */
test("setExperiment sets state", () => {
  const state = {};
  const experiment = {};
  mutations.setExperiment(state, { experiment });
  expect(state.experiment).toBe(experiment);
});

/*
 * Test ACTIONS
 */
const testAction = (
  action,
  {
    payload = null,
    state = {},
    getters = {},
    expectedMutations = [],
    done,
    expectedActions = [],
  }
) => {
  let mutationCount = 0;

  // mock commit
  const commit = (type, payload) => {
    const mutation = expectedMutations[mutationCount] || {};
    try {
      expect(type).toEqual(mutation.type);
      expect(payload).toEqual(mutation.payload);
    } catch (error) {
      done(error);
    }

    mutationCount++;
    checkIfDone();
  };

  const checkIfDone = () => {
    if (
      mutationCount >= expectedMutations.length &&
      actionCount >= expectedActions.length
    ) {
      done();
    }
  };

  // mock dispatch
  let actionCount = 0;
  const dispatch = (type, payload) => {
    const action = expectedActions[actionCount] || {};
    try {
      expect(type).toEqual(action.type);
      expect(payload).toEqual(action.payload);
    } catch (error) {
      done(error);
    }

    actionCount++;
    checkIfDone();
    return action.result;
  };

  // call the action with mocked store and arguments
  const result = action({ commit, dispatch, state, getters }, payload);

  // check if no expectedMutations should have been dispatched
  if (expectedMutations.length === 0 && expectedActions.length === 0) {
    expect(mutationCount).toEqual(0);
    expect(actionCount).toEqual(0);
    done();
  }
  return result;
};

const testApplyBatchQueueResourcePolicy = ({
  computationalResourceScheduling,
  batchQueueResourcePolicy,
  expectedMutations,
  done,
}) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.computationalResourceScheduling = new models.ComputationalResourceSchedulingModel(
    computationalResourceScheduling
  );
  const bqrp = new models.BatchQueueResourcePolicy(batchQueueResourcePolicy);

  const getters = {
    experiment: state.experiment,
    batchQueueResourcePolicy: bqrp,
  };
  testAction(actions.applyBatchQueueResourcePolicy, {
    state,
    getters,
    expectedMutations,
    done,
  });
};

test("applyBatchQueueResourcePolicy: maxAllowedCores caps totalCPUCount", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 1,
      wallTimeLimit: 30,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 5,
      maxAllowedNodes: 2,
      maxAllowedWalltime: 120,
    },
    expectedMutations: [
      { type: "updateTotalCPUCount", payload: { totalCPUCount: 5 } },
    ],
    done,
  });
});

test("applyBatchQueueResourcePolicy: maxAllowedCores doesn't affect totalCPUCount", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 1,
      wallTimeLimit: 30,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 50,
      maxAllowedNodes: 2,
      maxAllowedWalltime: 120,
    },
    expectedMutations: [],
    done,
  });
});

test("applyBatchQueueResourcePolicy: maxAllowedNodes caps nodeCount", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 11,
      wallTimeLimit: 30,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 50,
      maxAllowedNodes: 4,
      maxAllowedWalltime: 120,
    },
    expectedMutations: [{ type: "updateNodeCount", payload: { nodeCount: 4 } }],
    done,
  });
});

test("applyBatchQueueResourcePolicy: maxAllowedNodes doesn't affect nodeCount", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 7,
      wallTimeLimit: 30,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 50,
      maxAllowedNodes: 52,
      maxAllowedWalltime: 120,
    },
    expectedMutations: [],
    done,
  });
});

test("applyBatchQueueResourcePolicy: maxAllowedWalltime caps wallTimeLimit", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 1,
      wallTimeLimit: 8,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 50,
      maxAllowedNodes: 10,
      maxAllowedWalltime: 6,
    },
    expectedMutations: [
      { type: "updateWallTimeLimit", payload: { wallTimeLimit: 6 } },
    ],
    done,
  });
});

test("applyBatchQueueResourcePolicy: maxAllowedWalltime doesn't affect wallTimeLimit", (done) => {
  testApplyBatchQueueResourcePolicy({
    computationalResourceScheduling: {
      totalCPUCount: 10,
      nodeCount: 1,
      wallTimeLimit: 30,
    },
    batchQueueResourcePolicy: {
      maxAllowedCores: 24,
      maxAllowedNodes: 2,
      maxAllowedWalltime: 120,
    },
    expectedMutations: [],
    done,
  });
});

test("initializeGroupResourceProfileId: set to most recent group resource profile when null", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.workspacePreferences = new models.WorkspacePreferences();
  state.workspacePreferences.most_recent_group_resource_profile_id =
    "ec50a69d-54ea-4b7c-a578-9a2a8da09ba0";
  state.groupResourceProfiles = [
    new models.GroupResourceProfile({
      groupResourceProfileId:
        state.workspacePreferences.most_recent_group_resource_profile_id,
    }),
  ];
  const expectedActions = [
    {
      type: "loadGroupResourceProfiles",
    },
    {
      type: "loadWorkspacePreferences",
    },
  ];

  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateExperimentGroupResourceProfileId",
      payload: {
        groupResourceProfileId:
          state.workspacePreferences.most_recent_group_resource_profile_id,
      },
    },
  ];
  testAction(actions.initializeGroupResourceProfile, {
    state,
    getters: g,
    expectedMutations,
    done,
    expectedActions,
  });
});

test("initializeGroupResourceProfileId: set to most recent group resource profile when no longer has access to grp", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.groupResourceProfileId =
    "2580d4e6-7a8d-444e-b259-a8e6ae886d66";
  state.workspacePreferences = new models.WorkspacePreferences();
  state.workspacePreferences.most_recent_group_resource_profile_id =
    "ec50a69d-54ea-4b7c-a578-9a2a8da09ba0";
  state.groupResourceProfiles = [
    new models.GroupResourceProfile({
      groupResourceProfileId:
        state.workspacePreferences.most_recent_group_resource_profile_id,
    }),
  ];
  const expectedActions = [
    {
      type: "loadGroupResourceProfiles",
    },
    {
      type: "loadWorkspacePreferences",
    },
  ];
  // experiment's grp isn't in the list of available grps
  expect(
    state.groupResourceProfiles.find(
      (grp) =>
        grp.groupResourceProfileId ===
        state.experiment.userConfigurationData.groupResourceProfileId
    )
  ).toBeUndefined();
  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateExperimentGroupResourceProfileId",
      payload: {
        groupResourceProfileId:
          state.workspacePreferences.most_recent_group_resource_profile_id,
      },
    },
  ];
  testAction(actions.initializeGroupResourceProfile, {
    state,
    getters: g,
    expectedMutations,
    done,
    expectedActions,
  });
});

test("initializeGroupResourceProfileId: set to first group resource profile when no most recent grp", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.groupResourceProfileId =
    "2580d4e6-7a8d-444e-b259-a8e6ae886d66";
  state.workspacePreferences = new models.WorkspacePreferences();
  state.workspacePreferences.most_recent_group_resource_profile_id = null;
  state.groupResourceProfiles = [
    new models.GroupResourceProfile({
      groupResourceProfileId: "c84da77b-8ce6-457f-b5c7-c72a663d7f77",
    }),
  ];
  const expectedActions = [
    {
      type: "loadGroupResourceProfiles",
    },
    {
      type: "loadWorkspacePreferences",
    },
  ];
  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateExperimentGroupResourceProfileId",
      payload: {
        groupResourceProfileId: "c84da77b-8ce6-457f-b5c7-c72a663d7f77",
      },
    },
  ];
  testAction(actions.initializeGroupResourceProfile, {
    state,
    getters: g,
    expectedMutations,
    done,
    expectedActions,
  });
});

test("initializeGroupResourceProfileId: set to null when no longer has access", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.groupResourceProfileId =
    "2580d4e6-7a8d-444e-b259-a8e6ae886d66";
  state.workspacePreferences = new models.WorkspacePreferences();
  state.workspacePreferences.most_recent_group_resource_profile_id = null;
  state.groupResourceProfiles = [];
  const expectedActions = [
    {
      type: "loadGroupResourceProfiles",
    },
    {
      type: "loadWorkspacePreferences",
    },
  ];
  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateExperimentGroupResourceProfileId",
      payload: {
        groupResourceProfileId: null,
      },
    },
  ];
  testAction(actions.initializeGroupResourceProfile, {
    state,
    getters: g,
    expectedMutations,
    done,
    expectedActions,
  });
});

test("applyGroupResourceProfile: when compute resource changes, dispatches loadAppDeploymentQueues and setDefaultQueue", (done) => {
  const expectedActions = [
    {
      type: "initializeResourceHostId",
      result: true,
    },
    {
      type: "loadAppDeploymentQueues",
    },
    {
      type: "setDefaultQueue",
    },
  ];
  testAction(actions.applyGroupResourceProfile, {
    done,
    expectedActions,
  });
});

test("applyGroupResourceProfile: when compute resource doesn't change, but queue no longer allowed, dispatches setDefaultQueue", (done) => {
  const expectedActions = [
    {
      type: "initializeResourceHostId",
      result: false,
    },
    {
      type: "setDefaultQueue",
    },
  ];
  testAction(actions.applyGroupResourceProfile, {
    done,
    expectedActions,
  });
});

test("applyGroupResourceProfile: when compute resource doesn't change, and queue doesn't change, dispatches applyBatchQueueResourcePolicy", (done) => {
  const getters = {
    queue: new models.BatchQueue({ queueName: "shared" }),
  };
  const expectedActions = [
    {
      type: "initializeResourceHostId",
      result: false,
    },
    {
      type: "applyBatchQueueResourcePolicy",
    },
  ];
  testAction(actions.applyGroupResourceProfile, {
    getters,
    done,
    expectedActions,
  });
});

test("initializeResourceHostId: experiment has no resourceHostId, should dispatch getDefaultResourceHostId, return true", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.computationalResourceScheduling = new models.ComputationalResourceSchedulingModel();
  const mockGetters = {
    resourceHostId: getters.resourceHostId(state),
  };
  const expectedActions = [
    {
      type: "getDefaultResourceHostId",
      result: "resourceHostId1",
    },
  ];
  const expectedMutations = [
    {
      type: "updateExperimentResourceHostId",
      payload: { resourceHostId: "resourceHostId1" },
    },
  ];
  const result = testAction(actions.initializeResourceHostId, {
    state,
    getters: mockGetters,
    done,
    expectedActions,
    expectedMutations,
  });
  expect(result).resolves.toBe(true);
});

test("initializeResourceHostId: experiment has resourceHostId but not in list of app deployments, should dispatch getDefaultResourceHostId, return true", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.computationalResourceScheduling = new models.ComputationalResourceSchedulingModel(
    {
      resourceHostId: "resourceHostId1",
    }
  );
  // experiment's resourceHostId1 isn't in list of app deployments
  state.applicationDeployments = [
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId2",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId3",
    }),
  ];
  const mockGetters = {
    resourceHostId: getters.resourceHostId(state),
    computeResources: getters.computeResources(state),
  };
  expect(mockGetters.resourceHostId).toBe("resourceHostId1");
  expect(mockGetters.computeResources).toEqual([
    "resourceHostId2",
    "resourceHostId3",
  ]);
  const expectedActions = [
    {
      type: "getDefaultResourceHostId",
      result: "resourceHostId2",
    },
  ];
  const expectedMutations = [
    {
      type: "updateExperimentResourceHostId",
      payload: { resourceHostId: "resourceHostId2" },
    },
  ];
  const result = testAction(actions.initializeResourceHostId, {
    state,
    getters: mockGetters,
    done,
    expectedActions,
    expectedMutations,
  });
  expect(result).resolves.toBe(true);
});

test("initializeResourceHostId: experiment has resourceHostId and in list of app deployments, should return false", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.computationalResourceScheduling = new models.ComputationalResourceSchedulingModel(
    {
      resourceHostId: "resourceHostId1",
    }
  );
  state.applicationDeployments = [
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId1",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId2",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId3",
    }),
  ];
  const mockGetters = {
    resourceHostId: getters.resourceHostId(state),
    computeResources: getters.computeResources(state),
  };
  expect(mockGetters.resourceHostId).toBe("resourceHostId1");
  expect(mockGetters.computeResources).toEqual([
    "resourceHostId1",
    "resourceHostId2",
    "resourceHostId3",
  ]);
  const result = testAction(actions.initializeResourceHostId, {
    state,
    getters: mockGetters,
    done,
  });
  expect(result).resolves.toBe(false);
});

test("getDefaultResourceHostId: dispatch loadDefaultComputeResourceId, return defaultComputeResourceId when in deployments list", (done) => {
  const state = {};
  state.workspacePreferences = new models.WorkspacePreferences({
    most_recent_compute_resource_id: "resourceHostId1",
  });
  state.applicationDeployments = [
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId1",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId2",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId3",
    }),
  ];
  const mockGetters = {
    defaultComputeResourceId: getters.defaultComputeResourceId(state),
    computeResources: getters.computeResources(state),
  };
  expect(mockGetters.defaultComputeResourceId).toBe("resourceHostId1");
  expect(mockGetters.computeResources).toEqual([
    "resourceHostId1",
    "resourceHostId2",
    "resourceHostId3",
  ]);
  const expectedActions = [{ type: "loadDefaultComputeResourceId" }];
  const result = testAction(actions.getDefaultResourceHostId, {
    state,
    getters: mockGetters,
    expectedActions,
    done,
  });
  expect(result).resolves.toBe("resourceHostId1");
});

test("getDefaultResourceHostId: dispatch loadDefaultComputeResourceId, return first compute resource when defaultComputeResourceId not in deployments list", (done) => {
  const state = {};
  state.workspacePreferences = new models.WorkspacePreferences({
    most_recent_compute_resource_id: "resourceHostId1",
  });
  state.applicationDeployments = [
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId2",
    }),
    new models.ApplicationDeploymentDescription({
      computeHostId: "resourceHostId3",
    }),
  ];
  const mockGetters = {
    defaultComputeResourceId: getters.defaultComputeResourceId(state),
    computeResources: getters.computeResources(state),
  };
  expect(mockGetters.defaultComputeResourceId).toBe("resourceHostId1");
  expect(mockGetters.computeResources).toEqual([
    "resourceHostId2",
    "resourceHostId3",
  ]);
  const expectedActions = [{ type: "loadDefaultComputeResourceId" }];
  const result = testAction(actions.getDefaultResourceHostId, {
    state,
    getters: mockGetters,
    expectedActions,
    done,
  });
  expect(result).resolves.toBe("resourceHostId2");
});

test("getDefaultResourceHostId: dispatch loadDefaultComputeResourceId, return null when no compute resources", (done) => {
  const state = {};
  state.workspacePreferences = new models.WorkspacePreferences({
    most_recent_compute_resource_id: "resourceHostId1",
  });
  state.applicationDeployments = [];
  const mockGetters = {
    defaultComputeResourceId: getters.defaultComputeResourceId(state),
    computeResources: getters.computeResources(state),
  };
  expect(mockGetters.defaultComputeResourceId).toBe("resourceHostId1");
  expect(mockGetters.computeResources).toEqual([]);
  const expectedActions = [{ type: "loadDefaultComputeResourceId" }];
  const result = testAction(actions.getDefaultResourceHostId, {
    state,
    getters: mockGetters,
    expectedActions,
    done,
  });
  expect(result).resolves.toBe(null);
});

test("initializeQueue: when queue selected, when defaults are less than batch queue policy limits, queue settings use defaults", (done) => {
  const mockGetters = {
    queue: new models.BatchQueue({
      queueName: "shared",
      defaultNodeCount: 1,
      defaultCPUCount: 8,
      defaultWalltime: 30,
    }),
    batchQueueResourcePolicy: new models.BatchQueueResourcePolicy({
      queuename: "shared",
      maxAllowedNodes: 2,
      maxAllowedCores: 32,
      maxAllowedWalltime: 60,
    }),
  };
  mockGetters.getDefaultCPUCount = getters.getDefaultCPUCount(
    null,
    mockGetters
  );
  mockGetters.getDefaultNodeCount = getters.getDefaultNodeCount(
    null,
    mockGetters
  );
  mockGetters.getDefaultWalltime = getters.getDefaultWalltime(
    null,
    mockGetters
  );
  expect(mockGetters.getDefaultCPUCount(mockGetters.queue)).toBe(8);
  expect(mockGetters.getDefaultNodeCount(mockGetters.queue)).toBe(1);
  expect(mockGetters.getDefaultWalltime(mockGetters.queue)).toBe(30);
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount: 8 } },
    { type: "updateNodeCount", payload: { nodeCount: 1 } },
    { type: "updateWallTimeLimit", payload: { wallTimeLimit: 30 } },
    { type: "updateTotalPhysicalMemory", payload: { totalPhysicalMemory: 0 } },
  ];
  testAction(actions.initializeQueue, {
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("initializeQueue: when queue selected, when defaults are more than batch queue policy limits, queue settings use batch queue policy limits", (done) => {
  const mockGetters = {
    queue: new models.BatchQueue({
      queueName: "shared",
      defaultNodeCount: 4,
      defaultCPUCount: 16,
      defaultWalltime: 120,
    }),
    batchQueueResourcePolicy: new models.BatchQueueResourcePolicy({
      queuename: "shared",
      maxAllowedNodes: 2,
      maxAllowedCores: 12,
      maxAllowedWalltime: 45,
    }),
  };
  mockGetters.getDefaultCPUCount = getters.getDefaultCPUCount(
    null,
    mockGetters
  );
  mockGetters.getDefaultNodeCount = getters.getDefaultNodeCount(
    null,
    mockGetters
  );
  mockGetters.getDefaultWalltime = getters.getDefaultWalltime(
    null,
    mockGetters
  );
  expect(mockGetters.getDefaultCPUCount(mockGetters.queue)).toBe(12);
  expect(mockGetters.getDefaultNodeCount(mockGetters.queue)).toBe(2);
  expect(mockGetters.getDefaultWalltime(mockGetters.queue)).toBe(45);
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount: 12 } },
    { type: "updateNodeCount", payload: { nodeCount: 2 } },
    { type: "updateWallTimeLimit", payload: { wallTimeLimit: 45 } },
    { type: "updateTotalPhysicalMemory", payload: { totalPhysicalMemory: 0 } },
  ];
  testAction(actions.initializeQueue, {
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("initializeQueue: when no queue selected, settings are set to 0", (done) => {
  const mockGetters = {
    queue: null,
  };
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount: 0 } },
    { type: "updateNodeCount", payload: { nodeCount: 0 } },
    { type: "updateWallTimeLimit", payload: { wallTimeLimit: 0 } },
    { type: "updateTotalPhysicalMemory", payload: { totalPhysicalMemory: 0 } },
  ];
  testAction(actions.initializeQueue, {
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateNodeCount: only update nodeCount when cpuPerNode <= 0", (done) => {
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 0,
    }),
  };
  const expectedMutations = [
    { type: "updateNodeCount", payload: { nodeCount: 7 } },
  ];
  testAction(actions.updateNodeCount, {
    payload: {
      nodeCount: 7,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateNodeCount: update also totalCPUCount when cpuPerNode > 0", (done) => {
  const nodeCount = 4;
  const enableNodeCountToCpuCheck = true;
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 24,
    }),
    maxAllowedCores: 1000,
  };
  const expectedMutations = [
    { type: "updateNodeCount", payload: { nodeCount } },
    { type: "updateTotalCPUCount", payload: { totalCPUCount: 96 } },
  ];
  testAction(actions.updateNodeCount, {
    payload: {
      nodeCount,
      enableNodeCountToCpuCheck,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateNodeCount: update totalCPUCount when cpuPerNode > 0, but apply maximums", (done) => {
  const nodeCount = 4;
  const enableNodeCountToCpuCheck = true;
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 24,
    }),
    maxAllowedCores: 50,
  };
  const expectedMutations = [
    { type: "updateNodeCount", payload: { nodeCount } },
    { type: "updateTotalCPUCount", payload: { totalCPUCount: 50 } },
  ];
  testAction(actions.updateNodeCount, {
    payload: {
      nodeCount,
      enableNodeCountToCpuCheck,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateTotalCPUCount: only update totalCPUCount when cpuPerNode <= 0", (done) => {
  const totalCPUCount = 23;
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 0,
    }),
  };
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount } },
  ];
  testAction(actions.updateTotalCPUCount, {
    payload: {
      totalCPUCount,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateTotalCPUCount: update also nodeCount when cpuPerNode > 0", (done) => {
  const nodeCount = 4;
  const totalCPUCount = 96;
  const enableNodeCountToCpuCheck = true;
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 24,
    }),
    maxAllowedNodes: 1000,
  };
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount } },
    { type: "updateNodeCount", payload: { nodeCount } },
  ];
  testAction(actions.updateTotalCPUCount, {
    payload: {
      totalCPUCount,
      enableNodeCountToCpuCheck,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateTotalCPUCount: update nodeCount when cpuPerNode > 0, but apply maximums", (done) => {
  const totalCPUCount = 96;
  const enableNodeCountToCpuCheck = true;
  const mockGetters = {
    queue: new models.BatchQueue({
      cpuPerNode: 24,
    }),
    maxAllowedNodes: 2,
  };
  expect(totalCPUCount / mockGetters.queue.cpuPerNode).toBeGreaterThan(
    mockGetters.maxAllowedNodes
  );
  const expectedMutations = [
    { type: "updateTotalCPUCount", payload: { totalCPUCount } },
    { type: "updateNodeCount", payload: { nodeCount: 2 } },
  ];
  testAction(actions.updateTotalCPUCount, {
    payload: {
      totalCPUCount,
      enableNodeCountToCpuCheck,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});

test("updateGroupResourceProfileId: test normal case where updated to a GRP id", (done) => {
  const mockGetters = {
    groupResourceProfileId: "old_grp_id",
  };
  const groupResourceProfileId = "new_grp_id";
  const expectedMutations = [
    {
      type: "updateGroupResourceProfileId",
      payload: { groupResourceProfileId },
    },
  ];
  const expectedActions = [
    { type: "loadApplicationDeployments" },
    { type: "applyGroupResourceProfile" },
  ];
  testAction(actions.updateGroupResourceProfileId, {
    payload: {
      groupResourceProfileId,
    },
    getters: mockGetters,
    expectedMutations,
    done,
    expectedActions,
  });
});

test("updateGroupResourceProfileId: test case where GRP id is updated to null", (done) => {
  const mockGetters = {
    groupResourceProfileId: "old_grp_id",
  };
  const groupResourceProfileId = null;
  const expectedMutations = [
    {
      type: "updateGroupResourceProfileId",
      payload: { groupResourceProfileId },
    },
  ];
  testAction(actions.updateGroupResourceProfileId, {
    payload: {
      groupResourceProfileId,
    },
    getters: mockGetters,
    expectedMutations,
    done,
  });
});
