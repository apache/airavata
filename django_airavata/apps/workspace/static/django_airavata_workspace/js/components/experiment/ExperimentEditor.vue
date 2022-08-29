<template>
  <div>
    <unsaved-changes-guard :dirty="dirty" />
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">
          <div
            v-if="appModule"
            class="application-name text-muted text-uppercase"
          >
            <i class="fa fa-code" aria-hidden="true"></i>
            {{ appModule.appModuleName }}
          </div>
          <slot name="title">Experiment Editor</slot>
        </h1>
      </div>
      <div class="col-auto">
        <share-button
          ref="shareButton"
          :entity-id="localExperiment.experimentId"
          :entity-label="'Experiment'"
          :parent-entity-id="localExperiment.projectId"
          :parent-entity-label="'Project'"
          :auto-add-default-gateway-users-group="false"
        />
      </div>
    </div>
    <b-form novalidate>
      <div class="row">
        <div class="col">
          <b-form-group
            label="Experiment Name"
            label-for="experiment-name"
            :feedback="getValidationFeedback('experimentName')"
            :state="getValidationState('experimentName')"
          >
            <b-form-input
              id="experiment-name"
              type="text"
              v-model="localExperiment.experimentName"
              required
              placeholder="Experiment name"
              :state="getValidationState('experimentName')"
            ></b-form-input>
          </b-form-group>
          <experiment-description-editor
            v-model="localExperiment.description"
          />
        </div>
      </div>
      <div class="row">
        <div class="col">
          <b-form-group
            label="Project"
            label-for="project"
            :feedback="getValidationFeedback('projectId')"
            :state="getValidationState('projectId')"
          >
            <b-form-select
              id="project"
              v-model="localExperiment.projectId"
              required
              :state="getValidationState('projectId')"
            >
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
        </div>
      </div>
      <div class="row">
        <div class="col">
          <workspace-notices-management-container
            class="mt-2"
            v-if="appInterface && appInterface.applicationDescription"
            :data="[
              { notificationMessage: appInterface.applicationDescription },
            ]"
          />
        </div>
      </div>
      <div class="row">
        <div class="col">
          <h1 class="h4 mt-2 mb-4">Application Configuration</h1>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <div class="card border-default">
            <div class="card-body">
              <h2 class="h6 mb-3">Application Inputs</h2>

              <transition-group name="fade">
                <input-editor-container
                  v-for="experimentInput in localExperiment.experimentInputs"
                  :experiment-input="experimentInput"
                  :experiment="localExperiment"
                  v-model="experimentInput.value"
                  v-show="experimentInput.show"
                  :key="experimentInput.name"
                  @invalid="recordInvalidInputEditorValue(experimentInput.name)"
                  @valid="recordValidInputEditorValue(experimentInput.name)"
                  @input="inputValueChanged"
                  @uploadstart="uploadStart(experimentInput.name)"
                  @uploadend="uploadEnd(experimentInput.name)"
                />
              </transition-group>
            </div>
          </div>
        </div>
      </div>
      <group-resource-profile-selector
        v-model="localExperiment.userConfigurationData.groupResourceProfileId"
      >
      </group-resource-profile-selector>
      <div class="row">
        <div class="col">
          <computational-resource-scheduling-editor
            v-model="
              localExperiment.userConfigurationData
                .computationalResourceScheduling
            "
            v-if="localExperiment.userConfigurationData.groupResourceProfileId"
            :app-module-id="appModule.appModuleId"
            :group-resource-profile-id="
              localExperiment.userConfigurationData.groupResourceProfileId
            "
            @invalid="invalidComputationalResourceSchedulingEditor = true"
            @valid="invalidComputationalResourceSchedulingEditor = false"
          >
          </computational-resource-scheduling-editor>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <b-form-group label="Email Settings">
            <b-form-checkbox v-model="localExperiment.enableEmailNotification">
              Receive email notification of experiment status
            </b-form-checkbox>
          </b-form-group>
        </div>
      </div>
      <div class="row">
        <div id="col-exp-buttons" class="col">
          <b-button
            variant="success"
            @click="saveAndLaunchExperiment"
            :disabled="isSaveDisabled"
          >
            Save and Launch
          </b-button>
          <b-button
            variant="primary"
            @click="saveExperiment"
            :disabled="isSaveDisabled"
          >
            Save
          </b-button>
        </div>
      </div>
    </b-form>
  </div>
</template>

<script>
import ComputationalResourceSchedulingEditor from "./ComputationalResourceSchedulingEditor.vue";
import ExperimentDescriptionEditor from "./ExperimentDescriptionEditor.vue";
import GroupResourceProfileSelector from "./GroupResourceProfileSelector.vue";
import InputEditorContainer from "./input-editors/InputEditorContainer.vue";
import { models, services } from "django-airavata-api";
import { components, utils } from "django-airavata-common-ui";
import WorkspaceNoticesManagementContainer from "../notices/WorkspaceNoticesManagementContainer";
import _ from "lodash";

