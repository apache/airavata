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
          <b-form-radio-group id="archive-directory" v-model="data.archiveWorkingDirectory" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
      </div>
      <div class="col">
        <b-form-group label="Enable Optional File Inputs" label-for="optional-file-inputs">
          <b-form-radio-group id="optional-file-inputs" v-model="data.hasOptionalFileInputs" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <h1 class="h5 mb-4">
          Input Fields
        </h1>
        <draggable v-model="data.applicationInputs">
          <application-input-field-editor v-for="(input, index) in data.applicationInputs" :value="input" :key="index" :id="'app-input-'+index" :focus="index === focusApplicationInputIndex" @input="updatedInput($event, index)" @delete="deleteInput($event, index)" />
        </draggable>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button variant="secondary" @click="addApplicationInput">
          Add application input
        </b-button>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button variant="primary" @click="save">
          Save
        </b-button>
        <b-button variant="secondary" @click="cancel">
          Cancel
        </b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import vmodel_mixin from "../commons/vmodel_mixin";
import ApplicationInputFieldEditor from "./ApplicationInputFieldEditor.vue";

import draggable from "vuedraggable";

export default {
  name: "application-interface-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: models.ApplicationInterfaceDefinition
    }
  },
  components: {
    ApplicationInputFieldEditor,
    draggable
  },
  computed: {
    trueFalseOptions() {
      return [{ text: "True", value: true }, { text: "False", value: false }];
    }
  },
  data() {
    return {
      focusApplicationInputIndex: null
    };
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    updatedInput(newValue, index) {
      Object.assign(this.data.applicationInputs[index], newValue);
      this.$emit("input", this.data);
    },
    addApplicationInput() {
      this.data.applicationInputs.push(new models.InputDataObjectType());
      this.focusApplicationInputIndex = this.data.applicationInputs.length - 1;
    },
    deleteInput(e, index) {
      this.data.applicationInputs.splice(index, 1);
    }
  }
};
</script>

