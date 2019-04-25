<template>
  <div v-if="project">
    <project-editor v-model="project" @save="saveProject" @valid="valid = true" @invalid="valid = false"/>
    <div class="d-flex justify-content-end">
        <b-button @click="saveProject" variant="primary" :disabled="!valid">Save</b-button>
        <b-button @click="cancel" variant="secondary">Cancel</b-button>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import urls from "../utils/urls";
import ProjectEditor from "../components/project/ProjectEditor.vue"

export default {
  name: "edit-project-container",
  props: {
    projectId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      project: null,
      valid: false
    };
  },
  components: {
    ProjectEditor
  },
  created() {
    services.ProjectService.retrieve({lookup: this.projectId})
    .then(project => this.project = project);
  },
  methods: {
    saveProject() {
      if (this.valid) {
        services.ProjectService.update({lookup: this.projectId, data: this.project})
        .then(() => {
          urls.navigateToProjectsList();
        });
      }
    },
    cancel() {
      urls.navigateToProjectsList();
    }
  }
};
</script>
<style>
/* style the containing div, in base.html template */
.main-content {
  background-color: #ffffff;
}
</style>
