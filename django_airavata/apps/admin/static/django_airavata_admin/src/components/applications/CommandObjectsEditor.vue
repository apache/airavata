<template>
  <b-card :title="title" title-tag="h5">
    <b-input-group v-for="commandObject in data" :key="commandObject.key" class="mb-1">
      <b-form-input type="text" v-model="commandObject.command" required ref="commandObjectInputs" :disabled="readonly" />
      <b-input-group-append v-if="!readonly">
        <b-button variant="secondary" @click="deleteCommandObject(commandObject)">
          <i class="fa fa-trash"></i>
          <span class="sr-only">Delete</span>
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <b-button v-if="!readonly" variant="secondary" @click="addCommandObject">{{ addButtonLabel }}</b-button>
  </b-card>
</template>

<script>
import vmodel_mixin from "../commons/vmodel_mixin";
import { models, services } from "django-airavata-api";

export default {
  name: "command-objects-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: Array
    },
    title: {
      type: String,
      required: true
    },
    addButtonLabel: {
      type: String,
      required: true
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    addCommandObject() {
      if (!this.data) {
        this.data = [];
      }
      this.data.push(new models.CommandObject());
      this.$nextTick(() =>
        this.$refs.commandObjectInputs[
          this.$refs.commandObjectInputs.length - 1
        ].focus()
      );
    },
    deleteCommandObject(commandObject) {
      const index = this.data.findIndex(cmd => cmd.key === commandObject.key);
      this.data.splice(index, 1);
    }
  }
};
</script>

