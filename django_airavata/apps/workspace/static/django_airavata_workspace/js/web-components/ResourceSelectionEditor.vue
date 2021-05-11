<template>
  <div v-if="userConfigurationData">
    <div @input.stop="updateGroupResourceProfileId">
      <adpf-group-resource-profile-selector
        slot="resource-selection-grp"
        :value="userConfigurationData.groupResourceProfileId"
      />
    </div>
    <div @input.stop="updateComputeResourceHostId">
      <adpf-compute-resource-selector
        ref="computeResourceSelector"
        slot="resource-selection-compute-resource"
        :value="
          userConfigurationData.computationalResourceScheduling.resourceHostId
        "
      />
    </div>
    <div @input.stop="updateComputationalResourceScheduling">
      <adpf-queue-settings-editor
        slot="resource-selection-queue-settings"
        :value="userConfigurationData.computationalResourceScheduling"
        :queues="queues"
        :max-allowed-nodes="maxAllowedNodes"
        :max-allowed-cores="maxAllowedCores"
        :max-allowed-walltime="maxAllowedWalltime"
        :max-memory="maxMemory"
      />
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import {
  getApplicationDeployments,
  getDefaultComputeResourceId,
} from "./store";
export default {
  // TODO: better name? UserConfigurationDataEditor?
  name: "resource-selection-editor",
  props: {
    value: {
      type: models.UserConfigurationData,
    },
    applicationModuleId: {
      type: String,
    },
  },
  data() {
    return {
      userConfigurationData: this.cloneValue(),
      applicationDeployments: [],
      queues: [],
      maxAllowedNodes: 0,
      maxAllowedCores: 0,
      maxAllowedWalltime: 0,
      maxMemory: 0,
      defaultComputeResourceId: null,
    };
  },
  computed: {
    computeResources() {
      return this.applicationDeployments.map((dep) => dep.computeHostId);
    },
    groupResourceProfileId() {
      return this.userConfigurationData
        ? this.userConfigurationData.groupResourceProfileId
        : null;
    },
    resourceHostId() {
      return this.userConfigurationData &&
        this.userConfigurationData.computationalResourceScheduling
        ? this.userConfigurationData.computationalResourceScheduling
            .resourceHostId
        : null;
    },
  },
  methods: {
    emitValueChanged: function () {
      const inputEvent = new CustomEvent("input", {
        detail: [this.userConfigurationData],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
    updateGroupResourceProfileId(event) {
      const [groupResourceProfileId] = event.detail;
      this.userConfigurationData.groupResourceProfileId = groupResourceProfileId;
      this.emitValueChanged();
      this.loadApplicationDeployments();
    },
    updateComputeResourceHostId(event) {
      const [computeResourceHostId] = event.detail;
      this.userConfigurationData.computationalResourceScheduling.resourceHostId = computeResourceHostId;
      this.emitValueChanged();
      // TODO: recalculate queues for the selected host
    },
    updateComputationalResourceScheduling(event) {
      const [computationalResourceScheduling] = event.detail;
      this.userConfigurationData.computationalResourceScheduling = computationalResourceScheduling;
      this.emitValueChanged();
      // TODO: recalculate maxes for the selected queue, etc.
    },
    async loadApplicationDeployments() {
      this.applicationDeployments = await getApplicationDeployments(
        this.applicationModuleId,
        this.groupResourceProfileId
      );
      if (
        !this.userConfigurationData.computationalResourceScheduling
          .computeHostId
      ) {
        this.userConfigurationData.computationalResourceScheduling.resourceHostId = this.getDefaultResourceHostId();
      }
    },
    cloneValue() {
      return this.value ? new models.UserConfigurationData(this.value) : null;
    },
    async loadData() {
      if (this.groupResourceProfileId) {
        this.loadApplicationDeployments();
      }
      this.loadDefaultComputeResourceId();
    },
    async loadDefaultComputeResourceId() {
      this.defaultComputeResourceId = await getDefaultComputeResourceId();
    },
    getDefaultResourceHostId() {
      if (
        this.defaultComputeResourceId &&
        this.computeResources.find(
          (crid) => crid === this.defaultComputeResourceId
        )
      ) {
        return this.defaultComputeResourceId;
      } else if (this.computeResources.length > 0) {
        // Just pick the first one
        return this.computeResources[0];
      }
    },
    bindWebComponentProps() {
      this.$nextTick(() => {
        this.$refs.computeResourceSelector.computeResources = this.computeResources;
        this.$refs.computeResourceSelector.value = this.resourceHostId;
      });
    },
  },
  watch: {
    value() {
      this.userConfigurationData = this.cloneValue();
      this.loadData();
    },
    computeResources: "bindWebComponentProps",
    resourceHostId: "bindWebComponentProps",
  },
};
</script>

<style></style>
