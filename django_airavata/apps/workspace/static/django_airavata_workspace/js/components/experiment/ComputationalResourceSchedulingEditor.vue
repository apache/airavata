<template>
  <div>
    <div class="row">
      <div class="col">
        <b-form-group
          label="Compute Resource"
          label-for="compute-resource"
          :feedback="getValidationFeedback('resourceHostId')"
          :state="getValidationState('resourceHostId')"
        >
          <b-form-select
            id="compute-resource"
            v-model="resourceHostId"
            :options="computeResourceOptions"
            required
            @change="computeResourceChanged"
            :state="getValidationState('resourceHostId')"
            :disabled="
              !computeResourceOptions || computeResourceOptions.length === 0
            "
          >
            <template slot="first">
              <option :value="null" disabled>Select a Compute Resource</option>
            </template>
          </b-form-select>
        </b-form-group>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <queue-settings-editor
          v-model="data"
          v-if="appDeploymentId"
          :app-module-id="appModuleId"
          :app-deployment-id="appDeploymentId"
          :compute-resource-policy="selectedComputeResourcePolicy"
          :batch-queue-resource-policies="batchQueueResourcePolicies"
          @input="queueSettingsChanged"
          @valid="queueSettingsValidityChanged(true)"
          @invalid="queueSettingsValidityChanged(false)"
        >
        </queue-settings-editor>
      </div>
    </div>
  </div>
</template>

<script>
import QueueSettingsEditor from "./QueueSettingsEditor.vue";
import {
  errors,
  models,
  services,
  utils as apiUtils,
} from "django-airavata-api";
import { mixins, utils } from "django-airavata-common-ui";

