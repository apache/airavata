<template>
  <b-card>
    <div slot="header" class="d-flex align-items-baseline">
      <h6>{{ experimentOutput.name }}</h6>
      <b-dropdown v-if="showMenu" :text="currentView['name']" class="ml-auto">
        <b-dropdown-item
          v-for="(view, index) in outputViews"
          :key="view['provider-id']"
          :active="view['provider-id'] === currentView['provider-id']"
          @click="selectView(index)"
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
    <div
      slot="footer"
      v-if="dataProducts.length > 0 || isExecuting"
      class="d-flex justify-content-end align-items-baseline"
    >
      <template v-if="isExecuting">
        <span class="small text-muted mr-2">
          {{ fetchIntermediateOutputStatusMessage }}</span
        >
        <b-btn size="sm" @click="fetchLatest" :disabled="fetchLatestDisabled">
          <b-spinner
            small
            v-if="currentlyRunningIntermediateOutputFetch"
          ></b-spinner>
          Fetch Latest</b-btn
        >
      </template>
      <template v-else-if="dataProducts.length === 1">
        <b-btn size="sm" :href="dataProducts[0].downloadURL + '&download'"
          >Download</b-btn
        >
      </template>
    </div>
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
import { mapActions, mapGetters, mapState } from "vuex";
import ProcessState from "django-airavata-api/static/django_airavata_api/js/models/ProcessState";

export default {
  name: "output-viewer-container",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
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
    // Only show the default output view while executing or if no output dataProducts
    if (
      this.outputViews.length > 0 &&
      (!this.isFinished || this.dataProducts.length === 0)
    ) {
      this.currentViewIndex = this.outputViews.findIndex(
        (ov) => ov["provider-id"] === "default"
      );
    }
    if (this.providerId && this.providerId !== "default") {
      this.loader = this.createLoader();
      this.loader.load();
    }
  },
  data() {
    return {
      currentViewIndex: 0,
      loader: null,
    };
  },
  computed: {
    ...mapState("viewExperiment", ["fullExperiment"]),
    ...mapGetters("viewExperiment", [
      "outputDataProducts",
      "experimentId",
      "isExecuting",
      "isJobActive",
      "isFinished",
      "currentlyRunningIntermediateOutputFetches",
      "userHasWriteAccess",
    ]),
    outputViews() {
      return this.fullExperiment
        ? this.fullExperiment.outputViews[this.experimentOutput.name]
        : [];
    },
    dataProducts() {
      return this.outputDataProducts[this.experimentOutput.name];
    },
    currentView() {
      return this.outputViews.length > this.currentViewIndex
        ? this.outputViews[this.currentViewIndex]
        : null;
    },
    viewData() {
      return this.loader && this.loader.data
        ? this.loader.data
        : this.outputViewData;
    },
    outputViewData() {
      return this.currentView && this.currentView.data
        ? this.currentView.data
        : {};
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
      return this.currentView ? this.currentView["display-type"] : null;
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
      return (
        this.isFinished &&
        this.outputViews.length > 1 &&
        this.dataProducts.length > 0
      );
    },
    providerId() {
      return this.currentView ? this.currentView["provider-id"] : null;
    },
    hasInteractiveParameters() {
      return this.viewData && this.viewData.interactive;
    },
    currentlyRunningIntermediateOutputFetch() {
      return this.currentlyRunningIntermediateOutputFetches[
        this.experimentOutput.name
      ];
    },
    canFetchIntermediateOutput() {
      return this.isJobActive && !this.currentlyRunningIntermediateOutputFetch;
    },
    fetchLatestDisabled() {
      return !this.canFetchIntermediateOutput || !this.userHasWriteAccess;
    },
    fetchIntermediateOutputStatusMessage() {
      let msg = "";
      if (
        this.experimentOutput.intermediateOutput &&
        this.experimentOutput.intermediateOutput.processStatus &&
        this.experimentOutput.intermediateOutput.processStatus.isFinished
      ) {
        const timestamp = this.experimentOutput.intermediateOutput.processStatus
          .timeOfStateChange;
        msg +=
          "Latest output fetched on " +
          timestamp.toLocaleString([], {
            dateStyle: "medium",
            timeStyle: "short",
          }) +
          ". ";
      }
      if (
        this.experimentOutput.intermediateOutput &&
        this.experimentOutput.intermediateOutput.processStatus
      ) {
        if (
          this.experimentOutput.intermediateOutput.processStatus.state ===
          ProcessState.FAILED
        ) {
          msg += "Last fetch failed, please try again.";
        }
      }
      return msg;
    },
  },
  methods: {
    ...mapActions("viewExperiment", ["submitFetchIntermediateOutputs"]),
    selectView(outputViewIndex) {
      this.currentViewIndex = outputViewIndex;
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
    fetchLatest() {
      this.submitFetchIntermediateOutputs({
        outputNames: [this.experimentOutput.name],
      });
    },
  },
};
</script>
