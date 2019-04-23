<template>
  <div>
    <div class="row">
      <div class="col">
        <project-editor v-if="project" v-model="project" />
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button @click="saveProject" variant="primary">Save</b-button>
      </div>
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
      services.ProjectService.update({lookup: this.projectId, data: this.project})
      .then(() => {
        urls.navigateToProjectsList();
      });
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
