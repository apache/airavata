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
  payload,
  state,
  getters,
  expectedMutations,
  done
) => {
  let count = 0;

  // mock commit
  const commit = (type, payload) => {
    const mutation = expectedMutations[count] || {};
    try {
      expect(type).toEqual(mutation.type);
      expect(payload).toEqual(mutation.payload);
    } catch (error) {
      done(error);
    }

    count++;
    if (count >= expectedMutations.length) {
      done();
    }
  };

  // call the action with mocked store and arguments
  action({ commit, state, getters }, payload);

  // check if no mutations should have been dispatched
  if (expectedMutations.length === 0) {
    expect(count).toEqual(0);
    done();
  }
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
  testAction(
    actions.applyBatchQueueResourcePolicy,
    null,
    state,
    getters,
    expectedMutations,
    done
  );
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

  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateGroupResourceProfileId",
      payload: {
        groupResourceProfileId:
          state.workspacePreferences.most_recent_group_resource_profile_id,
      },
    },
  ];
  testAction(
    actions.initializeGroupResourceProfileId,
    null,
    state,
    g,
    expectedMutations,
    done
  );
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
      type: "updateGroupResourceProfileId",
      payload: {
        groupResourceProfileId:
          state.workspacePreferences.most_recent_group_resource_profile_id,
      },
    },
  ];
  testAction(
    actions.initializeGroupResourceProfileId,
    null,
    state,
    g,
    expectedMutations,
    done
  );
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
  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateGroupResourceProfileId",
      payload: {
        groupResourceProfileId: "c84da77b-8ce6-457f-b5c7-c72a663d7f77",
      },
    },
  ];
  testAction(
    actions.initializeGroupResourceProfileId,
    null,
    state,
    g,
    expectedMutations,
    done
  );
});

test("initializeGroupResourceProfileId: set to null when no longer has access", (done) => {
  const state = {};
  state.experiment = new models.Experiment();
  state.experiment.userConfigurationData.groupResourceProfileId =
    "2580d4e6-7a8d-444e-b259-a8e6ae886d66";
  state.workspacePreferences = new models.WorkspacePreferences();
  state.workspacePreferences.most_recent_group_resource_profile_id = null;
  state.groupResourceProfiles = [];
  const g = {
    experiment: state.experiment,
    findGroupResourceProfile: (groupResourceProfileId) =>
      getters.findGroupResourceProfile(state)(groupResourceProfileId),
  };
  const expectedMutations = [
    {
      type: "updateGroupResourceProfileId",
      payload: {
        groupResourceProfileId: null,
      },
    },
  ];
  testAction(
    actions.initializeGroupResourceProfileId,
    null,
    state,
    g,
    expectedMutations,
    done
  );
});
