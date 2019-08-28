<template>
  <b-card>
    <div
      slot="header"
      class="d-flex align-items-baseline"
    >
      <h6>{{ experimentOutput.name }}</h6>
      <b-dropdown
        v-if="showMenu"
        :text="currentView['name']"
        class="ml-auto"
      >
        <b-dropdown-item
          v-for="view in outputViews"
          :key="view['provider-id']"
          :active="view['provider-id'] === currentView['provider-id']"
          @click="selectView(view)"
        >{{ view['name']}}</b-dropdown-item>
      </b-dropdown>
    </div>
    <component
      :is="outputDisplayComponentName"
      :experiment-output="experimentOutput"
      :data-products="dataProducts"
      :experiment-id="experimentId"
      :provider-id="currentView['provider-id']"
      :data="outputViewData"
    />
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import DefaultOutputDisplay from "./DefaultOutputDisplay";
import HtmlOutputDisplay from "./HtmlOutputDisplay";
import ImageOutputDisplay from "./ImageOutputDisplay";
import LinkDisplay from "./LinkDisplay";
import NotebookOutputDisplay from "./NotebookOutputDisplay";

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
    },
    experimentId: {
      type: String,
      required: true
    }
  },
  components: {
    "data-product-viewer": components.DataProductViewer,
    DefaultOutputDisplay,
    HtmlOutputDisplay,
    ImageOutputDisplay,
    LinkDisplay,
    NotebookOutputDisplay
  },
  data() {
    return {
      currentView: this.outputViews[0]
    };
  },
  computed: {
    outputViewData() {
      return this.currentView.data ? this.currentView.data : {};
    },
    outputDisplayComponentName() {
      if (this.currentView["display-type"] === "default") {
        return "default-output-display";
      } else if (this.currentView["display-type"] === "link") {
        return "link-display";
      } else if (this.currentView["display-type"] === "notebook") {
        return "notebook-output-display";
      } else if (this.currentView["display-type"] === "html") {
        return "html-output-display";
      } else if (this.currentView["display-type"] === "image") {
        return "image-output-display";
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

