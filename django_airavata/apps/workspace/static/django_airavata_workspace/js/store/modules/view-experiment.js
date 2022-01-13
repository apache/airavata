import { services } from "django-airavata-api";

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
};
export const actions = {
  async setInitialFullExperimentData(
    { commit, dispatch },
    { fullExperimentData }
  ) {
    const fullExperiment = await services.FullExperimentService.retrieve({
      lookup: fullExperimentData.experimentId,
      initialFullExperimentData: fullExperimentData,
    });
    commit("setFullExperiment", { fullExperiment });
    dispatch("initPollingExperiment");
  },
  setFullExperiment({ dispatch, commit }, { fullExperiment }) {
    commit("setFullExperiment", { fullExperiment });
    dispatch("initPollingExperiment");
  },
  setLaunching({ dispatch, commit }, { launching }) {
    commit("setLaunching", { launching });
    if (launching) {
      dispatch("initPollingExperiment");
    }
  },
  async loadExperiment({ commit }, { experimentId }) {
    const fullExperiment = await services.FullExperimentService.retrieve(
      { lookup: experimentId },
      { ignoreErrors: true, showSpinner: false }
    );
    commit("setFullExperiment", { fullExperiment });
  },
  async pollExperiment({ commit, dispatch, state }) {
    if (!state.fullExperiment) {
      return;
    }
    if (
      (state.launching && !state.fullExperiment.experiment.hasLaunched) ||
      state.fullExperiment.experiment.isProgressing
    ) {
      commit("startPolling");
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
  initPollingExperiment({ dispatch, getters }) {
    // Only start polling if we aren't already polling
    if (!getters.isPolling) {
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
};

export const getters = {
  isPolling: (state) => state.polling,
  experimentId: (state) => state.fullExperiment.experimentId,
};

export default {
  namespaced: true,
  state: {
    fullExperiment: null,
    launching: false,
    polling: false,
    clonedExperiment: null,
  },
  mutations,
  actions,
  getters,
};
