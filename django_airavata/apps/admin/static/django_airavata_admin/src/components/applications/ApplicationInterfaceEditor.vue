<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          Application Interface
        </h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-form-group label="Enable Archiving Working Directory" label-for="archive-directory">
          <b-form-radio-group id="archive-directory" v-model="data.archiveWorkingDirectory" :options="trueFalseOptions" :disabled="readonly">
          </b-form-radio-group>
        </b-form-group>
      </div>
      <div class="col">
        <b-form-group label="Enable Optional File Inputs" label-for="optional-file-inputs">
          <b-form-radio-group id="optional-file-inputs" v-model="data.hasOptionalFileInputs" :options="trueFalseOptions" :disabled="true">
          </b-form-radio-group>
          <div slot="description"><b>Removed</b>: please add an input of Type URI_COLLECTION with Required set to False instead.</div>
        </b-form-group>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <h1 class="h5 mb-4">
          Input Fields
        </h1>
        <draggable v-model="data.applicationInputs" :options="dragOptions" @start="onDragStart" @end="onDragEnd">
          <application-input-field-editor v-for="input in data.applicationInputs" :value="input" :key="input.key" :focus="input.key === focusApplicationInputKey"
            :collapse="collapseApplicationInputs" @input="updatedInput" @delete="deleteInput(input)" :readonly="readonly" />
        </draggable>
      </div>
    </div>
    <div class="row mb-4">
      <div class="col">
        <b-button variant="secondary" @click="addApplicationInput" :disabled="readonly">
          Add application input
        </b-button>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <h1 class="h5 mb-4">
          Output Fields
        </h1>
        <application-output-field-editor v-for="output in data.applicationOutputs" :value="output" :key="output.key" :focus="output.key === focusApplicationOutputKey"
          @input="updatedOutput" @delete="deleteOutput(output)" :readonly="readonly" />
      </div>
    </div>
    <div class="row mb-4">
      <div class="col">
        <b-button variant="secondary" @click="addApplicationOutput" :disabled="readonly">
          Add application output
        </b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui"
import ApplicationInputFieldEditor from "./ApplicationInputFieldEditor.vue";
import ApplicationOutputFieldEditor from "./ApplicationOutputFieldEditor.vue";

import draggable from "vuedraggable";

export default {
  name: "application-interface-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.ApplicationInterfaceDefinition
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  components: {
    ApplicationInputFieldEditor,
    ApplicationOutputFieldEditor,
    draggable
  },
  computed: {
    trueFalseOptions() {
      return [{ text: "True", value: true }, { text: "False", value: false }];
    }
  },
  data() {
    return {
      focusApplicationInputKey: null,
      focusApplicationOutputKey: null,
      dragOptions: {
        handle: ".drag-handle"
      },
      collapseApplicationInputs: false
    };
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    updatedInput(newValue) {
      const input = this.data.applicationInputs.find(
        input => input.key === newValue.key
      );
      Object.assign(input, newValue);
    },
    addApplicationInput() {
      const appInput = new models.InputDataObjectType();
      this.data.applicationInputs.push(appInput);
      this.focusApplicationInputKey = appInput.key;
    },
    deleteInput(input) {
      const inputIndex = this.data.applicationInputs.findIndex(
        inp => inp.key === input.key
      );
      this.data.applicationInputs.splice(inputIndex, 1);
    },
    updatedOutput(newValue) {
      const output = this.data.applicationOutputs.find(
        o => o.key === newValue.key
      );
      Object.assign(output, newValue);
    },
    addApplicationOutput() {
      const newOutput = new models.OutputDataObjectType();
      this.data.applicationOutputs.push(newOutput);
      this.focusApplicationOutputKey = newOutput.key;
    },
    deleteOutput(output) {
      const outputIndex = this.data.applicationOutputs.findIndex(
        o => o.key === output.key
      );
      this.data.applicationOutputs.splice(outputIndex, 1);
    },
    onDragStart() {
      this.collapseApplicationInputs = true;
    },
    onDragEnd() {
      this.collapseApplicationInputs = false;
    }
  }
};
</script>
