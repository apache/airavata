export default {
  editExperiment(experiment) {
    return (
      "/workspace/experiments/" +
      encodeURIComponent(experiment.experimentId) +
      "/edit"
    );
  },
  navigateToEditExperiment(experiment) {
    window.location.assign(this.editExperiment(experiment));
  },
  experimentsList() {
    return "/workspace/experiments";
  },
  navigateToExperimentsList() {
    window.location.assign(this.experimentsList());
  },
  viewExperiment(experiment, { launching = false } = {}) {
    return (
      "/workspace/experiments/" +
      encodeURIComponent(experiment.experimentId) +
      "/" +
      (launching ? "?launching=true" : "")
    );
  },
  navigateToViewExperiment(experiment, { launching = false } = {}) {
    window.location.assign(
      this.viewExperiment(experiment, { launching: launching })
    );
  },
  createExperiment(appModule) {
    return (
      "/workspace/applications/" +
      encodeURIComponent(appModule.appModuleId) +
      "/create_experiment"
    );
  },
  navigateToCreateExperiment(appModule) {
    window.location.assign(this.createExperiment(appModule));
  },
  editProject(project) {
    return "/workspace/projects/" + encodeURIComponent(project.projectID) + "/";
  },
  projectsList() {
    return "/workspace/projects";
  },
  navigateToProjectsList() {
    window.location.assign(this.projectsList());
  },
};
