<template>
  <b-form-group label="Project">
    <b-form-select v-model="projectId" required>
      <template slot="first">
        <option :value="null" disabled>Select a Project</option>
      </template>
      <optgroup label="My Projects">
        <option
          v-for="project in myProjectOptions"
          :value="project.value"
          :key="project.value"
        >
          {{ project.text }}
        </option>
      </optgroup>
      <optgroup label="Projects Shared With Me">
        <option
          v-for="project in sharedProjectOptions"
          :value="project.value"
          :key="project.value"
        >
          {{ project.text }}
        </option>
      </optgroup>
    </b-form-select>
  </b-form-group>
</template>

<script>
import { getDefaultProjectId, getProjects } from "./store";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      projectId: this.value,
      projects: null,
    };
  },
  async mounted() {
    this.projects = await getProjects();
    const defaultProjectId = await getDefaultProjectId();
    if (!this.projectId) {
      this.projectId = defaultProjectId;
      this.$emit("input", this.projectId);
    }
  },
  computed: {
    sharedProjectOptions: function () {
      return this.projects
        ? this.projects
            .filter((p) => !p.isOwner)
            .map((project) => ({
              value: project.projectID,
              text:
                project.name +
                (!project.isOwner ? " (owned by " + project.owner + ")" : ""),
            }))
        : [];
    },
    myProjectOptions() {
      return this.projects
        ? this.projects
            .filter((p) => p.isOwner)
            .map((project) => ({
              value: project.projectID,
              text: project.name,
            }))
        : [];
    },
  },
  watch: {
    projectId() {
      const inputEvent = new CustomEvent("input", {
        detail: [this.projectId],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
  },
};
</script>

<style>
@import url("./styles.css");
</style>
