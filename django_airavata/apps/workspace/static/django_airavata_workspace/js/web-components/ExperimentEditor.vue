<template>
  <form v-if="experiment" @submit.prevent="onSubmit">
    <div ref="experimentName" @input="updateExperimentName">
      <!-- programmatically define slot for experiment-name as native slot
            (not Vue slots), see #mounted() -->
    </div>
    <div ref="projectSelector" @input="updateProjectId">
      <!-- programmatically define slot for experiment-project as native slot
           (not Vue slots), see #mounted() -->
    </div>
    <template v-for="input in experiment.experimentInputs">
      <div
        :ref="input.name"
        :key="input.name"
        @input="updateInputValue(input.name, $event)"
      >
        <!-- programmatically define slots as native slots (not Vue slots), see #mounted() -->
      </div>
    </template>
    <!-- TODO: programmatically define slot for adpf-group-resource-profile-selector -->
    <div @input.stop="updateGroupResourceProfileId">
      <adpf-group-resource-profile-selector
        :value="experiment.userConfigurationData.groupResourceProfileId"
      />
    </div>
    <!-- TODO: programmatically define slot for adpf-experiment-compute-resource-selector -->
    <div @input.stop="updateComputeResourceHostId">
      <adpf-experiment-compute-resource-selector
        ref="computeResourceSelector"
        :value="
          experiment.userConfigurationData.computationalResourceScheduling
            .resourceHostId
        "
      />
    </div>
    <!-- TODO: programmatically define slot for adpf-queue-settings-editor -->
    <div
      @queue-name-changed="updateQueueName"
      @node-count-changed="updateNodeCount"
      @total-cpu-count-change="updateTotalCPUCount"
      @walltime-limit-changed="updateWallTimeLimit"
      @total-physical-memory-changed="updateTotalPhysicalMemory"
    >
      <adpf-queue-settings-editor
        ref="queueSettingsEditor"
        :queue-name="
          experiment.userConfigurationData.computationalResourceScheduling
            .queueName
        "
        :total-cpu-count="
          experiment.userConfigurationData.computationalResourceScheduling
            .totalCPUCount
        "
        :node-count="
          experiment.userConfigurationData.computationalResourceScheduling
            .nodeCount
        "
        :wall-time-limit="
          experiment.userConfigurationData.computationalResourceScheduling
            .wallTimeLimit
        "
        :total-physical-memory="
          experiment.userConfigurationData.computationalResourceScheduling
            .totalPhysicalMemory
        "
      />

    </div>
    <div ref="experimentButtons">
      <!-- programmatically define slot for experiment-buttons as
          native slot (not Vue slots), see #mounted() -->
    </div>
  </form>
</template>

