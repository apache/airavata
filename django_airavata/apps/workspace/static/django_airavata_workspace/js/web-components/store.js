import { services } from "django-airavata-api";
const APPLICATION_MODULES = {};
const APPLICATION_INTERFACES = {};
export async function getApplicationModule(applicationId) {
  if (applicationId in APPLICATION_MODULES) {
    return APPLICATION_MODULES[applicationId];
  }
  const result = await services.ApplicationModuleService.retrieve({
    lookup: applicationId,
  });
  APPLICATION_MODULES[applicationId] = result;
  return result;
}

export async function getApplicationInterfaceForModule(applicationId) {
  if (applicationId in APPLICATION_INTERFACES) {
    return APPLICATION_INTERFACES[applicationId];
  }
  const result = await services.ApplicationModuleService.getApplicationInterface(
    { lookup: applicationId }
  );
  APPLICATION_INTERFACES[applicationId] = result;
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

export async function getDefaultProjectId() {
  const preferences = await services.WorkspacePreferencesService.get();
  return preferences.most_recent_project_id;
}

export async function getExperiment(experimentId) {
  return await services.ExperimentService.retrieve({ lookup: experimentId });
}

export async function getProjects() {
  return await services.ProjectService.listAll();
}
