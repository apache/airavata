import { errors, services, utils } from "django-airavata-api";
import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

const PROMISES = {
  workspacePreferences: null,
};
export const mutations = {
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
  updateGroupResourceProfileId(state, { groupResourceProfileId }) {
    state.experiment.userConfigurationData.groupResourceProfileId = groupResourceProfileId;
  },
  updateResourceHostId(state, { resourceHostId }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.resourceHostId = resourceHostId;
  },
  updateQueueName(state, { queueName }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.queueName = queueName;
  },
  setLazyQueueName(state, { queueName }) {
    state.queueName = queueName;
  },
  updateTotalCPUCount(state, { totalCPUCount }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount = totalCPUCount;
  },
  updateNodeCount(state, { nodeCount }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.nodeCount = nodeCount;
  },
  updateWallTimeLimit(state, { wallTimeLimit }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit = wallTimeLimit;
  },
  updateTotalPhysicalMemory(state, { totalPhysicalMemory }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.totalPhysicalMemory = totalPhysicalMemory;
  },
  setProjects(state, { projects }) {
    state.projects = projects;
  },
  setComputeResourceNames(state, { computeResourceNames }) {
    state.computeResourceNames = computeResourceNames;
  },
  setGroupResourceProfiles(state, { groupResourceProfiles }) {
    state.groupResourceProfiles = groupResourceProfiles;
  },
  setWorkspacePreferences(state, { workspacePreferences }) {
    state.workspacePreferences = workspacePreferences;
  },
  setApplicationModuleId(state, { applicationModuleId }) {
    state.applicationModuleId = applicationModuleId;
  },
  setApplicationDeployments(state, { applicationDeployments }) {
    state.applicationDeployments = applicationDeployments;
  },
  setAppDeploymentQueues(state, { appDeploymentQueues }) {
    state.appDeploymentQueues = appDeploymentQueues;
  },
};
export const actions = {
  async loadNewExperiment({ commit, dispatch }, { applicationId }) {
    const applicationModule = await services.ApplicationModuleService.retrieve({
      lookup: applicationId,
    });
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
    commit("setApplicationModuleId", { applicationModuleId: applicationId });
    await dispatch("setExperiment", { experiment });
  },
  async loadExperiment({ commit, dispatch }, { experimentId }) {
    const experiment = await services.ExperimentService.retrieve({
      lookup: experimentId,
    });
    const appInterface = await services.ApplicationInterfaceService.retrieve({
      lookup: experiment.executionId,
    });
    commit("setApplicationModuleId", {
      applicationModuleId: appInterface.applicationModuleId,
    });
    await dispatch("setExperiment", { experiment });
  },
  async setExperiment({ commit, dispatch, state }, { experiment }) {
    commit("setExperiment", { experiment });
    await dispatch("loadExperimentData");
    // Check lazy experiment state properties and apply them
    if (state.queueName) {
      dispatch("updateQueueName", { queueName: state.queueName });
    }
  },
  async loadExperimentData({ commit, dispatch, state }) {
    await Promise.all([
      dispatch("loadProjects"),
      dispatch("loadWorkspacePreferences"),
      dispatch("loadGroupResourceProfiles"),
    ]);

    if (!state.experiment.projectId) {
      commit("updateProjectId", {
        projectId: state.workspacePreferences.most_recent_project_id,
      });
    }

    dispatch("initializeGroupResourceProfileId");
    const groupResourceProfileId =
      state.experiment.userConfigurationData.groupResourceProfileId;
    // If experiment has a group resource profile, load additional necessary
    // data and re-apply group resource profile
    if (groupResourceProfileId) {
      await dispatch("loadApplicationDeployments");
      await dispatch("loadAppDeploymentQueues");
      await dispatch("applyGroupResourceProfile");
    }
  },
  initializeGroupResourceProfileId({ commit, getters, state }) {
    // If there is no groupResourceProfileId set on the experiment, or there
    // is one set but it is no longer in the list of accessible
    // groupResourceProfiles, set to the default one, or the first one
    let groupResourceProfileId =
      state.experiment.userConfigurationData.groupResourceProfileId;
    if (
      !groupResourceProfileId ||
      !getters.findGroupResourceProfile(groupResourceProfileId)
    ) {
      if (
        getters.findGroupResourceProfile(
          state.workspacePreferences.most_recent_group_resource_profile_id
        )
      ) {
        commit("updateGroupResourceProfileId", {
          groupResourceProfileId:
            state.workspacePreferences.most_recent_group_resource_profile_id,
        });
      } else if (state.groupResourceProfiles.length > 0) {
        commit("updateGroupResourceProfileId", {
          groupResourceProfileId:
            state.groupResourceProfiles[0].groupResourceProfileId,
        });
      } else {
        commit("updateGroupResourceProfileId", {
          groupResourceProfileId: null,
        });
      }
    }
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
  async updateGroupResourceProfileId(
    { commit, dispatch },
    { groupResourceProfileId }
  ) {
    commit("updateGroupResourceProfileId", { groupResourceProfileId });
    await dispatch("loadApplicationDeployments");
    await dispatch("applyGroupResourceProfile");
  },
  async updateComputeResourceHostId(
    { commit, dispatch, getters },
    { resourceHostId }
  ) {
    if (getters.resourceHostId !== resourceHostId) {
      commit("updateResourceHostId", { resourceHostId });
      await dispatch("loadAppDeploymentQueues");
      await dispatch("setDefaultQueue");
    }
  },
  updateQueueName({ commit, dispatch, state }, { queueName }) {
    if (state.experiment) {
      commit("updateQueueName", { queueName });
      dispatch("initializeQueue");
    } else {
      commit("setLazyQueueName", { queueName });
    }
  },
  updateTotalCPUCount({ commit }, { totalCPUCount }) {
    commit("updateTotalCPUCount", { totalCPUCount });
  },
  updateNodeCount({ commit }, { nodeCount }) {
    commit("updateNodeCount", { nodeCount });
  },
  updateWallTimeLimit({ commit }, { wallTimeLimit }) {
    commit("updateWallTimeLimit", { wallTimeLimit });
  },
  updateTotalPhysicalMemory({ commit }, { totalPhysicalMemory }) {
    commit("updateTotalPhysicalMemory", { totalPhysicalMemory });
  },
  async loadApplicationDeployments({ commit, getters, state }) {
    const applicationDeployments = await services.ApplicationDeploymentService.list(
      {
        appModuleId: state.applicationModuleId,
        groupResourceProfileId: getters.groupResourceProfileId,
      },
      { ignoreErrors: true }
    )
      .catch((error) => {
        // Ignore unauthorized errors, force user to pick another GroupResourceProfile
        if (!errors.ErrorUtils.isUnauthorizedError(error)) {
          return Promise.reject(error);
        } else {
          return Promise.resolve([]);
        }
      })
      // Report all other error types
      .catch(utils.FetchUtils.reportError);
    commit("setApplicationDeployments", { applicationDeployments });
  },
  async applyGroupResourceProfile({ dispatch, getters }) {
    // Make sure that resource host id is in the list of app deployments
    const computeResourceChanged = await dispatch("initializeResourceHostId");
    if (computeResourceChanged) {
      await dispatch("loadAppDeploymentQueues");
      await dispatch("setDefaultQueue");
    } else if (!getters.queue) {
      // allowed queues may have changed. If selected queue isn't in the list
      // of allowed queues, reset to the default
      await dispatch("setDefaultQueue");
    } else {
      // reapply batchQueueResourcePolicy maximums since they may have changed
      dispatch("applyBatchQueueResourcePolicy");
    }
  },
  async initializeResourceHostId({ commit, dispatch, getters }) {
    // if there isn't a selected compute resource or there is but it isn't in
    // the list of app deployments, set a default one
    // Returns true if the resourceHostId changed
    if (
      !getters.resourceHostId ||
      !getters.computeResources.find((crid) => crid === getters.resourceHostId)
    ) {
      const defaultResourceHostId = await dispatch("getDefaultResourceHostId");
      commit("updateResourceHostId", {
        resourceHostId: defaultResourceHostId,
      });
      return true;
    }
    return false;
  },
  async getDefaultResourceHostId({ dispatch, getters }) {
    await dispatch("loadDefaultComputeResourceId");
    if (
      getters.defaultComputeResourceId &&
      getters.computeResources.find(
        (crid) => crid === getters.defaultComputeResourceId
      )
    ) {
      return getters.defaultComputeResourceId;
    } else if (getters.computeResources.length > 0) {
      // Just pick the first one
      return getters.computeResources[0];
    } else {
      return null;
    }
  },
  async loadDefaultComputeResourceId({ dispatch }) {
    await dispatch("loadWorkspacePreferences");
  },
  async loadAppDeploymentQueues({ commit, getters }) {
    const applicationDeployment = getters.applicationDeployment;
    if (applicationDeployment) {
      const appDeploymentQueues = await services.ApplicationDeploymentService.getQueues(
        {
          lookup: applicationDeployment.appDeploymentId,
        }
      );
      commit("setAppDeploymentQueues", { appDeploymentQueues });
    } else {
      commit("setAppDeploymentQueues", { appDeploymentQueues: [] });
    }
  },
  async setDefaultQueue({ dispatch, getters }) {
    // set to the default queue or the first one
    const defaultQueue = getters.defaultQueue;
    if (defaultQueue) {
      dispatch("updateQueueName", { queueName: defaultQueue.queueName });
    } else {
      dispatch("updateQueueName", { queueName: null });
    }
  },
  initializeQueue({ commit, getters }) {
    const queue = getters.queue;
    if (queue) {
      commit("updateTotalCPUCount", {
        totalCPUCount: getters.getDefaultCPUCount(queue),
      });
      commit("updateNodeCount", {
        nodeCount: getters.getDefaultNodeCount(queue),
      });
      commit("updateWallTimeLimit", {
        wallTimeLimit: getters.getDefaultWalltime(queue),
      });
      commit("updateTotalPhysicalMemory", { totalPhysicalMemory: 0 });
    } else {
      commit("updateTotalCPUCount", { totalCPUCount: 0 });
      commit("updateNodeCount", { nodeCount: 0 });
      commit("updateWallTimeLimit", { wallTimeLimit: 0 });
      commit("updateTotalPhysicalMemory", { totalPhysicalMemory: 0 });
    }
  },
  applyBatchQueueResourcePolicy({ commit, getters }) {
    if (getters.batchQueueResourcePolicy) {
      const crs =
        getters.experiment.userConfigurationData
          .computationalResourceScheduling;
      const totalCPUCount = Math.min(
        crs.totalCPUCount,
        getters.batchQueueResourcePolicy.maxAllowedCores
      );
      if (totalCPUCount !== crs.totalCPUCount) {
        commit("updateTotalCPUCount", {
          totalCPUCount,
        });
      }
      const nodeCount = Math.min(
        crs.nodeCount,
        getters.batchQueueResourcePolicy.maxAllowedNodes
      );
      if (nodeCount !== crs.nodeCount) {
        commit("updateNodeCount", {
          nodeCount,
        });
      }
      const wallTimeLimit = Math.min(
        crs.wallTimeLimit,
        getters.batchQueueResourcePolicy.maxAllowedWalltime
      );
      if (wallTimeLimit !== crs.wallTimeLimit) {
        commit("updateWallTimeLimit", {
          wallTimeLimit,
        });
      }
    }
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
    if (!PROMISES.projects) {
      PROMISES.projects = services.ProjectService.listAll();
    }
    const projects = await PROMISES.projects;
    commit("setProjects", { projects });
  },
  async loadWorkspacePreferences({ commit }) {
    if (!PROMISES.workspacePreferences) {
      PROMISES.workspacePreferences = services.WorkspacePreferencesService.get();
    }
    const workspacePreferences = await PROMISES.workspacePreferences;
    commit("setWorkspacePreferences", { workspacePreferences });
  },
  async loadDefaultProjectId({ dispatch }) {
    await dispatch("loadWorkspacePreferences");
  },
  async loadComputeResourceNames({ commit }) {
    const computeResourceNames = await services.ComputeResourceService.names();
    commit("setComputeResourceNames", { computeResourceNames });
  },
  async loadDefaultGroupResourceProfileId({ dispatch }) {
    await dispatch("loadWorkspacePreferences");
  },
  async loadGroupResourceProfiles({ commit }) {
    if (!PROMISES.groupResourceProfiles) {
      PROMISES.groupResourceProfiles = services.GroupResourceProfileService.list();
    }
    const groupResourceProfiles = await PROMISES.groupResourceProfiles;
    commit("setGroupResourceProfiles", { groupResourceProfiles });
  },
};

export const getters = {
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
  defaultProjectId: (state) =>
    state.workspacePreferences
      ? state.workspacePreferences.most_recent_project_id
      : null,
  defaultGroupResourceProfileId: (state) =>
    state.workspacePreferences
      ? state.workspacePreferences.most_recent_group_resource_profile_id
      : null,
  defaultComputeResourceId: (state) =>
    state.workspacePreferences
      ? state.workspacePreferences.most_recent_compute_resource_id
      : null,
  computeResourceNames: (state) => state.computeResourceNames,
  groupResourceProfiles: (state) => state.groupResourceProfiles,
  groupResourceProfileId: (state) =>
    state.experiment
      ? state.experiment.userConfigurationData.groupResourceProfileId
      : null,
  findGroupResourceProfile: (state) => (groupResourceProfileId) =>
    state.groupResourceProfiles
      ? state.groupResourceProfiles.find(
          (g) => g.groupResourceProfileId === groupResourceProfileId
        )
      : null,
  groupResourceProfile: (state, getters) =>
    getters.findGroupResourceProfile(getters.groupResourceProfileId),
  resourceHostId: (state) =>
    state.experiment &&
    state.experiment.userConfigurationData &&
    state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .resourceHostId
      : null,
  computeResources: (state) =>
    state.applicationDeployments.map((dep) => dep.computeHostId),
  applicationDeployment: (state, getters) => {
    if (state.applicationDeployments && getters.resourceHostId) {
      return state.applicationDeployments.find(
        (ad) => ad.computeHostId === getters.resourceHostId
      );
    } else {
      return null;
    }
  },
  isQueueInComputeResourcePolicy: (state, getters) => (queueName) => {
    if (!getters.computeResourcePolicy) {
      return true;
    }
    return getters.computeResourcePolicy.allowedBatchQueues.includes(queueName);
  },
  queues: (state, getters) => {
    return state.appDeploymentQueues
      ? state.appDeploymentQueues.filter((q) =>
          getters.isQueueInComputeResourcePolicy(q.queueName)
        )
      : [];
  },
  defaultQueue: (state, getters) => {
    const defaultQueue = getters.queues.find((q) => q.isDefaultQueue);
    if (defaultQueue) {
      return defaultQueue;
    } else if (getters.queues.length > 0) {
      return getters.queues[0];
    } else {
      return null;
    }
  },
  queueName: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .queueName
      : null;
  },
  totalCPUCount: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .totalCPUCount
      : null;
  },
  nodeCount: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .nodeCount
      : null;
  },
  wallTimeLimit: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .wallTimeLimit
      : null;
  },
  totalPhysicalMemory: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .totalPhysicalMemory
      : null;
  },
  queue: (state, getters) => {
    return getters.queues && getters.queueName
      ? getters.queues.find((q) => q.queueName === getters.queueName)
      : null;
  },
  getDefaultCPUCount: (state, getters) => (queue) => {
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedCores,
        queue.defaultCPUCount
      );
    }
    return queue.defaultCPUCount;
  },
  getDefaultNodeCount: (state, getters) => (queue) => {
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedNodes,
        queue.defaultNodeCount
      );
    }
    return queue.defaultNodeCount;
  },
  getDefaultWalltime: (state, getters) => (queue) => {
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedWalltime,
        queue.defaultWalltime
      );
    }
    return queue.defaultWalltime;
  },
  computeResourcePolicy: (state, getters) => {
    if (!getters.groupResourceProfile || !getters.resourceHostId) {
      return null;
    }
    return getters.groupResourceProfile.computeResourcePolicies.find(
      (crp) => crp.computeResourceId === getters.resourceHostId
    );
  },
  batchQueueResourcePolicies: (state, getters) => {
    if (!getters.groupResourceProfile || !getters.resourceHostId) {
      return null;
    }
    return getters.groupResourceProfile.batchQueueResourcePolicies.filter(
      (bqrp) => bqrp.computeResourceId === getters.resourceHostId
    );
  },
  batchQueueResourcePolicy: (state, getters) => {
    if (!getters.batchQueueResourcePolicies || !getters.queueName) {
      return null;
    }
    return getters.batchQueueResourcePolicies.find(
      (bqrp) => bqrp.queuename === getters.queueName
    );
  },
  maxAllowedCores: (state, getters) => {
    if (!getters.queue) {
      return 0;
    }
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedCores,
        getters.queue.maxProcessors
      );
    }
    return getters.queue.maxProcessors;
  },
  maxAllowedNodes: (state, getters) => {
    if (!getters.queue) {
      return 0;
    }
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedNodes,
        getters.queue.maxNodes
      );
    }
    return getters.queue.maxNodes;
  },
  maxAllowedWalltime: (state, getters) => {
    if (!getters.queue) {
      return 0;
    }
    const batchQueueResourcePolicy = getters.batchQueueResourcePolicy;
    if (batchQueueResourcePolicy) {
      return Math.min(
        batchQueueResourcePolicy.maxAllowedWalltime,
        getters.queue.maxRunTime
      );
    }
    return getters.queue.maxRunTime;
  },
  maxMemory: (state, getters) => {
    return getters.queue ? getters.queue.maxMemory : 0;
  },
};

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== "production",
  state: {
    experiment: null,
    projects: null,
    computeResourceNames: {},
    applicationDeployments: [],
    groupResourceProfiles: null,
    applicationModuleId: null,
    appDeploymentQueues: [],
    // Lazy state fields that will be copied to the experiment once it is loaded
    queueName: null,
  },
  mutations,
  actions,
  getters,
});
