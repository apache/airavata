<template>
  <!-- TODO: Add menu when there are more than one outputViews -->
  <b-card :title="experimentOutput.name">
    <component
      :is="outputDisplayComponentName"
      :experiment-output="experimentOutput"
      :data-products="dataProducts"
      :data="outputViewData"
    />
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import DownloadOutputDisplay from "./DownloadOutputDisplay";
import LinkDisplay from "./LinkDisplay";
import DataProductViewer from "../DataProductViewer";

export default {
  name: "output-viewer-container",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true
    },
    outputViews: {
      type: Array,
      required: true
    },
    dataProducts: {
      type: Array,
      required: false,
      default: null
    }
  },
  components: {
    DataProductViewer,
    DownloadOutputDisplay,
    LinkDisplay
  },
  computed: {
    // TODO: support multiple output views
    outputViewData() {
      return this.outputView ? this.outputView["data"] : null;
    },
    outputDisplayComponentName() {
      if (this.outputView) {
        if (this.outputView["display-type"] === "download") {
          return "download-output-display";
        } else if (this.outputView["display-type"] === "link") {
          return "link-display";
        } else {
          return null;
        }
      } else {
        return null;
      }
    },
    outputView() {
      if (this.outputViews && this.outputViews.length > 0) {
        return this.outputViews[0];
      } else {
        return null;
      }
    }
  }
};
</script>

