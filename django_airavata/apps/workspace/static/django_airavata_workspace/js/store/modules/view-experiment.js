import { errors, models, services } from "django-airavata-api";
import ExperimentState from "django-airavata-api/static/django_airavata_api/js/models/ExperimentState";
import JobState from "django-airavata-api/static/django_airavata_api/js/models/JobState";

// const PROMISES = {
//   workspacePreferences: null,
// };
export const mutations = {
  setFullExperiment(state, { fullExperiment }) {
    state.fullExperiment = fullExperiment;
  },
  setLaunching(state, { launching }) {
    state.launching = launching;
  },
  startPolling(state) {
    state.polling = true;
  },
  stopPolling(state) {
    state.polling = false;
  },
  setClonedExperiment(state, { clonedExperiment }) {
    state.clonedExperiment = clonedExperiment;
  },
  setRunningIntermediateOutputFetches(
    state,
    { runningIntermediateOutputFetches }
  ) {
    state.runningIntermediateOutputFetches = runningIntermediateOutputFetches;
  },
  setApplicationInterface(state, { applicationInterface }) {
    state.applicationInterface = applicationInterface;
  },
};
export const actions = {
  async setInitialFullExperimentData({ dispatch }, { fullExperimentData }) {
    const fullExperiment = await services.FullExperimentService.retrieve({
      lookup: fullExperimentData.experimentId,
      initialFullExperimentData: fullExperimentData,
    });
    dispatch("setFullExperiment", { fullExperiment });
  },
  async setFullExperiment({ dispatch, commit }, { fullExperiment }) {
    commit("setFullExperiment", { fullExperiment });
    const appInterfaceId = fullExperiment.experiment.executionId;
    try {
      const applicationInterface = await services.ApplicationInterfaceService.retrieve(
        { lookup: appInterfaceId },
        { ignoreErrors: true }
      );
      commit("setApplicationInterface", { applicationInterface });
    } catch (error) {
      // Ignore when application interface is not found; it was probably deleted
      // But in all other cases, report the error as unhandled
      if (!errors.ErrorUtils.isNotFoundError(error)) {
        errors.UnhandledErrorDispatcher.reportUnhandledError(error);
      }
    }
    dispatch("initPollingExperiment");
  },
  setLaunching({ dispatch, commit }, { launching }) {
    commit("setLaunching", { launching });
    if (launching) {
      dispatch("initPollingExperiment");
    }
  },
  async loadExperiment({ commit }, { experimentId, showSpinner = false }) {
    const fullExperiment = await services.FullExperimentService.retrieve(
      { lookup: experimentId },
      { ignoreErrors: true, showSpinner }
    );
    commit("setFullExperiment", { fullExperiment });
  },
  async pollExperiment({ commit, dispatch, state }) {
    if (!state.fullExperiment) {
      commit("stopPolling");
      return;
    }
    if (
      (state.launching && !state.fullExperiment.experiment.hasLaunched) ||
      state.fullExperiment.experiment.isProgressing
    ) {
      try {
        await dispatch("loadExperiment", {
          experimentId: state.fullExperiment.experimentId,
        });
        setTimeout(() => {
          dispatch("pollExperiment");
        }, 3000);
      } catch (error) {
        // Wait 30 seconds after an error and then try again
        setTimeout(() => {
          dispatch("pollExperiment");
        }, 30000);
      }
    } else {
      commit("stopPolling");
    }
  },
  initPollingExperiment({ commit, dispatch, getters }) {
    // Only start polling if we aren't already polling
    if (!getters.isPolling) {
      commit("startPolling");
      dispatch("pollExperiment");
    }
  },
  async clone({ commit, getters }) {
    const clonedExperiment = await services.ExperimentService.clone({
      lookup: getters.experimentId,
    });
    commit("setClonedExperiment", { clonedExperiment });
  },
  async launch({ dispatch, getters }) {
    try {
      await services.ExperimentService.launch({
        lookup: getters.experimentId,
      });
      dispatch("setLaunching", { launching: true });
    } catch (error) {
      // TODO: handle launch error
    }
  },
  async cancel({ dispatch, getters }) {
    await services.ExperimentService.cancel({
      lookup: getters.experimentId,
    });
    dispatch("loadExperiment", { experimentId: getters.experimentId });
  },
  async submitFetchIntermediateOutputs(
    { commit, getters, state },
    { outputNames }
  ) {
    await services.ExperimentService.fetchIntermediateOutputs({
      lookup: getters.experimentId,
      data: {
        outputNames,
      },
    });
    // add an entry for each output name in a runningIntermediateOutputFetches, with timestamp
    for (const outputName of outputNames) {
      commit("setRunningIntermediateOutputFetches", {
        runningIntermediateOutputFetches: {
          ...state.runningIntermediateOutputFetches,
          [outputName]: new Date(),
        },
      });
    }
  },
};

