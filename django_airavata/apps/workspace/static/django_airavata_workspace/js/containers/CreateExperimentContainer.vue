<template>
  <experiment-editor
    v-if="experiment"
    :experiment="experiment"
    :app-module="appModule"
    :app-interface="appInterface"
    @saved="handleSavedExperiment"
    @savedAndLaunched="handleSavedAndLaunchedExperiment"
  >
    <span slot="title">Create a New Experiment</span>
  </experiment-editor>
</template>

<script>
import { services } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";
import ExperimentEditor from "../components/experiment/ExperimentEditor.vue";
import urls from "../utils/urls";

import moment from "moment";

export default {
  name: "create-experiment-container",
  props: ["app-module-id", "user-input-values", "experiment-data-dir"],
  data() {
    return {
      experiment: null,
      appModule: null,
      appInterface: null
    };
  },
  components: {
    "experiment-editor": ExperimentEditor,
  },
  methods: {
    handleSavedExperiment: function () {
      // Redirect to experiment view
      urls.navigateToExperimentsList();
    },
    handleSavedAndLaunchedExperiment: function (experiment) {
      // Redirect to experiment view
      urls.navigateToViewExperiment(experiment, { launching: true });
    },
  },
  computed: {},
  mounted: function () {
    const loadAppModule = services.ApplicationModuleService.retrieve(
      { lookup: this.appModuleId },
      { ignoreErrors: true }
    );
    const loadAppInterface = services.ApplicationModuleService.getApplicationInterface(
      { lookup: this.appModuleId },
      { ignoreErrors: true }
    );
    Promise.all([loadAppModule, loadAppInterface])
      .then(([appModule, appInterface]) => {
        const experiment = appInterface.createExperiment();
        experiment.experimentName =
          appModule.appModuleName + " on " + moment().format("lll");
        this.appModule = appModule;
        this.appInterface = appInterface;
        if (this.userInputValues) {
          Object.keys(this.userInputValues).forEach((k) => {
            const experimentInput = experiment.experimentInputs.find(
              (inp) => inp.name === k
            );
            if (experimentInput) {
              experimentInput.value = this.userInputValues[k];
            }
          });
        }
        if (this.experimentDataDir) {
          experiment.userConfigurationData.experimentDataDir = this.experimentDataDir;
        }
        this.experiment = experiment;
      })
      .catch((error) => {
        notifications.NotificationList.addError(error);
      });
  },
};
</script>
<style>
/* style the containing div, in base.html template */
.main-content-wrapper {
  background-color: #ffffff;
}
</style>
