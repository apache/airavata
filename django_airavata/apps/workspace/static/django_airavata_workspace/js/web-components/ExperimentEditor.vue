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
      <div :key="input.name" @input="updateInputValue($event, input)">
        <slot :name="input.name">
          {{ input.name }}
          <input
            v-if="input.type.name == 'STRING'"
            :name="`input:${input.name}`"
            :value="input.value"
          />
        </slot>
        <!-- TODO: add support for other input types -->
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
  async created() {
    this.applicationModule = await getApplicationModule(this.applicationId);
    this.appInterface = await getApplicationInterfaceForModule(
      this.applicationId
    );
    this.experiment = await this.loadExperiment();
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
          "bigred3.uits.iu.edu_2141bf96-c458-4ecd-8759-aa3a08f31956";
        this.$emit("loaded", experiment);
        return experiment;
      }
    },
  },
};
</script>

<style></style>
