import { services } from "django-airavata-api";
const CACHE = {
  APPLICATION_MODULES: {},
  APPLICATION_INTERFACES: {},
  WORKSPACE_PREFERENCES: null,
};
export async function getApplicationModule(applicationId) {
  if (applicationId in CACHE.APPLICATION_MODULES) {
    return CACHE.APPLICATION_MODULES[applicationId];
  }
  const result = await services.ApplicationModuleService.retrieve({
    lookup: applicationId,
  });
  CACHE.APPLICATION_MODULES[applicationId] = result;
  return result;
}

export async function getApplicationInterfaceForModule(applicationId) {
  if (applicationId in CACHE.APPLICATION_INTERFACES) {
    return CACHE.APPLICATION_INTERFACES[applicationId];
  }
  const result = await services.ApplicationModuleService.getApplicationInterface(
    { lookup: applicationId }
  );
  CACHE.APPLICATION_INTERFACES[applicationId] = result;
  return result;
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
    CACHE.WORKSPACE_PREFERENCES = await services.WorkspacePreferencesService.get();
  }
  return CACHE.WORKSPACE_PREFERENCES;
}

export async function getDefaultProjectId() {
  const prefs = await getWorkspacePreferences();
  return prefs.most_recent_project_id;
}

export async function getDefaultGroupResourceProfileId() {
  const prefs = await getWorkspacePreferences();
  return prefs.most_recent_group_resource_profile_id;
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