export default {
  name: "edit-experiment",
  props: {
    experiment: {
      type: models.Experiment,
      required: true,
    },
    appModule: {
      type: models.ApplicationModule,
      required: true,
    },
    appInterface: {
      type: models.ApplicationInterfaceDefinition,
      required: true,
    },
  },
  data() {
    return {
      projects: [],
      localExperiment: this.experiment.clone(),
      invalidInputs: [],
      invalidComputationalResourceSchedulingEditor: false,
      edited: false,
      saved: false,
      uploadingInputs: [],
    };
  },
  components: {
    WorkspaceNoticesManagementContainer,
    ComputationalResourceSchedulingEditor,
    ExperimentDescriptionEditor,
    GroupResourceProfileSelector,
    InputEditorContainer,
    "share-button": components.ShareButton,
    "unsaved-changes-guard": components.UnsavedChangesGuard,
  },
  mounted: function () {
    services.ProjectService.listAll().then((projects) => {
      this.projects = projects;
      if (!this.localExperiment.projectId) {
        services.WorkspacePreferencesService.get().then(
          (workspacePreferences) => {
            if (!this.localExperiment.projectId) {
              this.localExperiment.projectId =
                workspacePreferences.most_recent_project_id;
            }
          }
        );
      }
    });
  },
  computed: {
    sharedProjectOptions: function () {
      return this.projects
        .filter((p) => !p.isOwner)
        .map((project) => ({
          value: project.projectID,
          text:
            project.name +
            (!project.isOwner ? " (owned by " + project.owner + ")" : ""),
        }));
    },
    myProjectOptions() {
      return this.projects
        .filter((p) => p.isOwner)
        .map((project) => ({
          value: project.projectID,
          text: project.name,
        }));
    },
    valid: function () {
      const validation = this.localExperiment.validate();
      return (
        Object.keys(validation).length === 0 &&
        this.invalidInputs.length === 0 &&
        !this.invalidComputationalResourceSchedulingEditor
      );
    },
    isSaveDisabled: function () {
      return !this.valid || this.hasUploadingInputs;
    },
    dirty() {
      return this.edited && !this.saved;
    },
    hasUploadingInputs() {
      return this.uploadingInputs.length > 0;
    },
  },
  methods: {
    saveExperiment: function () {
      return this.saveOrUpdateExperiment().then((experiment) => {
        this.localExperiment = experiment;
        this.$emit("saved", experiment);
      });
    },
    saveAndLaunchExperiment: function () {
      return this.saveOrUpdateExperiment().then((experiment) => {
        this.localExperiment = experiment;
        return services.ExperimentService.launch({
          lookup: experiment.experimentId,
        }).then(() => {
          this.$emit("savedAndLaunched", experiment);
        });
      });
    },
    saveOrUpdateExperiment: function () {
      if (this.localExperiment.experimentId) {
        return services.ExperimentService.update({
          lookup: this.localExperiment.experimentId,
          data: this.localExperiment,
        }).then((experiment) => {
          this.saved = true;
          return experiment;
        });
      } else {
        return services.ExperimentService.create({
          data: this.localExperiment,
        }).then((experiment) => {
          // Can't save sharing settings for a new experiment until it has been
          // created
          this.saved = true;
          return this.$refs.shareButton
            .mergeAndSave(experiment.experimentId)
            .then(() => experiment);
        });
      }
    },
    getValidationFeedback: function (properties) {
      return utils.getProperty(this.localExperiment.validate(), properties);
    },
    getValidationState: function (properties) {
      return this.getValidationFeedback(properties) ? false : null;
    },
    recordInvalidInputEditorValue: function (experimentInputName) {
      if (!this.invalidInputs.includes(experimentInputName)) {
        this.invalidInputs.push(experimentInputName);
      }
    },
    recordValidInputEditorValue: function (experimentInputName) {
      if (this.invalidInputs.includes(experimentInputName)) {
        const index = this.invalidInputs.indexOf(experimentInputName);
        this.invalidInputs.splice(index, 1);
      }
    },
    uploadStart(experimentInputName) {
      if (!this.uploadingInputs.includes(experimentInputName)) {
        this.uploadingInputs.push(experimentInputName);
      }
    },
    uploadEnd(experimentInputName) {
      if (this.uploadingInputs.includes(experimentInputName)) {
        const index = this.uploadingInputs.indexOf(experimentInputName);
        this.uploadingInputs.splice(index, 1);
      }
    },
    inputValueChanged: function () {
      this.localExperiment.evaluateInputDependencies();
    },
    calculateQueueSettings: _.debounce(async function () {
      const queueSettingsUpdate = await services.QueueSettingsCalculatorService.calculate(
        {
          lookup: this.appInterface.queueSettingsCalculatorId,
          data: this.localExperiment,
        },
        { showSpinner: false }
      );
      // Override values in computationalResourceScheduling with the values
      // returned from the queue settings calculator
      Object.assign(
        this.localExperiment.userConfigurationData
          .computationalResourceScheduling,
        queueSettingsUpdate
      );
    }, 500),
    experimentInputsChanged() {
      if (this.appInterface.queueSettingsCalculatorId) {
        this.calculateQueueSettings();
      }
    },
    resourceHostIdChanged() {
      if (this.appInterface.queueSettingsCalculatorId) {
        this.calculateQueueSettings();
      }
    },
  },
  watch: {
    experiment: function (newValue) {
      this.localExperiment = newValue.clone();
    },
    localExperiment: {
      handler() {
        this.edited = true;
      },
      deep: true,
    },
    "experiment.experimentInputs": {
      handler() {
        this.experimentInputsChanged();
      },
      deep: true,
    },
    "experiment.userConfigurationData.computationalResourceScheduling.resourceHostId": function () {
      this.resourceHostIdChanged();
    },
  },
};
</script>

<style>
.application-name {
  font-size: 12px;
}

#col-exp-buttons {
  text-align: right;
}
</style>
