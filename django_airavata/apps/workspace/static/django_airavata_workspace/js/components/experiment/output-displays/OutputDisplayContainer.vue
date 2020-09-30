<template>
  <b-card>
    <div slot="header" class="d-flex align-items-baseline">
      <h6>{{ experimentOutput.name }}</h6>
      <b-dropdown v-if="showMenu" :text="currentView['name']" class="ml-auto">
        <b-dropdown-item
          v-for="view in outputViews"
          :key="view['provider-id']"
          :active="view['provider-id'] === currentView['provider-id']"
          @click="selectView(view)"
          >{{ view["name"] }}</b-dropdown-item
        >
      </b-dropdown>
    </div>
    <component
      :is="outputDisplayComponentName"
      :view-data="viewData"
      :data-products="dataProducts"
      :experiment-output="experimentOutput"
    />
    <interactive-parameters-panel
      ref="interactiveParametersPanel"
      v-if="viewData && viewData.interactive"
      :parameters="viewData.interactive"
      @input="parametersUpdated"
    />
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import DefaultOutputDisplay from "./DefaultOutputDisplay";
import HtmlOutputDisplay from "./HtmlOutputDisplay";
import ImageOutputDisplay from "./ImageOutputDisplay";
import LinkOutputDisplay from "./LinkOutputDisplay";
import NotebookOutputDisplay from "./NotebookOutputDisplay";
import InteractiveParametersPanel from "./interactive-parameters/InteractiveParametersPanel";
import OutputViewDataLoader from "./OutputViewDataLoader";

export default {
  name: "output-viewer-container",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true,
    },
    outputViews: {
      type: Array,
      required: true,
    },
    dataProducts: {
      type: Array,
      required: false,
      default: null,
    },
    experimentId: {
      type: String,
      required: true,
    },
  },
  components: {
    "data-product-viewer": components.DataProductViewer,
    DefaultOutputDisplay,
    HtmlOutputDisplay,
    ImageOutputDisplay,
    LinkOutputDisplay,
    NotebookOutputDisplay,
    InteractiveParametersPanel,
  },
  created() {
    if (this.providerId !== "default") {
      this.loader = this.createLoader();
      this.loader.load();
    }
  },
  data() {
    return {
      currentView: this.outputViews[0],
      loader: null,
    };
  },
  computed: {
    viewData() {
      return this.loader && this.loader.data
        ? this.loader.data
        : this.outputViewData;
    },
    outputViewData() {
      return this.currentView.data ? this.currentView.data : {};
    },
    displayTypeData() {
      return {
        default: {
          component: "default-output-display",
          url: null,
        },
        link: {
          component: "link-output-display",
          url: "/api/link-output/",
        },
        notebook: {
          component: "notebook-output-display",
          url: "/api/notebook-output/",
        },
        html: {
          component: "html-output-display",
          url: "/api/html-output/",
        },
        image: {
          component: "image-output-display",
          url: "/api/image-output/",
        },
      };
    },
    displayType() {
      return this.currentView["display-type"];
    },
    outputDisplayComponentName() {
      if (this.displayType in this.displayTypeData) {
        return this.displayTypeData[this.displayType].component;
      } else {
        return null;
      }
    },
    outputDataURL() {
      if (this.displayType in this.displayTypeData) {
        return this.displayTypeData[this.displayType].url;
      } else {
        return null;
      }
    },
    showMenu() {
      return this.outputViews.length > 1;
    },
    providerId() {
      return this.currentView["provider-id"];
    },
    hasInteractiveParameters() {
      return this.viewData && this.viewData.interactive;
    },
  },
  methods: {
    selectView(outputView) {
      this.currentView = outputView;
      if (this.outputDataURL === null) {
        this.loader = null;
      } else {
        this.loader = this.createLoader();
        this.loader.load();
      }
    },
    parametersUpdated(newParams) {
      if (
        this.hasInteractiveParameters &&
        !this.$refs.interactiveParametersPanel.valid
      ) {
        // Don't update if we have invalid interactive parameters
        return;
      }
      this.loader.load(newParams);
    },
    createLoader() {
      return new OutputViewDataLoader({
        url: this.outputDataURL,
        experimentId: this.experimentId,
        experimentOutputName: this.experimentOutput.name,
        providerId: this.providerId,
      });
    },
  },
};
</script>
