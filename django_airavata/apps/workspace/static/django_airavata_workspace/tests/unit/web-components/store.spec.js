import { actions, mutations } from "@/web-components/store.js";
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
      { type: "updateWallTimeLimit", payload: { wallTimeLimit: 6} },
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
    expectedMutations: [
    ],
    done,
  });
});