<script>
import Vue from "vue";
import vuestore from "./vuestore";
import { mapGetters } from "vuex";
import { BootstrapVue } from "bootstrap-vue";
import urls from "../utils/urls";
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
  store: vuestore,
  async created() {},
  async mounted() {
    if (this.experimentId) {
      await this.$store.dispatch("loadExperiment", {
        experimentId: this.experimentId,
      });
    } else {
      await this.$store.dispatch("loadNewExperiment", {
        applicationId: this.applicationId,
      });
    }
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
          const textInput = document.createElement("adpf-string-input-editor");
          textInput.setAttribute("value", input.value);
          textInput.setAttribute("name", input.name);
          slot.appendChild(textInput);
          this.$refs[input.name][0].append(slot);
        }
        // TODO: add support for other input types
      }
      // this.injectPropsIntoSlottedInputs();

      /*
       * Experiment Name native slot
       */
      // <slot name="experiment-name">
      //   <b-form-group label="Experiment Name" label-for="experiment-name">
      //     <b-form-input
      //       type="text"
      //       name="experiment-name"
      //       :value="experiment.experimentName"
      //       required
      //     >
      //     </b-form-input>
      //   </b-form-group>
      // </slot>
      const experimentNameGroupEl = document.createElement("div");
      experimentNameGroupEl.classList.add("form-group");
      const experimentNameLabelEl = document.createElement("label");
      experimentNameLabelEl.setAttribute("for", "experiment-name-input");
      experimentNameLabelEl.textContent = "Experiment Name";
      const experimentNameInputEl = document.createElement("input");
      experimentNameInputEl.classList.add("form-control");
      experimentNameInputEl.setAttribute("id", "experiment-name-input");
      experimentNameInputEl.setAttribute("type", "text");
      experimentNameInputEl.setAttribute("name", "experiment-name");
      experimentNameInputEl.setAttribute(
        "value",
        this.experiment.experimentName
      );
      experimentNameInputEl.setAttribute("required", "required");
      experimentNameGroupEl.append(
        experimentNameLabelEl,
        experimentNameInputEl
      );
      this.$refs.experimentName.append(
        this.createSlot("experiment-name", experimentNameGroupEl)
      );

      const projectSelectorEl = document.createElement("adpf-project-selector");
      if (this.experiment.projectId) {
        projectSelectorEl.setAttribute("value", this.experiment.projectId);
      }
      this.$refs.projectSelector.append(
        this.createSlot("experiment-project", projectSelectorEl)
      );

      /*
       * Experiment (save/launch) Buttons native slot
       */
      // <slot name="experiment-buttons">
      //   <div class="d-flex justify-content-end">
      //     <b-button
      //       type="submit"
      //       variant="success"
      //       name="save-and-launch-experiment-button"
      //       class="mr-2"
      //     >
      //       Save and Launch
      //     </b-button>
      //     <b-button type="submit" variant="primary" name="save-experiment-button">
      //       Save
      //     </b-button>
      //   </div>
      // </slot>
      const buttonsRowEl = document.createElement("div");
      buttonsRowEl.classList.add("d-flex", "justify-content-end");
      const saveAndLaunchButtonEl = document.createElement("button");
      saveAndLaunchButtonEl.setAttribute("type", "submit");
      saveAndLaunchButtonEl.setAttribute(
        "name",
        "save-and-launch-experiment-button"
      );
      saveAndLaunchButtonEl.classList.add("btn", "btn-success", "mr-2");
      saveAndLaunchButtonEl.textContent = "Save and Launch";
      const saveButtonEl = document.createElement("button");
      saveButtonEl.setAttribute("type", "submit");
      saveButtonEl.setAttribute("name", "save-experiment-button");
      saveButtonEl.classList.add("btn", "btn-primary");
      saveButtonEl.textContent = "Save";
      buttonsRowEl.append(saveAndLaunchButtonEl, saveButtonEl);
      this.$refs.experimentButtons.append(
        this.createSlot("experiment-buttons", buttonsRowEl)
      );
    });
  },
  computed: {
    ...mapGetters(["experiment"]),
  },
  methods: {
    updateExperimentName(event) {
      this.$store.dispatch("updateExperimentName", {
        name: event.target.value,
      });
    },
    updateInputValue(inputName, event) {
      if (event.inputType) {
        // Ignore these fine-grained events about the type of change made
        return;
      }
      // web component input events have the current value in a detail array,
      // native input events have the current value in target.value
      const value = Array.isArray(event.detail)
        ? event.detail[0]
        : event.target.value;
      this.$store.dispatch("updateExperimentInputValue", { inputName, value });
    },
    updateProjectId(event) {
      const [projectId] = event.detail;
      this.$store.dispatch("updateProjectId", { projectId });
    },
    updateGroupResourceProfileId(event) {
      const [groupResourceProfileId] = event.detail;
      this.$store.dispatch("updateGroupResourceProfileId", {
        groupResourceProfileId,
      });
    },
    updateComputeResourceHostId(event) {
      const [resourceHostId] = event.detail;
      this.$store.dispatch("updateComputeResourceHostId", {
        resourceHostId,
      });
    },
    updateQueueName(event) {
      const [queueName] = event.detail;
      this.$store.dispatch("updateQueueName", { queueName });
    },
    updateTotalCPUCount(event) {
      const [totalCPUCount] = event.detail;
      this.$store.dispatch("updateTotalCPUCount", { totalCPUCount });
    },
    updateNodeCount(event) {
      const [nodeCount] = event.detail;
      this.$store.dispatch("updateNodeCount", { nodeCount });
    },
    updateWallTimeLimit(event) {
      const [wallTimeLimit] = event.detail;
      this.$store.dispatch("updateWallTimeLimit", { wallTimeLimit });
    },
    updateTotalPhysicalMemory(event) {
      const [totalPhysicalMemory] = event.detail;
      this.$store.dispatch("updateTotalPhysicalMemory", {
        totalPhysicalMemory,
      });
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
        await this.$store.dispatch("saveExperiment");
        this.postSave();
        return;
      } else {
        // Default submit button handling is save and launch
        await this.$store.dispatch("saveExperiment");
        await this.$store.dispatch("launchExperiment");
        this.postSaveAndLaunch(this.experiment);
        return;
      }
    },
    postSave() {
      // client code can listen for 'saved' and preventDefault() on it to handle
      // it differently. Default action is to navigate to experiments list.
      const savedEvent = new CustomEvent("saved", {
        detail: [this.experiment],
        cancelable: true,
        composed: true,
      });
      this.$el.dispatchEvent(savedEvent);
      if (savedEvent.defaultPrevented) {
        return;
      }
      urls.navigateToExperimentsList();
    },
    postSaveAndLaunch(experiment) {
      // client code can listen for 'saved-and-launched' and preventDefault() on
      // it to handle it differently. Default action is to navigate to
      // the experiment summary page.
      const savedAndLaunchedEvent = new CustomEvent("saved-and-launched", {
        detail: [this.experiment],
        cancelable: true,
        composed: true,
      });
      this.$el.dispatchEvent(savedAndLaunchedEvent);
      if (savedAndLaunchedEvent.defaultPrevented) {
        return;
      }
      urls.navigateToViewExperiment(experiment, { launching: true });
    },
    createSlot(name, ...children) {
      const slot = document.createElement("slot");
      slot.setAttribute("name", name);
      slot.append(...children);
      return slot;
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
