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
        ref="queueSettingsEditor"
        slot="resource-selection-queue-settings"
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
  getAppDeploymentQueues,
  getApplicationDeployments,
  getDefaultComputeResourceId,
  getDefaultGroupResourceProfileId,
  getGroupResourceProfile,
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
      appDeploymentQueues: [],
      groupResourceProfile: null,
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
    applicationDeployment() {
      if (this.applicationDeployments && this.resourceHostId) {
        return this.applicationDeployments.find(
          (ad) => ad.computeHostId === this.resourceHostId
        );
      } else {
        return null;
      }
    },
    computeResourcePolicy() {
      if (!this.groupResourceProfile || !this.resourceHostId) {
        return null;
      }
      return this.groupResourceProfile.computeResourcePolicies.find(
        (crp) => crp.computeResourceId === this.resourceHostId
      );
    },
    batchQueueResourcePolicies: function () {
      if (!this.groupResourceProfile || !this.resourceHostId) {
        return null;
      }
      return this.groupResourceProfile.batchQueueResourcePolicies.filter(
        (bqrp) => bqrp.computeResourceId === this.resourceHostId
      );
    },
    batchQueueResourcePolicy() {
      if (!this.batchQueueResourcePolicies || !this.queueName) {
        return null;
      }
      return this.batchQueueResourcePolicies.find(
        (bqrp) => bqrp.queuename === this.queueName
      );
    },
    queueName() {
      return this.userConfigurationData &&
        this.userConfigurationData.computationalResourceScheduling
        ? this.userConfigurationData.computationalResourceScheduling.queueName
        : null;
    },
    queues() {
      return this.appDeploymentQueues
        ? this.appDeploymentQueues.filter((q) =>
            this.isQueueInComputeResourcePolicy(q.queueName)
          )
        : [];
    },
    queue() {
      return this.queues && this.queueName
        ? this.queues.find((q) => q.queueName === this.queueName)
        : null;
    },
    maxAllowedCores: function () {
      if (!this.queue) {
        return 0;
      }
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedCores,
          this.queue.maxProcessors
        );
      }
      return this.queue.maxProcessors;
    },
    maxAllowedNodes: function () {
      if (!this.queue) {
        return 0;
      }
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedNodes,
          this.queue.maxNodes
        );
      }
      return this.queue.maxNodes;
    },
    maxAllowedWalltime: function () {
      if (!this.queue) {
        return 0;
      }
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedWalltime,
          this.queue.maxRunTime
        );
      }
      return this.queue.maxRunTime;
    },
    maxMemory() {
      return this.queue ? this.queue.maxMemory : 0;
    },
  },
  methods: {
    emitValueChanged: function () {
      const inputEvent = new CustomEvent("input", {
        detail: [this.userConfigurationData.clone()],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
    async updateGroupResourceProfileId(event) {
      const [groupResourceProfileId] = event.detail;
      this.userConfigurationData.groupResourceProfileId = groupResourceProfileId;
      await this.loadGroupResourceProfile();
      await this.loadApplicationDeployments();
      await this.applyGroupResourceProfile();
      this.emitValueChanged();
    },
    async updateComputeResourceHostId(event) {
      const [computeResourceHostId] = event.detail;
      if (
        this.userConfigurationData.computationalResourceScheduling
          .resourceHostId !== computeResourceHostId
      ) {
        this.userConfigurationData.computationalResourceScheduling.resourceHostId = computeResourceHostId;
        await this.loadAppDeploymentQueues();
        this.setDefaultQueue();
        this.emitValueChanged();
      }
    },
    updateComputationalResourceScheduling(event) {
      const [computationalResourceScheduling] = event.detail;
      const queueChanged =
        this.queueName !== computationalResourceScheduling.queueName;
      this.userConfigurationData.computationalResourceScheduling = computationalResourceScheduling;
      if (queueChanged) {
        this.initializeQueue();
      }
      this.emitValueChanged();
    },
    async loadApplicationDeployments() {
      this.applicationDeployments = await getApplicationDeployments(
        this.applicationModuleId,
        this.groupResourceProfileId
      );
    },
    async initializeGroupResourceProfileId() {
      this.userConfigurationData.groupResourceProfileId = await getDefaultGroupResourceProfileId();
    },
    async applyGroupResourceProfile() {
      // Make sure that resource host id is in the list of app deployments
      const computeResourceChanged = await this.initializeResourceHostId();
      if (computeResourceChanged) {
        await this.loadAppDeploymentQueues();
        this.setDefaultQueue();
      } else if (!this.queue) {
        // allowed queues may have changed. If selected queue isn't in the list
        // of allowed queues, reset to the default
        this.setDefaultQueue();
      } else {
        // reapply batchQueueResourcePolicy maximums since they may have changed
        this.applyBatchQueueResourcePolicy();
      }
    },
    async initializeResourceHostId() {
      // if there isn't a selected compute resource or there is but it isn't in
      // the list of app deployments, set a default one
      // Returns true if the resourceHostId changed
      if (
        !this.resourceHostId ||
        !this.computeResources.find((crid) => crid === this.resourceHostId)
      ) {
        this.userConfigurationData.computationalResourceScheduling.resourceHostId = await this.getDefaultResourceHostId();
        return true;
      }
      return false;
    },
    async loadAppDeploymentQueues() {
      const applicationDeployment = this.applicationDeployment;
      this.appDeploymentQueues = await getAppDeploymentQueues(
        applicationDeployment.appDeploymentId
      );
    },
    setDefaultQueue() {
      // set to the default queue or the first one
      const defaultQueue = this.getDefaultQueue();
      if (defaultQueue) {
        this.userConfigurationData.computationalResourceScheduling.queueName =
          defaultQueue.queueName;
      } else {
        this.userConfigurationData.computationalResourceScheduling.queueName = null;
      }
      this.initializeQueue();
    },
    isQueueInComputeResourcePolicy: function (queueName) {
      if (!this.computeResourcePolicy) {
        return true;
      }
      return this.computeResourcePolicy.allowedBatchQueues.includes(queueName);
    },
    initializeQueue() {
      const queue = this.queue;
      if (queue) {
        const crs = this.userConfigurationData.computationalResourceScheduling;
        crs.queueName = queue.queueName;
        crs.totalCPUCount = this.getDefaultCPUCount(queue);
        crs.nodeCount = this.getDefaultNodeCount(queue);
        crs.wallTimeLimit = this.getDefaultWalltime(queue);
        crs.totalPhysicalMemory = 0;
      } else {
        const crs = this.userConfigurationData.computationalResourceScheduling;
        crs.queueName = null;
        crs.totalCPUCount = 0;
        crs.nodeCount = 0;
        crs.wallTimeLimit = 0;
        crs.totalPhysicalMemory = 0;
      }
    },
    getDefaultQueue() {
      const defaultQueue = this.queues.find((q) => q.isDefaultQueue);
      if (defaultQueue) {
        return defaultQueue;
      } else if (this.queues.length > 0) {
        return this.queues[0];
      } else {
        return null;
      }
    },
    getDefaultCPUCount(queue) {
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedCores,
          queue.defaultCPUCount
        );
      }
      return queue.defaultCPUCount;
    },
    getDefaultNodeCount(queue) {
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedNodes,
          queue.defaultNodeCount
        );
      }
      return queue.defaultNodeCount;
    },
    getDefaultWalltime(queue) {
      const batchQueueResourcePolicy = this.batchQueueResourcePolicy;
      if (batchQueueResourcePolicy) {
        return Math.min(
          batchQueueResourcePolicy.maxAllowedWalltime,
          queue.defaultWalltime
        );
      }
      return queue.defaultWalltime;
    },
    applyBatchQueueResourcePolicy() {
      if (this.batchQueueResourcePolicy) {
        const crs = this.userConfigurationData.computationalResourceScheduling;
        crs.totalCPUCount = Math.min(
          crs.totalCPUCount,
          this.batchQueueResourcePolicy.maxAllowedCores
        );
        crs.nodeCount = Math.min(
          crs.nodeCount,
          this.batchQueueResourcePolicy.maxAllowedNodes
        );
        crs.wallTimeLimit = Math.min(
          crs.wallTimeLimit,
          this.batchQueueResourcePolicy.maxAllowedWalltime
        );
      }
    },
    cloneValue() {
      return this.value ? this.value.clone() : null;
    },
    async loadData() {
      if (this.groupResourceProfileId) {
        let groupResourceProfile = await this.loadGroupResourceProfile();
        // handle user no longer has access to GRP
        if (!groupResourceProfile) {
          await this.initializeGroupResourceProfileId();
          groupResourceProfile = await this.loadGroupResourceProfile();
        }
        if (groupResourceProfile) {
          await this.loadApplicationDeployments();
          await this.loadAppDeploymentQueues();
          await this.applyGroupResourceProfile();
          // If existing values are no longer selectable, the userConfigurationData
          // may have changed
          this.emitValueChanged();
        }
      } else {
        await this.initializeGroupResourceProfileId();
        if (this.groupResourceProfileId) {
          await this.loadGroupResourceProfile();
          await this.loadApplicationDeployments();
          await this.applyGroupResourceProfile();
          this.emitValueChanged();
        }
      }
    },
    async loadGroupResourceProfile() {
      this.groupResourceProfile = await getGroupResourceProfile(
        this.groupResourceProfileId
      );
      return this.groupResourceProfile;
    },
    async getDefaultResourceHostId() {
      const defaultComputeResourceId = await getDefaultComputeResourceId();
      if (
        defaultComputeResourceId &&
        this.computeResources.find((crid) => crid === defaultComputeResourceId)
      ) {
        return defaultComputeResourceId;
      } else if (this.computeResources.length > 0) {
        // Just pick the first one
        return this.computeResources[0];
      } else {
        return null;
      }
    },
    bindWebComponentProps() {
      this.$nextTick(() => {
        this.$refs.computeResourceSelector.computeResources = this.computeResources;
        this.$refs.computeResourceSelector.value = this.resourceHostId;
        this.$refs.queueSettingsEditor.value = this.userConfigurationData.computationalResourceScheduling;
        this.$refs.queueSettingsEditor.queues = this.queues;
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
    "userConfigurationData.computationalResourceScheduling":
      "bindWebComponentProps",
    queues: "bindWebComponentProps",
  },
};
</script>

<style>
@import "./styles.css";

:host {
  display: block;
  margin-bottom: 1rem;
}
</style>