function getDataProducts(io, collection) {
  if (!io.value || !collection) {
    return [];
  }
  let dataProducts = null;
  if (io.type === models.DataType.URI_COLLECTION) {
    const dataProductURIs = io.value.split(",");
    dataProducts = dataProductURIs.map((uri) =>
      collection.find((dp) => dp.productUri === uri)
    );
  } else {
    const dataProductURI = io.value;
    dataProducts = collection.filter((dp) => dp.productUri === dataProductURI);
  }
  return dataProducts ? dataProducts.filter((dp) => (dp ? true : false)) : [];
}

export const getters = {
  isPolling: (state) => state.polling,
  experimentId: (state) =>
    state.fullExperiment ? state.fullExperiment.experimentId : null,
  experiment: (state) =>
    state.fullExperiment ? state.fullExperiment.experiment : null,
  isExecuting: (state, getters) =>
    getters.experiment &&
    getters.experiment.latestStatus &&
    getters.experiment.latestStatus.state === ExperimentState.EXECUTING,
  isFinished: (state, getters) =>
    getters.experiment && getters.experiment.isFinished,
  finishedOrExecuting: (state, getters) =>
    getters.experiment &&
    (getters.experiment.isFinished || getters.isExecuting),
  outputDataProducts(state) {
    const result = {};
    if (state.fullExperiment && state.fullExperiment.outputDataProducts) {
      state.fullExperiment.experiment.experimentOutputs.forEach((output) => {
        result[output.name] = getDataProducts(
          output,
          state.fullExperiment.outputDataProducts
        );
      });
    }
    return result;
  },
  // getter that derives a map of output names and whether they are currently executing
  currentlyRunningIntermediateOutputFetches(state, getters) {
    const result = {};
    if (getters.experiment) {
      for (const output of getters.experiment.experimentOutputs) {
        const runningIntermediateOutputFetchTimestamp =
          state.runningIntermediateOutputFetches[output.name];
        const processStatus = output.intermediateOutput
          ? output.intermediateOutput.processStatus
          : null;
        const processStatusTimestamp = processStatus
          ? processStatus.timeOfStateChange
          : null;
        result[output.name] = false;
        // If our most recent timestamp for the intermediate output is the
        // request to fetch it, the assume it is currently running
        if (
          runningIntermediateOutputFetchTimestamp &&
          (!processStatusTimestamp ||
            processStatusTimestamp < runningIntermediateOutputFetchTimestamp)
        ) {
          result[output.name] = true;
        }
        // intermediate output fetch is still running if process isn't finished
        else if (processStatus) {
          result[output.name] = !processStatus.isFinished;
        }
      }
    }
    return result;
  },
  userHasWriteAccess(state, getters) {
    return getters.experiment ? getters.experiment.userHasWriteAccess : false;
  },
  isJobActive(state) {
    return (
      state.fullExperiment &&
      state.fullExperiment.jobDetails &&
      state.fullExperiment.jobDetails.some(
        (job) =>
          job.latestJobStatus &&
          job.latestJobStatus.jobState === JobState.ACTIVE
      )
    );
  },
  showQueueSettings(state) {
    return state.applicationInterface
      ? state.applicationInterface.showQueueSettings
      : false;
  },
};

const state = {
  fullExperiment: null,
  launching: false,
  polling: false,
  clonedExperiment: null,
  runningIntermediateOutputFetches: {},
  applicationInterface: null,
};
export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};
