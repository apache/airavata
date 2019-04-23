<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">
          <slot name="title">Edit Project</slot>
        </h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-form-group
          label="Project Name"
          label-for="project-name"
          :feedback="nameFeedback"
          :state="nameState"
        >
          <b-form-input
            id="project-name"
            type="text"
            v-model="data.name"
            required
            placeholder="Project name"
            :state="nameState"
          ></b-form-input>
        </b-form-group>
        <b-form-group
          label="Project Description"
          label-for="project-description"
        >
          <b-form-textarea
            id="project-description"
            type="text"
            v-model="data.description"
            placeholder="(Optional) Project description"
            :rows="3"
          ></b-form-textarea>
        </b-form-group>
      </div>
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";

export default {
  name: "project-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.Project,
      required: true
    }
  },
  mounted() {
    this.$on("input", this.validate);
  },
  data() {
    return {
      validation: {
        name: null
      }
    };
  },
  computed: {
    nameFeedback() {
      if (this.validation.name) {
        return this.validation.name.join("; ");
      }
    },
    nameState() {
      return !this.validation.name || this.validation.name.length === 0;
    }
  },
  methods: {
    validate() {
      const v = this.data.validate();
      this.validation = v ? v : {};
    }
  }
};
</script>

