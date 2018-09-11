<template>
  <b-card :title="title" title-tag="h5">
    <b-input-group v-for="setEnvPath in data" :key="setEnvPath.key" class="mb-1 align-items-center">
      <b-form-input type="text" v-model="setEnvPath.name" required placeholder="NAME" ref="nameInputs" />
      <font-awesome-icon icon="equals" class="mx-1" />
      <b-form-input type="text" v-model="setEnvPath.value" required placeholder="VALUE" />
      <b-input-group-append>
        <b-button variant="secondary" @click="deleteEnvPath(setEnvPath)">
          <i class="fa fa-trash"></i>
          <span class="sr-only">Delete</span>
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <b-button variant="secondary" @click="addEnvPath">{{ addButtonLabel }}</b-button>
  </b-card>
</template>

<script>
import vmodel_mixin from "../commons/vmodel_mixin";
import { models, services } from "django-airavata-api";

export default {
  name: "set-env-paths-editor",
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
    }
  },
  methods: {
    addEnvPath() {
      if (!this.data) {
        this.data = [];
      }
      this.data.push(new models.SetEnvPaths());
      this.$nextTick(() =>
        this.$refs.nameInputs[this.$refs.nameInputs.length - 1].focus()
      );
    },
    deleteEnvPath(setEnvPath) {
      const index = this.data.findIndex(env => env.key === setEnvPath.key);
      this.data.splice(index, 1);
    }
  }
};
</script>

