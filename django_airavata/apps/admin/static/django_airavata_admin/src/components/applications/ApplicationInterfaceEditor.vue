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
          <b-form-radio-group id="archive-directory"
            v-model="appInterface.archiveWorkingDirectory"
            :options="archiveWorkingDirectoryOptions"
            @input="emitChanged">
          </b-form-radio-group>
        </b-form-group>
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

export default {
  name: "application-interface-editor",
  props: {
    value: {
      type: models.ApplicationInterfaceDefinition,
      required: true
    }
  },
  data: function() {
    return {
      appInterface: this.value.clone()
    };
  },
  computed: {
    archiveWorkingDirectoryOptions() {
      return [{ text: "True", value: true }, { text: "False", value: false }];
    }
  },
  methods: {
    emitChanged() {
      this.$emit("input", this.appInterface);
    },
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    }
  },
  watch: {
    value: function(newValue) {
      this.appInterface = newValue.clone();
    }
  }
};
</script>

