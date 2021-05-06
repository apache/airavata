<template>
  <b-card header="Experiment Data Directory">
    <experiment-storage-path-viewer
      v-if="experimentStoragePath"
      :experiment-storage-path="experimentStoragePath"
      @directory-selected="directorySelected"
      :download-in-new-window="true"
    ></experiment-storage-path-viewer>
    <b-alert v-else-if="experimentDataDirNotFound" show variant="warning">
      Experiment Data Directory does not exist in storage.
    </b-alert>
  </b-card>
</template>

<script>
import { errors, services, utils } from "django-airavata-api";
import ExperimentStoragePathViewer from "./ExperimentStoragePathViewer.vue";

export default {
  name: "experiment-storage-view-container",
  computed: {},
  props: {
    experimentId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      experimentStoragePath: null,
      experimentDataDirNotFound: false,
    };
  },
  components: {
    ExperimentStoragePathViewer,
  },
  created() {
    return this.loadExperimentStoragePath("");
  },
  methods: {
    loadExperimentStoragePath(path) {
      return services.ExperimentStoragePathService.get(
        {
          experimentId: this.experimentId,
          path,
        },
        { ignoreErrors: true }
      )
        .then((result) => (this.experimentStoragePath = result))
        .catch((error) => {
          if (
            errors.ErrorUtils.isAPIException(error) &&
            error.details.status === 404
          ) {
            this.experimentDataDirNotFound = true;
          } else {
            throw error;
          }
        })
        .catch(utils.FetchUtils.reportError);
    },
    directorySelected(path) {
      return this.loadExperimentStoragePath(path);
    },
  },
};
</script>
