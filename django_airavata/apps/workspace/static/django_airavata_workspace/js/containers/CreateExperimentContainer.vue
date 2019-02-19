<template>
  <experiment-editor
    v-if="experiment"
    :experiment="experiment"
    :app-module="appModule"
    @saved="handleSavedExperiment"
    @savedAndLaunched="handleSavedAndLaunchedExperiment"
  >
    <span slot="title">Create a New Experiment</span>
  </experiment-editor>
</template>

<script>
import { models, services } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";
import ExperimentEditor from "../components/experiment/ExperimentEditor.vue";
import urls from "../utils/urls";

import moment from "moment";

export default {
  name: "create-experiment-container",
  props: ["app-module-id"],
  data() {
    return {
      experiment: null,
      appModule: null
    };
  },
  components: {
    "experiment-editor": ExperimentEditor
  },
  methods: {
    handleSavedExperiment: function() {
      // Redirect to experiment view
      urls.navigateToExperimentsList();
    },
    handleSavedAndLaunchedExperiment: function(experiment) {
      // Redirect to experiment view
      urls.navigateToViewExperiment(experiment, {launching: true});
    }
  },
  computed: {},
  mounted: function() {
    const experiment = new models.Experiment();
    const loadAppModule = services.ApplicationModuleService.retrieve(
      { lookup: this.appModuleId },
      { ignoreErrors: true }
    ).then(appModule => {
      experiment.experimentName =
        appModule.appModuleName + " on " + moment().format("lll");
      this.appModule = appModule;
    });
    const loadAppInterface = services.ApplicationModuleService.getApplicationInterface(
      { lookup: this.appModuleId },
      { ignoreErrors: true }
    ).then(appInterface => {
      experiment.populateInputsOutputsFromApplicationInterface(appInterface);
      experiment.executionId = appInterface.applicationInterfaceId;
    });
    Promise.all([loadAppModule, loadAppInterface])
      .then(() => (this.experiment = experiment))
      .catch(error => {
        notifications.NotificationList.addError(error);
      });
  }
};
</script>
<style>
/* style the containing div, in base.html template */
.main-content {
  background-color: #ffffff;
}
</style>
