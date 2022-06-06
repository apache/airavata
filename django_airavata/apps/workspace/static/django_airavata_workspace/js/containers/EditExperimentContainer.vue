<template>
  <experiment-editor
    v-if="appModule"
    :experiment="experiment"
    :app-module="appModule"
    :app-interface="appInterface"
    @saved="handleSavedExperiment"
    @savedAndLaunched="handleSavedAndLaunchedExperiment"
  >
    <span slot="title">Edit Experiment</span>
  </experiment-editor>
</template>

<script>
import { errors, services } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";
import ExperimentEditor from "../components/experiment/ExperimentEditor.vue";
import urls from "../utils/urls";

export default {
  name: "edit-experiment-container",
  props: {
    experimentId: {
      type: String,
      required: true,
    },
  },
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
      // Redirect to experiments list view
      urls.navigateToExperimentsList();
    },
    handleSavedAndLaunchedExperiment: function (experiment) {
      // Redirect to experiment view
      urls.navigateToViewExperiment(experiment, { launching: true });
    },
  },
  computed: {},
  mounted: function () {
    services.ExperimentService.retrieve({ lookup: this.experimentId })
      .then((experiment) => {
        this.experiment = experiment;
        const appInterfaceId = experiment.executionId;
        return services.ApplicationInterfaceService.retrieve(
          {
            lookup: appInterfaceId,
          },
          {
            ignoreErrors: true,
          }
        );
      })
      .then((appInterface) => {
        this.appInterface = appInterface;
        const appModuleId = appInterface.applicationModules[0];
        return services.ApplicationModuleService.retrieve({
          lookup: appModuleId,
        });
      })
      .then((appModule) => {
        this.appModule = appModule;
      })
      .catch((error) => {
        const message = errors.ErrorUtils.isNotFoundError(error)
          ? `Application interface (${this.experiment.executionId}) was not found.
           If it has been deleted then you won't be able to edit this experiment.`
          : `Unable to load application interface (${this.experiment.executionId}) or module`;
        notifications.NotificationList.add(
          new notifications.Notification({
            type: "ERROR",
            message,
          })
        );
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