export default {
  name: "computational-resource-scheduling-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.ComputationalResourceSchedulingModel,
    },
    appModuleId: {
      type: String,
      required: true,
    },
    groupResourceProfileId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      computeResources: {},
      applicationDeployments: [],
      selectedGroupResourceProfileData: null,
      resourceHostId: this.value.resourceHostId,
      invalidQueueSettings: false,
      workspacePreferences: null,
    };
  },
  components: {
    QueueSettingsEditor,
  },
  mounted: function () {
    this.loadWorkspacePreferences().then(() => {
      this.loadApplicationDeployments(
        this.appModuleId,
        this.groupResourceProfileId
      );
    });
    this.loadComputeResourceNames();
    this.loadGroupResourceProfile();
    this.validate();
    this.$on("input", () => this.validate());
  },
  computed: {
    localComputationalResourceScheduling() {
      return this.data;
    },
    computeResourceOptions: function () {
      const computeResourceOptions = this.applicationDeployments.map((dep) => {
        return {
          value: dep.computeHostId,
          text:
            dep.computeHostId in this.computeResources
              ? this.computeResources[dep.computeHostId]
              : "",
        };
      });
      computeResourceOptions.sort((a, b) => a.text.localeCompare(b.text));
      return computeResourceOptions;
    },
    selectedComputeResourcePolicy: function () {
      if (this.selectedGroupResourceProfileData === null) {
        return null;
      }
      return this.selectedGroupResourceProfileData.computeResourcePolicies.find(
        (crp) => {
          return (
            crp.computeResourceId ===
            this.localComputationalResourceScheduling.resourceHostId
          );
        }
      );
    },
    batchQueueResourcePolicies: function () {
      if (this.selectedGroupResourceProfileData === null) {
        return null;
      }
      return this.selectedGroupResourceProfileData.batchQueueResourcePolicies.filter(
        (bqrp) => {
          return (
            bqrp.computeResourceId ===
            this.localComputationalResourceScheduling.resourceHostId
          );
        }
      );
    },
    appDeploymentId: function () {
      // We'll only be able to figure out the appDeploymentId when a
      // resourceHostId is selected and the application deployments are
      // loaded
      if (!this.resourceHostId || this.applicationDeployments.length === 0) {
        return null;
      }
      // Find application deployment that corresponds to this compute resource
      let selectedApplicationDeployment = this.applicationDeployments.find(
        (dep) => dep.computeHostId === this.resourceHostId
      );
      if (!selectedApplicationDeployment) {
        throw new Error("Failed to find application deployment!");
      }
      return selectedApplicationDeployment.appDeploymentId;
    },
    validation() {
      const queueInfo = {}; // QueueSettingsEditor will validate queue information
      return this.localComputationalResourceScheduling.validate(queueInfo);
    },
    valid() {
      return (
        !this.invalidQueueSettings && Object.keys(this.validation).length === 0
      );
    },
  },
  methods: {
    computeResourceChanged: function (selectedComputeResourceId) {
      this.data.resourceHostId = selectedComputeResourceId;
    },
    loadApplicationDeployments: function (appModuleId, groupResourceProfileId) {
      services.ApplicationDeploymentService.list(
        {
          appModuleId: appModuleId,
          groupResourceProfileId: groupResourceProfileId,
        },
        { ignoreErrors: true }
      )
        .then((applicationDeployments) => {
          this.applicationDeployments = applicationDeployments;
        })
        .catch((error) => {
          // Ignore unauthorized errors, force user to pick another GroupResourceProfile
          if (!errors.ErrorUtils.isUnauthorizedError(error)) {
            return Promise.reject(error);
          }
        })
        // Report all other error types
        .catch(apiUtils.FetchUtils.reportError);
    },
    loadGroupResourceProfile: function () {
      services.GroupResourceProfileService.retrieve(
        { lookup: this.groupResourceProfileId },
        { ignoreErrors: true }
      )
        .then((groupResourceProfile) => {
          this.selectedGroupResourceProfileData = groupResourceProfile;
        })
        .catch((error) => {
          // Ignore unauthorized errors, force user to pick a different GroupResourceProfile
          if (!errors.ErrorUtils.isUnauthorizedError(error)) {
            return Promise.reject(error);
          }
        })
        // Report all other error types
        .catch(apiUtils.FetchUtils.reportError);
    },
    loadComputeResourceNames: function () {
      services.ComputeResourceService.names().then(
        (computeResourceNames) => (this.computeResources = computeResourceNames)
      );
    },
    loadWorkspacePreferences() {
      return services.WorkspacePreferencesService.get().then(
        (workspacePreferences) =>
          (this.workspacePreferences = workspacePreferences)
      );
    },
    queueSettingsChanged: function () {
      // QueueSettingsEditor updates the full
      // ComputationalResourceSchedulingModel instance but doesn't know
      // the resourceHostId so we need to copy it back into the instance
      // whenever it changes
      this.localComputationalResourceScheduling.resourceHostId = this.resourceHostId;
      this.$emit("input", this.data);
    },
    queueSettingsValidityChanged(valid) {
      this.invalidQueueSettings = !valid;
      this.validate();
    },
    validate() {
      if (!this.valid) {
        this.$emit("invalid");
      } else {
        this.$emit("valid");
      }
    },
    emitValueChanged: function () {
      this.validate();
      this.$emit("input", this.localComputationalResourceScheduling);
    },
    getValidationFeedback: function (properties) {
      return utils.getProperty(this.validation, properties);
    },
    getValidationState: function (properties) {
      return this.getValidationFeedback(properties) ? false : null;
    },
  },
  watch: {
    computeResourceOptions: function (newOptions) {
      // If the selected resourceHostId is not in the new list of
      // computeResourceOptions, reset it to null
      if (
        this.resourceHostId !== null &&
        !newOptions.find((opt) => opt.value === this.resourceHostId)
      ) {
        this.resourceHostId = null;
      }
      // Apply preferred (most recently used) compute resource
      if (
        this.resourceHostId === null &&
        this.workspacePreferences.most_recent_compute_resource_id &&
        newOptions.find(
          (opt) =>
            opt.value ===
            this.workspacePreferences.most_recent_compute_resource_id
        )
      ) {
        this.resourceHostId = this.workspacePreferences.most_recent_compute_resource_id;
      }
      // If none selected, just pick the first one
      if (this.resourceHostId === null && newOptions.length > 0) {
        this.resourceHostId = newOptions[0].value;
      }
      this.computeResourceChanged(this.resourceHostId);
    },
    groupResourceProfileId: function (newGroupResourceProfileId) {
      this.loadApplicationDeployments(
        this.appModuleId,
        newGroupResourceProfileId
      );
      if (
        this.selectedGroupResourceProfileData &&
        this.selectedGroupResourceProfileData.groupResourceProfileId !==
          newGroupResourceProfileId
      ) {
        this.loadGroupResourceProfile();
      }
    },
  },
};
</script>

<style></style>
