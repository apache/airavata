<template>
  <b-card header="Experiment Data Directory">
    <experiment-storage-path-viewer
      v-if="experimentStoragePath"
      :experiment-storage-path="experimentStoragePath"
      @directory-selected="directorySelected"
      :download-in-new-window="true"
    ></experiment-storage-path-viewer>
  </b-card>
</template>

<script>
import { services } from "django-airavata-api";
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
      return services.ExperimentStoragePathService.get({
        experimentId: this.experimentId,
        path,
      }).then((result) => (this.experimentStoragePath = result));
    },
    directorySelected(path) {
      return this.loadExperimentStoragePath(path);
    },
  },
};
</script>
