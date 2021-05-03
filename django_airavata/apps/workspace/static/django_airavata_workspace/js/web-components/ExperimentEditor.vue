<template>
  <form v-if="experiment" @submit.prevent="onSubmit">
    <div @input="updateExperimentName">
      <slot name="experiment-name">
        <input
          type="text"
          name="experiment-name"
          :value="experiment.experimentName"
        />
      </slot>
    </div>
    <template v-for="input in experiment.experimentInputs">
      <div
        :ref="input.name"
        :key="input.name"
        @input="updateInputValue($event, input)"
      >
      <!-- programmatically define slots as native slots (not Vue slots), see #mounted() -->
      </div>
    </template>
    <slot name="save-button">
      <button type="submit" name="save-experiment-button">Save</button>
    </slot>
  </form>
</template>

<script>
import {
  getApplicationModule,
  getApplicationInterfaceForModule,
  saveExperiment,
  getDefaultProjectId,
  getExperiment,
} from "./store";

export default {
  props: {
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
    updateInputValue(event, experimentInput) {
      experimentInput.value = event.target.value;
    },
    onSubmit(event) {
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
        const defaultProjectId = await getDefaultProjectId();
        experiment.projectId = defaultProjectId;
        experiment.userConfigurationData.computationalResourceScheduling.resourceHostId =
          "js-169-51.jetstream-cloud.org_6672e8fe-8d63-4bbe-8bf8-4ea04092e72f";
        this.$emit("loaded", experiment);
        return experiment;
      }
    },
  },
};
</script>

<style></style>
