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
    <div ref="groupResourceProfileSelector">
      <!-- programmatically define slot for adpf-group-resource-profile-selector -->
    </div>
    <div ref="computeResourceSelector">
      <!-- programmatically define slot for adpf-experiment-compute-resource-selector -->
    </div>
    <div ref="queueSettingsEditor">
      <!-- programmatically define slot for adpf-queue-settings-editor -->
    </div>
    <div ref="experimentButtons">
      <!-- programmatically define slot for experiment-buttons as
          native slot (not Vue slots), see #mounted() -->
    </div>
  </form>
</template>

<script>
import Vue from "vue";
import store from "./store";
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
  store: store,
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
    this.$emit("loaded", this.experiment);
    // vue-web-component-wrapper clones native slots and turns them into Vue
    // slots which means they lose any event listeners and they basically aren't
    // in the DOM any more.  As a workaround, programmatically create native
    // slots. See also https://github.com/vuejs/vue-web-component-wrapper/issues/38
    this.$nextTick(() => {
      for (const input of this.experiment.experimentInputs) {
        const slot = document.createElement("slot");
        slot.setAttribute("name", input.name);
        if (["STRING", "INTEGER", "FLOAT"].includes(input.type.name)) {
          slot.textContent = `${input.name} `;
          const textInput = document.createElement("adpf-string-input-editor");
          textInput.setAttribute(
            "value",
            input.value !== null ? input.value : ""
          );
          textInput.setAttribute("name", input.name);
          slot.appendChild(textInput);
          this.$refs[input.name][0].append(slot);
        } else if (input.type.name === "URI") {
          slot.textContent = `${input.name} `;
          const fileInputEditor = document.createElement(
            "adpf-file-input-editor"
          );
          fileInputEditor.setAttribute(
            "value",
            input.value !== null ? input.value : ""
          );
          fileInputEditor.setAttribute("name", input.name);
          slot.appendChild(fileInputEditor);
          this.$refs[input.name][0].append(slot);
        } else if (input.type.name === "URI_COLLECTION") {
          slot.textContent = `${input.name} `;
          const multiFileInputEditor = document.createElement(
            "adpf-multi-file-input-editor"
          );
          multiFileInputEditor.setAttribute(
            "value",
            input.value !== null ? input.value : ""
          );
          multiFileInputEditor.setAttribute("name", input.name);
          slot.appendChild(multiFileInputEditor);
          this.$refs[input.name][0].append(slot);
        }
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

      const groupResourceProfileSelectorEl = document.createElement(
        "adpf-group-resource-profile-selector"
      );
      if (this.groupResourceProfileId) {
        groupResourceProfileSelectorEl.setAttribute(
          "value",
          this.groupResourceProfileId
        );
      }
      this.$refs.groupResourceProfileSelector.append(
        this.createSlot(
          "experiment-group-resource-profile",
          groupResourceProfileSelectorEl
        )
      );

      const computeResourceSelectorEl = document.createElement(
        "adpf-experiment-compute-resource-selector"
      );
      computeResourceSelectorEl.setAttribute(
        "application-module-id",
        this.applicationId
      );
      this.$refs.computeResourceSelector.append(
        this.createSlot(
          "experiment-compute-resource",
          computeResourceSelectorEl
        )
      );

      const queueSettingsEditorEl = document.createElement(
        "adpf-queue-settings-editor"
      );
      this.$refs.queueSettingsEditor.append(
        this.createSlot("experiment-queue-settings", queueSettingsEditorEl)
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
    ...mapGetters(["experiment", "groupResourceProfileId"]),
  },
  methods: {
    updateExperimentName(event) {
      this.$store.dispatch("updateExperimentName", {
        name: event.target.value,
      });
    },
    updateInputValue(inputName, event) {
      // web component input events have the current value in a detail array,
      // native input events have the current value in target.value
      const value = Array.isArray(event.detail)
        ? event.detail[0]
        : event.target // Backwards compatibility: second argument changed from the value to the 'event'
        ? event.target.value
        : event;
      this.$store.dispatch("updateExperimentInputValue", { inputName, value });
    },
    updateProjectId(event) {
      const [projectId] = event.detail;
      this.$store.dispatch("updateProjectId", { projectId });
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

<style lang="scss">
@import "./styles";

:host {
  display: block;
  margin-bottom: 1em;
}
</style>
