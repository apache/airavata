import { services } from "../index";

const createExperiment = async function ({
  applicationName, // name of the application interface (usually the same as the application module)
  computeResourceName,
  experimentName,
  experimentInputs,
} = {}) {
  let applicationInterface = null;
  if (applicationName) {
    applicationInterface = await loadApplicationInterfaceByName(
      applicationName
    );
  } else {
    throw new Error("applicationName is required");
  }
  const applicationModuleId = applicationInterface.applicationModuleId;
  let computeResourceId = null;
  if (computeResourceName) {
    computeResourceId = await loadComputeResourceIdByName(computeResourceName);
  } else {
    throw new Error("computeResourceName is required");
  }
  let groupResourceProfile = await loadGroupResourceProfile(computeResourceId);
  let deployments = await loadApplicationDeployments(
    applicationModuleId,
    groupResourceProfile
  );
  const deployment = deployments.find(
    (d) => d.computeHostId === computeResourceId
  );
  if (!deployment) {
    throw new Error(
      `Couldn't find a deployment for compute resource ${computeResourceId}`
    );
  }
  let queueDescription = await loadQueue(deployment);
  let workspacePreferences = await loadWorkspacePreferences();
  const projectId = workspacePreferences.most_recent_project_id;

  const experiment = applicationInterface.createExperiment();
  if (experimentName) {
    experiment.experimentName = experimentName;
  } else {
    experiment.experimentName = `${
      applicationInterface.applicationName
    } on ${new Date().toLocaleString([], {
      dateStyle: "medium",
      timeStyle: "short",
    })}`;
  }
  experiment.projectId = projectId;
  experiment.userConfigurationData.groupResourceProfileId =
    groupResourceProfile.groupResourceProfileId;
  experiment.userConfigurationData.computationalResourceScheduling.resourceHostId = computeResourceId;
  experiment.userConfigurationData.computationalResourceScheduling.totalCPUCount =
    queueDescription.defaultCPUCount;
  experiment.userConfigurationData.computationalResourceScheduling.nodeCount =
    queueDescription.defaultNodeCount;
  experiment.userConfigurationData.computationalResourceScheduling.wallTimeLimit =
    queueDescription.defaultWalltime;
  experiment.userConfigurationData.computationalResourceScheduling.queueName =
    queueDescription.queueName;

  if (experimentInputs) {
    for (let input of experiment.experimentInputs) {
      if (input.name in experimentInputs) {
        input.value = experimentInputs[input.name];
      }
    }
  }
  return experiment;
};

const loadApplicationInterfaceByName = async function (applicationName) {
  const applicationInterfaces = await services.ApplicationInterfaceService.list();
  const applicationInterface = applicationInterfaces.find(
    (ai) => ai.applicationName === applicationName
  );
  if (!applicationInterface) {
    throw new Error(
      `Could not find application interface named ${applicationName}`
    );
  }
  return applicationInterface;
};

const loadComputeResourceIdByName = async function (computeResourceName) {
  const computeResourceNames = await services.ComputeResourceService.names();
  for (const computeResourceId in computeResourceNames) {
    if (
      computeResourceNames.hasOwnProperty(computeResourceId) &&
      computeResourceNames[computeResourceId] === computeResourceName
    ) {
      return computeResourceId;
    }
  }
  throw new Error(
    `Could not find compute resource with name ${computeResourceName}`
  );
};

const loadGroupResourceProfile = async function (computeResourceId) {
  const groupResourceProfiles = await services.GroupResourceProfileService.list();
  const groupResourceProfile = groupResourceProfiles.find((grp) => {
    for (let computePref of grp.computePreferences) {
      if (computePref.computeResourceId === computeResourceId) {
        return true;
      }
    }
    return false;
  });
  if (!groupResourceProfile) {
    throw new Error(
      `Couldn't find a group resource profile for compute resource ${computeResourceId}`
    );
  }
  return groupResourceProfile;
};

const loadApplicationDeployments = async function (
  applicationModuleId,
  groupResourceProfile
) {
  return await services.ApplicationDeploymentService.list({
    appModuleId: applicationModuleId,
    groupResourceProfileId: groupResourceProfile.groupResourceProfileId,
  });
};

const loadQueue = async function (applicationDeployment) {
  const queues = await services.ApplicationDeploymentService.getQueues({
    lookup: applicationDeployment.appDeploymentId,
  });
  const queue = queues.find((q) => q.isDefaultQueue);
  if (!queue) {
    throw new Error(
      "Couldn't find a default queue for deployment " +
        applicationDeployment.appDeploymentId
    );
  }
  return queue;
};

const loadWorkspacePreferences = async function () {
  return await services.WorkspacePreferencesService.get();
};

export {
  createExperiment,
};

export default {
  createExperiment,
};
