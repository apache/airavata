<template>
  <experiment-editor
    v-if="appModule"
    :experiment="experiment"
    :app-module="appModule"
    @saved="handleSavedExperiment"
    @savedAndLaunched="handleSavedAndLaunchedExperiment"
  >
    <span slot="title">Edit Experiment</span>
  </experiment-editor>
</template>

<script>
import { models, services } from "django-airavata-api";
import ExperimentEditor from "../components/experiment/ExperimentEditor.vue";

import moment from "moment";

export default {
  name: "edit-experiment-container",
  props: {
    experimentId: {
      type: String,
      required: true
    }
  },
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
    handleSavedExperiment: function(experiment) {
      // Redirect to experiment view
      window.location.assign(
        "/workspace/experiments/" +
          encodeURIComponent(experiment.experimentId) +
          "/"
      );
    },
    handleSavedAndLaunchedExperiment: function(experiment) {
      // Redirect to experiment view
      window.location.assign(
        "/workspace/experiments/" +
          encodeURIComponent(experiment.experimentId) +
          "/?launching=true"
      );
    }
  },
  computed: {},
  mounted: function() {
    services.ExperimentService.retrieve({ lookup: this.experimentId })
      .then(experiment => {
        this.experiment = experiment;
        const appInterfaceId = experiment.executionId;
        return services.ApplicationInterfaceService.retrieve({
          lookup: appInterfaceId
        });
      })
      .then(appInterface => {
        const appModuleId = appInterface.applicationModules[0];
        return services.ApplicationModuleService.retrieve({
          lookup: appModuleId
        });
      })
      .then(appModule => {
        this.appModule = appModule;
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
