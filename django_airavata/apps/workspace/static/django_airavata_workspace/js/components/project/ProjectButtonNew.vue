<template>
  <div>
    <b-btn v-b-modal.modal-new-project variant="primary">
      <slot> New Project <i class="fa fa-plus" aria-hidden="true"></i> </slot>
    </b-btn>
    <b-modal
      id="modal-new-project"
      ref="modalNewProject"
      title="Create New Project"
      v-on:ok="onCreateProject"
      v-bind:ok-disabled="okDisabled"
      @cancel="onCancelNewProject"
    >
      <project-editor
        v-model="newProject"
        ref="projectEditor"
        @save="onCreateProject"
        @valid="valid = true"
        @invalid="valid = false"
      >
        <div slot="title"></div>
      </project-editor>
    </b-modal>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import ProjectEditor from "./ProjectEditor.vue";

export default {
  name: "project-button-new",
  data() {
    return {
      valid: false,
      newProject: new models.Project(),
    };
  },
  components: {
    ProjectEditor,
  },
  methods: {
    onCreateProject: function (event) {
      // Prevent hiding modal, hide it programmatically when project gets created
      event.preventDefault();
      services.ProjectService.create({ data: this.newProject }).then(
        (result) => {
          this.$refs.modalNewProject.hide();
          this.$emit("new-project", result);
          // Reset state
          this.newProject = new models.Project();
          this.$refs.projectEditor.reset();
        }
      );
    },
    onCancelNewProject() {
      this.newProject = new models.Project();
      this.$refs.projectEditor.reset();
    },
  },
  computed: {
    okDisabled: function () {
      return !this.valid;
    },
  },
};
</script>
