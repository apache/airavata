import { errors, services, utils } from "django-airavata-api";
import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

const PROMISES = {
  workspacePreferences: null,
};
let groupResourceProfileIdIsSet = false;
let resourceHostIdIsSet = false;
let queueSettingsAreSet = false;
let applicationModuleIdIsSet = false;

// For non-experiment editing case, need to defer compute resource settings
// initialization until each components' settings have been set
const areAllComputeResourceSettingsSet = () =>
  groupResourceProfileIdIsSet &&
  resourceHostIdIsSet &&
  queueSettingsAreSet &&
  applicationModuleIdIsSet;

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
  updateExperimentGroupResourceProfileId(state, { groupResourceProfileId }) {
    state.experiment.userConfigurationData.groupResourceProfileId = groupResourceProfileId;
  },
  updateGroupResourceProfileId(state, { groupResourceProfileId }) {
    state.groupResourceProfileId = groupResourceProfileId;
    groupResourceProfileIdIsSet = true;
  },
  updateExperimentResourceHostId(state, { resourceHostId }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.resourceHostId = resourceHostId;
  },
  updateResourceHostId(state, { resourceHostId }) {
    state.resourceHostId = resourceHostId;
    resourceHostIdIsSet = true;
  },
  updateExperimentQueueName(state, { queueName }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.queueName = queueName;
  },
  updateQueueName(state, { queueName }) {
    state.queueName = queueName;
    // Assume all queue settings are initialized at once
    queueSettingsAreSet = true;
  },
  updateExperimentTotalCPUCount(state, { totalCPUCount }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount = totalCPUCount;
  },
  updateExperimentNodeCount(state, { nodeCount }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.nodeCount = nodeCount;
  },
  updateExperimentWallTimeLimit(state, { wallTimeLimit }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit = wallTimeLimit;
  },
  updateExperimentTotalPhysicalMemory(state, { totalPhysicalMemory }) {
    state.experiment.userConfigurationData.computationalResourceScheduling.totalPhysicalMemory = totalPhysicalMemory;
  },
  updateTotalCPUCount(state, { totalCPUCount }) {
    state.totalCPUCount = totalCPUCount;
  },
  updateNodeCount(state, { nodeCount }) {
    state.nodeCount = nodeCount;
  },
  updateWallTimeLimit(state, { wallTimeLimit }) {
    state.wallTimeLimit = wallTimeLimit;
  },
  updateTotalPhysicalMemory(state, { totalPhysicalMemory }) {
    state.totalPhysicalMemory = totalPhysicalMemory;
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
    applicationModuleIdIsSet = true;
  },
  setApplicationDeployments(state, { applicationDeployments }) {
    state.applicationDeployments = applicationDeployments;
  },
  setAppDeploymentQueues(state, { appDeploymentQueues }) {
    state.appDeploymentQueues = appDeploymentQueues;
  },
  setApplicationInterface(state, { applicationInterface }) {
    state.applicationInterface = applicationInterface;
  },
};
export const actions = {
  async loadNewExperiment({ commit, dispatch }, { applicationId }) {
    const applicationModule = await services.ApplicationModuleService.retrieve({
      lookup: applicationId,
    });
    const applicationInterface = await dispatch(
      "initializeApplicationInterface",
      { applicationModuleId: applicationId }
    );
    const experiment = applicationInterface.createExperiment();
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
    const applicationInterface = await services.ApplicationInterfaceService.retrieve(
      {
        lookup: experiment.executionId,
      }
    );
    commit("setApplicationInterface", { applicationInterface });
    commit("setApplicationModuleId", {
      applicationModuleId: applicationInterface.applicationModuleId,
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

    // Since experiment is set, all of the compute resource settings are now
    // assumed to be initialized so we can do the cross component initialization
    dispatch("initializeComputeResourceSettings");
  },
  async initializeComputeResourceSettings({ dispatch, getters, state }) {
    // This method initializes GroupResourceProfile, ApplicationDeployments and
    // Queue settings at once since there they are interdependent.
    // This method should only be called after groupResourceProfileId,
    // resourceHostId, queue settings and applicationModuleId are all set (see
    // areAllComputeResourceSettingsSet).

    await dispatch("initializeGroupResourceProfile");
    // applicationInterface is initialized already when creating/editing an
    // experiment but needs to be done explicitly when using other web
    // components standalone
    await dispatch("initializeApplicationInterface", {
      applicationModuleId: state.applicationModuleId,
    });
    const groupResourceProfileId = getters.groupResourceProfileId;
    // If there is a group resource profile, load additional necessary
    // data and re-apply group resource profile
    if (groupResourceProfileId) {
      await dispatch("loadApplicationDeployments");
      await dispatch("loadAppDeploymentQueues");
      await dispatch("applyGroupResourceProfile");
    }
  },
  async initializeApplicationInterface({ commit }, { applicationModuleId }) {
    const applicationInterface = await services.ApplicationModuleService.getApplicationInterface(
      {
        lookup: applicationModuleId,
      }
    );
    commit("setApplicationInterface", { applicationInterface });
    return applicationInterface;
  },
  async initializeGroupResourceProfile({ commit, dispatch, getters, state }) {
    await dispatch("loadGroupResourceProfiles");
    await dispatch("loadWorkspacePreferences");
    let result = getters.groupResourceProfileId;

    if (
      !getters.groupResourceProfileId ||
      !getters.findGroupResourceProfile(getters.groupResourceProfileId)
    ) {
      // Figure out a default value for groupResourceProfileId
      if (
        getters.findGroupResourceProfile(
          state.workspacePreferences.most_recent_group_resource_profile_id
        )
      ) {
        result =
          state.workspacePreferences.most_recent_group_resource_profile_id;
      } else if (state.groupResourceProfiles.length > 0) {
        result = state.groupResourceProfiles[0].groupResourceProfileId;
      } else {
        result = null;
      }
    }
    if (state.experiment) {
      commit("updateExperimentGroupResourceProfileId", {
        groupResourceProfileId: result,
      });
    } else {
      commit("updateGroupResourceProfileId", {
        groupResourceProfileId: result,
      });
    }
  },
  async initializeGroupResourceProfileId(
    { commit, dispatch, state },
    { groupResourceProfileId }
  ) {
    commit("updateGroupResourceProfileId", {
      groupResourceProfileId,
    });
    // only for non-experiment loading case do we call initializeComputeResourceSettings
    if (!state.experiment && areAllComputeResourceSettingsSet()) {
      dispatch("initializeComputeResourceSettings");
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
    { commit, dispatch, getters, state },
    { groupResourceProfileId }
  ) {
    const oldValue = getters.groupResourceProfileId;
    if (state.experiment) {
      commit("updateExperimentGroupResourceProfileId", {
        groupResourceProfileId,
      });
    } else {
      commit("updateGroupResourceProfileId", { groupResourceProfileId });
    }
    if (groupResourceProfileId && oldValue !== groupResourceProfileId) {
      await dispatch("loadApplicationDeployments");
      await dispatch("applyGroupResourceProfile");
    }
  },
  async updateComputeResourceHostId(
    { commit, dispatch, getters, state },
    { resourceHostId }
  ) {
    if (getters.resourceHostId !== resourceHostId) {
      if (state.experiment) {
        commit("updateExperimentResourceHostId", { resourceHostId });
      } else {
        commit("updateResourceHostId", { resourceHostId });
      }
      await dispatch("loadAppDeploymentQueues");
      await dispatch("setDefaultQueue");
    }
  },
  async initializeQueueSettings(
    { commit, dispatch, state },
    { queueName, nodeCount, totalCPUCount, wallTimeLimit, totalPhysicalMemory }
  ) {
    commit("updateQueueName", { queueName });
    commit("updateNodeCount", { nodeCount });
    commit("updateTotalCPUCount", { totalCPUCount });
    commit("updateWallTimeLimit", { wallTimeLimit });
    commit("updateTotalPhysicalMemory", { totalPhysicalMemory });

    // only for non-experiment loading case do we call initializeComputeResourceSettings
    if (!state.experiment && areAllComputeResourceSettingsSet()) {
      dispatch("initializeComputeResourceSettings");
    }
  },
  updateQueueName({ commit, dispatch, state }, { queueName }) {
    if (state.experiment) {
      commit("updateExperimentQueueName", { queueName });
    } else {
      commit("updateQueueName", { queueName });
    }
    dispatch("initializeQueue");
  },
  updateTotalCPUCount(
    { commit, getters, state },
    { totalCPUCount, enableNodeCountToCpuCheck }
  ) {
    if (state.experiment) {
      commit("updateExperimentTotalCPUCount", { totalCPUCount });
    } else {
      commit("updateTotalCPUCount", { totalCPUCount });
    }
    if (enableNodeCountToCpuCheck && getters.queue.cpuPerNode > 0) {
      const totalCPUCountInt = parseInt(totalCPUCount);
      const nodeCount = Math.min(
        Math.ceil(totalCPUCountInt / getters.queue.cpuPerNode),
        getters.maxAllowedNodes
      );
      if (state.experiment) {
        commit("updateExperimentNodeCount", { nodeCount });
      } else {
        commit("updateNodeCount", { nodeCount });
      }
    }
  },
  updateNodeCount(
    { commit, getters, state },
    { nodeCount, enableNodeCountToCpuCheck }
  ) {
    if (state.experiment) {
      commit("updateExperimentNodeCount", { nodeCount });
    } else {
      commit("updateNodeCount", { nodeCount });
    }
    if (enableNodeCountToCpuCheck && getters.queue.cpuPerNode > 0) {
      const nodeCountInt = parseInt(nodeCount);
      const totalCPUCount = Math.min(
        nodeCountInt * getters.queue.cpuPerNode,
        getters.maxAllowedCores
      );
      if (state.experiment) {
        commit("updateExperimentTotalCPUCount", { totalCPUCount });
      } else {
        commit("updateTotalCPUCount", { totalCPUCount });
      }
    }
  },
  updateWallTimeLimit({ commit, state }, { wallTimeLimit }) {
    if (state.experiment) {
      commit("updateExperimentWallTimeLimit", { wallTimeLimit });
    } else {
      commit("updateWallTimeLimit", { wallTimeLimit });
    }
  },
  updateTotalPhysicalMemory({ commit, state }, { totalPhysicalMemory }) {
    if (state.experiment) {
      commit("updateExperimentTotalPhysicalMemory", { totalPhysicalMemory });
    } else {
      commit("updateTotalPhysicalMemory", { totalPhysicalMemory });
    }
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
  async initializeComputeResources(
    { commit, dispatch, state },
    { applicationModuleId, resourceHostId = null }
  ) {
    commit("setApplicationModuleId", { applicationModuleId });
    commit("updateResourceHostId", {
      resourceHostId,
    });
    // only for non-experiment loading case do we call initializeComputeResourceSettings
    if (!state.experiment && areAllComputeResourceSettingsSet()) {
      dispatch("initializeComputeResourceSettings");
    }
  },
  async initializeResourceHostId({ commit, dispatch, getters, state }) {
    // if there isn't a selected compute resource or there is but it isn't in
    // the list of app deployments, set a default one
    // Returns true if the resourceHostId changed
    if (
      !getters.resourceHostId ||
      !getters.computeResources.find((crid) => crid === getters.resourceHostId)
    ) {
      const defaultResourceHostId = await dispatch("getDefaultResourceHostId");
      if (state.experiment) {
        commit("updateExperimentResourceHostId", {
          resourceHostId: defaultResourceHostId,
        });
      } else {
        commit("updateResourceHostId", {
          resourceHostId: defaultResourceHostId,
        });
      }
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
  initializeQueue({ commit, getters, state }) {
    const queue = getters.queue;
    if (queue) {
      if (state.experiment) {
        commit("updateExperimentTotalCPUCount", {
          totalCPUCount: getters.getDefaultCPUCount(queue),
        });
        commit("updateExperimentNodeCount", {
          nodeCount: getters.getDefaultNodeCount(queue),
        });
        commit("updateExperimentWallTimeLimit", {
          wallTimeLimit: getters.getDefaultWalltime(queue),
        });
        commit("updateExperimentTotalPhysicalMemory", {
          totalPhysicalMemory: 0,
        });
      } else {
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
      }
    } else {
      if (state.experiment) {
        commit("updateExperimentTotalCPUCount", { totalCPUCount: 0 });
        commit("updateExperimentNodeCount", { nodeCount: 0 });
        commit("updateExperimentWallTimeLimit", { wallTimeLimit: 0 });
        commit("updateExperimentTotalPhysicalMemory", {
          totalPhysicalMemory: 0,
        });
      } else {
        commit("updateTotalCPUCount", { totalCPUCount: 0 });
        commit("updateNodeCount", { nodeCount: 0 });
        commit("updateWallTimeLimit", { wallTimeLimit: 0 });
        commit("updateTotalPhysicalMemory", { totalPhysicalMemory: 0 });
      }
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
      : state.groupResourceProfileId,
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
      : state.resourceHostId,
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
      : state.queueName;
  },
  totalCPUCount: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .totalCPUCount
      : state.totalCPUCount;
  },
  nodeCount: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .nodeCount
      : state.nodeCount;
  },
  wallTimeLimit: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .wallTimeLimit
      : state.wallTimeLimit;
  },
  totalPhysicalMemory: (state) => {
    return state.experiment &&
      state.experiment.userConfigurationData &&
      state.experiment.userConfigurationData.computationalResourceScheduling
      ? state.experiment.userConfigurationData.computationalResourceScheduling
          .totalPhysicalMemory
      : state.totalPhysicalMemory;
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
  showQueueSettings: (state) =>
    state.applicationInterface
      ? state.applicationInterface.showQueueSettings
      : false,
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
    workspacePreferences: null,
    // These state variables are to enable using UI components outside of a
    // normal experiment editing context
    queueName: null,
    nodeCount: null,
    totalCPUCount: null,
    wallTimeLimit: null,
    totalPhysicalMemory: null,
    groupResourceProfileId: null,
    resourceHostId: null,
    applicationInterface: null,
  },
  mutations,
  actions,
  getters,
});
