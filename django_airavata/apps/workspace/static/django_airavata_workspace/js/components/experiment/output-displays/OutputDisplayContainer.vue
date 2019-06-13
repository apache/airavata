<template>
  <b-card>
    <div slot="header" class="d-flex align-items-baseline">
      <h6>{{ experimentOutput.name }}</h6>
      <b-dropdown v-if="showMenu" :text="currentView['provider-id']" class="ml-auto">
        <!-- TODO: add view label to data so that that can be used instead of id -->
        <b-dropdown-item
          v-for="view in outputViews"
          :key="view['provider-id']"
          @click="selectView(view)"
        >{{ view['provider-id']}}</b-dropdown-item>
      </b-dropdown>
    </div>
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
  data() {
    return {
      currentView: this.outputViews[0]
    };
  },
  computed: {
    // TODO: support multiple output views
    outputViewData() {
      return this.currentView.data ? this.currentView.data : {};
    },
    outputDisplayComponentName() {
      // TODO: maybe rename download to default?
      if (this.currentView["display-type"] === "download") {
        return "download-output-display";
      } else if (this.currentView["display-type"] === "link") {
        return "link-display";
      } else {
        return null;
      }
    },
    showMenu() {
      return this.outputViews.length > 1;
    }
  },
  methods: {
    selectView(outputView) {
      this.currentView = outputView;
    }
  }
};
</script>

