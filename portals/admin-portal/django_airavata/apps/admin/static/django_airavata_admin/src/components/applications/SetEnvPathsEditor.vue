<template>
  <b-card :title="title" title-tag="h5">
    <b-input-group
      v-for="setEnvPath in data"
      :key="setEnvPath.key"
      class="mb-1 align-items-center"
    >
      <b-form-input
        type="text"
        v-model="setEnvPath.name"
        required
        placeholder="NAME"
        ref="nameInputs"
        :disabled="readonly"
      />
      <i class="fa fa-equals mx-1"></i>
      <b-form-input
        type="text"
        v-model="setEnvPath.value"
        required
        placeholder="VALUE"
        :disabled="readonly"
      />
      <b-input-group-append v-if="!readonly">
        <b-button variant="secondary" @click="deleteEnvPath(setEnvPath)">
          <i class="fa fa-trash"></i>
          <span class="sr-only">Delete</span>
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <b-button v-if="!readonly" variant="secondary" @click="addEnvPath">{{
      addButtonLabel
    }}</b-button>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";

export default {
  name: "set-env-paths-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: Array,
    },
    title: {
      type: String,
      required: true,
    },
    addButtonLabel: {
      type: String,
      required: true,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
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
      const index = this.data.findIndex((env) => env.key === setEnvPath.key);
      this.data.splice(index, 1);
    },
  },
};
</script>
