<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Browse Projects</h1>
      </div>
      <div id="col-new-project" class="col">
        <project-button-new @new-project="onNewProject" />
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <project-list v-bind:projects="projects"></project-list>
            <pager
              v-bind:paginator="projectsPaginator"
              v-on:next="nextProjects"
              v-on:previous="previousProjects"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProjectButtonNew from "../components/project/ProjectButtonNew.vue";
import ProjectList from "../components/project/ProjectList.vue";

import { services } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";

export default {
  props: ["initialProjectsData"],
  name: "project-list-container",
  data() {
    return {
      projectsPaginator: null,
    };
  },
  components: {
    "project-list": ProjectList,
    "project-button-new": ProjectButtonNew,
    pager: comps.Pager,
  },
  methods: {
    nextProjects: function () {
      this.projectsPaginator.next();
    },
    previousProjects: function () {
      this.projectsPaginator.previous();
    },
    onNewProject: function () {
      services.ProjectService.list().then(
        (result) => (this.projectsPaginator = result)
      );
    },
  },
  computed: {
    projects: function () {
      return this.projectsPaginator ? this.projectsPaginator.results : null;
    },
  },
  beforeMount: function () {
    services.ProjectService.list({
      initialData: this.initialProjectsData,
    }).then((result) => (this.projectsPaginator = result));
  },
};
</script>

<style>
#col-new-project {
  text-align: right;
}
#modal-new-project {
  text-align: left;
}
</style>
