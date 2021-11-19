import { services } from "../index";

const createExperiment = async function ({
  applicationName, // name of the application interface (usually the same as the application module)
  applicationId, // the id of the application module
  applicationInterfaceId, // the id of the application interface
  computeResourceName,
  experimentName,
  experimentInputs,
} = {}) {
  let applicationInterface = null;
  if (applicationInterfaceId) {
    applicationInterface = await loadApplicationInterfaceById(
      applicationInterfaceId
    );
  } else if (applicationId) {
    applicationInterface = await loadApplicationInterfaceByApplicationModuleId(
      applicationId
    );
  } else if (applicationName) {
    applicationInterface = await loadApplicationInterfaceByName(
      applicationName
    );
  } else {
    throw new Error(
      "Either applicationInterfaceId or applicationId or applicationName is required"
    );
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

const loadApplicationInterfaceById = async function (applicationInterfaceId) {
  return await services.ApplicationInterfaceService.retrieve({
    lookup: applicationInterfaceId,
  });
};

const loadApplicationInterfaceByApplicationModuleId = async function (
  applicationId
) {
  return await services.ApplicationModuleService.getApplicationInterface({
    lookup: applicationId,
  });
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

const loadExperiment = async function (experimentId) {
  return await services.ExperimentService.retrieve({ lookup: experimentId });
};

const readDataProduct = async function (
  dataProductURI,
  { bodyType = "text" } = {}
) {
  return await fetch(
    `/sdk/download/?data-product-uri=${encodeURIComponent(dataProductURI)}`,
    {
      credentials: "same-origin",
    }
  ).then((r) => {
    if (r.status === 404) {
      return null;
    }
    if (!r.ok) {
      throw new Error(r.statusText);
    }
    return r[bodyType]();
  });
};

const readExperimentDataObject = async function (
  experimentId,
  name,
  dataType,
  { bodyType = "text" } = {}
) {
  if (dataType !== "input" && dataType !== "output") {
    throw new Error("dataType should be one of 'input' or 'output'");
  }
  const experiment = await loadExperiment(experimentId);
  const dataObjectsField =
    dataType === "input" ? "experimentInputs" : "experimentOutputs";
  const dataObject = experiment[dataObjectsField].find(
    (dataObj) => dataObj.name === name
  );
  if (dataObject.value && dataObject.type.isFileValueType) {
    const downloads = dataObject.value
      .split(",")
      .map((dp) => readDataProduct(dp, { bodyType }));
    if (downloads.length === 1) {
      return await downloads[0];
    } else {
      return await Promise.all(downloads);
    }
  }
  return null;
};

const readInputFile = async function (
  experimentId,
  inputName,
  { bodyType = "text" } = {}
) {
  return await readExperimentDataObject(experimentId, inputName, "input", {
    bodyType,
  });
};

const readOutputFile = async function (
  experimentId,
  outputName,
  { bodyType = "text" } = {}
) {
  return await readExperimentDataObject(experimentId, outputName, "output", {
    bodyType,
  });
};

export { createExperiment, readInputFile, readOutputFile, readDataProduct };

export default {
  createExperiment,
  readInputFile,
  readOutputFile,
  readDataProduct,
};
