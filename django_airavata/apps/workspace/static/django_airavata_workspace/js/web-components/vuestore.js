import { services } from "django-airavata-api";
import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    experiment: null,
    projects: null,
    defaultProjectId: null,
    computeResourceNames: null,
  },
  mutations: {
    setExperiment(state, { experiment }) {
      state.experiment = experiment;
    },
    updateExperimentName(state, { name }) {
      state.experiment.experimentName = name;
    },
    updateExperimentInputValue(state, { inputName, value }) {
      const experimentInput = state.experiment.experimentInputs.find(
        (i) => i.name === inputName
      );
      experimentInput.value = value;
    },
    updateProjectId(state, { projectId }) {
      state.experiment.projectId = projectId;
    },
    updateUserConfigurationData(state, { userConfigurationData }) {
      state.experiment.userConfigurationData = userConfigurationData;
    },
    setProjects(state, { projects }) {
      state.projects = projects;
    },
    setDefaultProjectId(state, { defaultProjectId }) {
      state.defaultProjectId = defaultProjectId;
    },
    setComputeResourceNames(state, { computeResourceNames }) {
      state.computeResourceNames = computeResourceNames;
    },
  },
  actions: {
    async loadNewExperiment({ commit }, { applicationId }) {
      const applicationModule = await services.ApplicationModuleService.retrieve(
        {
          lookup: applicationId,
        }
      );
      const appInterface = await services.ApplicationModuleService.getApplicationInterface(
        {
          lookup: applicationId,
        }
      );
      const experiment = appInterface.createExperiment();
      const currentDate = new Date().toLocaleString([], {
        dateStyle: "medium",
        timeStyle: "short",
      });
      experiment.experimentName = `${applicationModule.appModuleName} on ${currentDate}`;
      commit("setExperiment", { experiment });
    },
    async loadExperiment({ commit }, { experimentId }) {
      const experiment = await services.ExperimentService.retrieve({
        lookup: experimentId,
      });
      commit("setExperiment", { experiment });
    },
    updateExperimentName({ commit }, { name }) {
      commit("updateExperimentName", { name });
    },
    updateExperimentInputValue({ commit }, { inputName, value }) {
      commit("updateExperimentInputValue", { inputName, value });
    },
    updateProjectId({ commit }, { projectId }) {
      commit("updateProjectId", { projectId });
    },
    updateUserConfigurationData({ commit }, { userConfigurationData }) {
      commit("updateUserConfigurationData", { userConfigurationData });
    },
    async saveExperiment({ commit, getters }) {
      if (getters.experiment.experimentId) {
        const experiment = await services.ExperimentService.update({
          data: getters.experiment,
          lookup: getters.experiment.experimentId,
        });
        commit("setExperiment", { experiment });
      } else {
        const experiment = await services.ExperimentService.create({
          data: getters.experiment,
        });
        commit("setExperiment", { experiment });
      }
    },
    async launchExperiment({ getters }) {
      await services.ExperimentService.launch({
        lookup: getters.experiment.experimentId,
      });
    },
    async loadProjects({ commit }) {
      const projects = await services.ProjectService.listAll();
      commit("setProjects", { projects });
    },
    async loadDefaultProjectId({ commit }) {
      // TODO: cache the workspace preferences so they aren't loaded more than once
      const prefs = await services.WorkspacePreferencesService.get();
      const defaultProjectId = prefs.most_recent_project_id;
      commit("setDefaultProjectId", { defaultProjectId });
    },
    async loadComputeResourceNames({ commit }) {
      const computeResourceNames = await services.ComputeResourceService.names();
      commit("setComputeResourceNames", { computeResourceNames });
    },
  },
  getters: {
    getExperimentInputByName: (state) => (name) => {
      if (!state.experiment) {
        return null;
      }
      const experimentInputs = state.experiment.experimentInputs;
      if (experimentInputs) {
        for (const experimentInput of experimentInputs) {
          if (experimentInput.name === name) {
            return experimentInput;
          }
        }
      }
      return null;
    },
    experiment: (state) => state.experiment,
    projects: (state) => state.projects,
    defaultProjectId: (state) => state.defaultProjectId,
    computeResourceNames: (state) => state.computeResourceNames,
  },
});
