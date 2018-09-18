<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          Application Details
        </h1>
        <b-form-group label="Application Name" label-for="application-name">
          <b-form-input id="application-name" type="text" v-model="data.appModuleName" required :disabled="readonly"></b-form-input>
        </b-form-group>
        <b-form-group label="Application Version" label-for="application-version">
          <b-form-input id="application-version" type="text" v-model="data.appModuleVersion" :disabled="readonly"></b-form-input>
        </b-form-group>
        <b-form-group label="Application Description" label-for="application-description">
          <b-form-textarea id="application-description" v-model="data.appModuleDescription" :rows="3" :disabled="readonly"></b-form-textarea>
        </b-form-group>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button variant="primary" @click="save" :disabled="readonly">
          Save
        </b-button>
        <delete-button v-if="data.appModuleId" :disabled="readonly" @delete="deleteApplicationModule">
          Are you sure you want to delete the {{ data.appModuleName }} application?
        </delete-button>
        <b-button variant="secondary" @click="cancel">
          Cancel
        </b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { components } from "django-airavata-common-ui";
import vmodel_mixin from "../commons/vmodel_mixin";

export default {
  name: "application-module-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: models.ApplicationModule
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  components: {
    "delete-button": components.DeleteButton
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    deleteApplicationModule() {
      this.$emit("delete", this.data);
    }
  }
};
</script>

