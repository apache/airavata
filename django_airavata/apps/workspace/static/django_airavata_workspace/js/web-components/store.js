import { errors, services, utils } from "django-airavata-api";
const CACHE = {
  APPLICATION_MODULES: {},
  APPLICATION_INTERFACES: {},
  WORKSPACE_PREFERENCES: null,
};
export async function getApplicationModule(applicationId) {
  if (!(applicationId in CACHE.APPLICATION_MODULES)) {
    const promise = services.ApplicationModuleService.retrieve({
      lookup: applicationId,
    });
    CACHE.APPLICATION_MODULES[applicationId] = promise;
  }
  return await CACHE.APPLICATION_MODULES[applicationId];
}

export async function getApplicationInterfaceForModule(applicationId) {
  if (!(applicationId in CACHE.APPLICATION_INTERFACES)) {
    const promise = services.ApplicationModuleService.getApplicationInterface({
      lookup: applicationId,
    });
    CACHE.APPLICATION_INTERFACES[applicationId] = promise;
  }
  return await CACHE.APPLICATION_INTERFACES[applicationId];
}

export async function saveExperiment(experiment) {
  if (experiment.experimentId) {
    return await services.ExperimentService.update({
      data: experiment,
      lookup: experiment.experimentId,
    });
  } else {
    return await services.ExperimentService.create({ data: experiment });
  }
}

export async function getWorkspacePreferences() {
  if (!CACHE.WORKSPACE_PREFERENCES) {
    CACHE.WORKSPACE_PREFERENCES = services.WorkspacePreferencesService.get();
  }
  return await CACHE.WORKSPACE_PREFERENCES;
}

export async function getDefaultProjectId() {
  const prefs = await getWorkspacePreferences();
  return prefs.most_recent_project_id;
}

export async function getDefaultGroupResourceProfileId() {
  const prefs = await getWorkspacePreferences();
  return prefs.most_recent_group_resource_profile_id;
}

export async function getDefaultComputeResourceId() {
  const prefs = await getWorkspacePreferences();
  return prefs.most_recent_compute_resource_id;
}

export async function getExperiment(experimentId) {
  return await services.ExperimentService.retrieve({ lookup: experimentId });
}

export async function getProjects() {
  return await services.ProjectService.listAll();
}

export async function getGroupResourceProfiles() {
  return await services.GroupResourceProfileService.list();
}

export async function getApplicationDeployments(
  applicationId,
  groupResourceProfileId
) {
  return await services.ApplicationDeploymentService.list(
    {
      appModuleId: applicationId,
      groupResourceProfileId: groupResourceProfileId,
    },
    { ignoreErrors: true }
  )
    .catch((error) => {
      // Ignore unauthorized errors, force user to pick another GroupResourceProfile
      if (!errors.ErrorUtils.isUnauthorizedError(error)) {
        return Promise.reject(error);
      }
    })
    // Report all other error types
    .catch(utils.FetchUtils.reportError);
}

export async function getComputeResourceNames() {
  // TODO: cache these
  return await services.ComputeResourceService.names();
}
