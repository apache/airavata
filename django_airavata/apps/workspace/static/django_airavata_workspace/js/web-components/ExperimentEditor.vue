<template>
  <form v-if="experiment" @submit.prevent="onSubmit">
    <div @input="updateExperimentName">
      <slot name="experiment-name">
        <b-form-group label="Experiment Name" label-for="experiment-name">
          <b-form-input
            type="text"
            name="experiment-name"
            :value="experiment.experimentName"
            required
          >
          </b-form-input>
        </b-form-group>
      </slot>
    </div>
    <div @input="updateProjectId">
      <!-- TODO: define this as a native slot? -->
      <slot name="experiment-project">
        <adpf-project-selector :value="experiment.projectId" />
      </slot>
    </div>
    <template v-for="input in experiment.experimentInputs">
      <div
        :ref="input.name"
        :key="input.name"
        @input="updateInputValue(input.name, $event.target.value)"
      >
        <!-- programmatically define slots as native slots (not Vue slots), see #mounted() -->
      </div>
    </template>
    <div @input="updateUserConfigurationData">
      <slot name="experiment-resource-selection">
        <adpf-resource-selection-editor ref="resourceSelectionEditor" />
      </slot>
    </div>
    <slot name="save-button">
      <div class="d-flex justify-content-end">
        <b-button
          type="submit"
          variant="success"
          name="save-and-launch-experiment-button"
          class="mr-2"
        >
          Save and Launch
        </b-button>
        <b-button type="submit" variant="primary" name="save-experiment-button">
          Save
        </b-button>
      </div>
    </slot>
  </form>
</template>

<script>
import {
  getApplicationModule,
  getApplicationInterfaceForModule,
  saveExperiment,
  getExperiment,
  launchExperiment,
} from "./store";

import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

export default {
  props: {
    // TODO: rename to applicationModuleId?
    applicationId: {
      type: String,
      required: true,
    },
    experimentId: {
      type: String,
      required: false,
    },
  },
  async created() {},
  async mounted() {
    this.applicationModule = await getApplicationModule(this.applicationId);
    this.appInterface = await getApplicationInterfaceForModule(
      this.applicationId
    );
    this.experiment = await this.loadExperiment();
    // vue-web-component-wrapper clones native slots and turns them into Vue
    // slots which means they lose any event listeners and they basically aren't
    // in the DOM any more.  As a workaround, programmatically create native
    // slots. See also https://github.com/vuejs/vue-web-component-wrapper/issues/38
    this.$nextTick(() => {
      for (const input of this.experiment.experimentInputs) {
        const slot = document.createElement("slot");
        slot.setAttribute("name", input.name);
        if (input.type.name === "STRING") {
          slot.textContent = `${input.name} `;
          const textInput = document.createElement("input");
          textInput.setAttribute("type", "text");
          textInput.setAttribute("value", input.value);
          slot.appendChild(textInput);
        }
        // TODO: add support for other input types
        this.$refs[input.name][0].append(slot);
      }
      // Can't set objects via attributes, must set as prop
      this.$refs.resourceSelectionEditor.value = this.experiment.userConfigurationData;
      this.$refs.resourceSelectionEditor.applicationModuleId = this.applicationId;
    });
  },
  data() {
    return {
      applicationModule: null,
      appInterface: null,
      experiment: null,
    };
  },
  methods: {
    updateExperimentName(event) {
      this.experiment.experimentName = event.target.value;
    },
    updateInputValue(inputName, value) {
      const experimentInput = this.experiment.experimentInputs.find(
        (i) => i.name === inputName
      );
      experimentInput.value = value;
    },
    updateProjectId(event) {
      const [projectId] = event.detail;
      this.experiment.projectId = projectId;
    },
    updateUserConfigurationData(event) {
      const [userConfigurationData] = event.detail;
      this.experiment.userConfigurationData = userConfigurationData;
    },
    async onSubmit(event) {
      // console.log(event);
      // 'save' event is cancelable. Listener can call .preventDefault() on the event to cancel.
      // composed: true allows the shadow DOM event to bubble up through the shadow root.
      const saveEvent = new CustomEvent("save", {
        detail: [this.experiment],
        cancelable: true,
        composed: true,
      });
      this.$el.dispatchEvent(saveEvent);
      if (saveEvent.defaultPrevented) {
        return;
      }
      if (event.submitter.name === "save-experiment-button") {
        this.saveExperiment();
      } else {
        // Default submit button handling is save and launch
        const experiment = await this.saveExperiment();
        await launchExperiment(experiment.experimentId);
      }
    },
    async saveExperiment() {
      return await saveExperiment(this.experiment);
    },
    async loadExperiment() {
      if (this.experimentId) {
        const experiment = await getExperiment(this.experimentId);
        this.$emit("loaded", experiment);
        return experiment;
      } else {
        const experiment = this.appInterface.createExperiment();
        experiment.experimentName =
          this.applicationModule.appModuleName +
          " on " +
          new Date().toLocaleString();
        this.$emit("loaded", experiment);
        return experiment;
      }
    },
  },
};
</script>

<style>
@import "./styles.css";

:host {
  display: block;
  margin-bottom: 1em;
}
</style>
