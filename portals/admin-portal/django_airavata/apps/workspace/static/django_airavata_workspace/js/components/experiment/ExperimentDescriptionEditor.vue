<template>
  <b-form-group
    v-if="isEditing"
    label="Experiment Description"
    label-for="experiment-description"
  >
    <b-form-textarea
      id="experiment-description"
      v-model="data"
      rows="3"
      ref="description"
      maxlength="255"
    ></b-form-textarea>
    <div class="mt-1">
      <b-button variant="success" size="sm" @click="toggleEditing"
        >Save description</b-button
      >
      <b-link
        @click="cancelEditing"
        title="Cancel editing"
        class="text-secondary ml-3"
      >
        <i class="fas fa-times"></i>
        <span class="sr-only">Cancel editing</span>
      </b-link>
    </div>
  </b-form-group>
  <div v-else class="mb-3">
    <b-link @click="startEditing" class="d-inline-block text-body mb-1">
      <i class="fas fa-align-left"></i>
      <span v-if="data"> Edit the description</span>
      <span v-else> Add a description</span>
    </b-link>
    <div v-if="data" class="ml-3">
      {{ data }}
    </div>
  </div>
</template>

<script>
import { mixins } from "django-airavata-common-ui";

export default {
  name: "experiment-description-editor",
  mixins: [mixins.VModelMixin],
  data() {
    return {
      isEditing: false,
      originalValue: this.value,
    };
  },
  methods: {
    toggleEditing() {
      this.isEditing = !this.isEditing;
    },
    startEditing() {
      this.originalValue = this.data;
      this.isEditing = true;
      this.$nextTick(() => this.$refs.description.focus());
    },
    cancelEditing() {
      this.data = this.originalValue;
      this.isEditing = false;
    },
  },
};
</script>
