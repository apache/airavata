<template>
  <ul>
    <li v-for="queuePolicy in queuePolicies" :key="queuePolicy.name">
      {{ queuePolicy.name }}
      <template v-if="queuePolicy.policy">
        (<span title="Max Allowed Nodes"
          >N:
          {{
            queuePolicy.policy.maxAllowedNodes
              ? queuePolicy.policy.maxAllowedNodes
              : "Unlimited"
          }}</span
        >,
        <span title="Max Allowed Cores"
          >C:
          {{
            queuePolicy.policy.maxAllowedCores
              ? queuePolicy.policy.maxAllowedCores
              : "Unlimited"
          }}</span
        >,
        <span title="Max Allowed Walltime"
          >W:
          {{
            queuePolicy.policy.maxAllowedWalltime
              ? queuePolicy.policy.maxAllowedWalltime
              : "Unlimited"
          }}</span
        >)
      </template>
    </li>
  </ul>
</template>

<script>
import { models } from "django-airavata-api";

export default {
  name: "compute-resource-policy-summary",
  props: {
    computeResourceId: {
      type: String,
      required: true,
    },
    groupResourceProfile: {
      type: models.GroupResourceProfile,
    },
  },
  computed: {
    queues: function () {
      const computeResourcePolicy = this.groupResourceProfile.getComputeResourcePolicy(
        this.computeResourceId
      );
      if (computeResourcePolicy && computeResourcePolicy.allowedBatchQueues) {
        const queues = computeResourcePolicy.allowedBatchQueues.slice();
        queues.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
        return queues;
      } else {
        return [];
      }
    },
    queuePolicies: function () {
      const result = [];
      for (const queue of this.queues) {
        const batchQueueResourcePolicies = this.groupResourceProfile.getBatchQueueResourcePolicies(
          this.computeResourceId
        );
        const batchQueueResourcePolicy = batchQueueResourcePolicies.find(
          (pol) => pol.queuename === queue
        );
        result.push({
          name: queue,
          policy: batchQueueResourcePolicy,
        });
      }
      return result;
    },
  },
};
</script>

<style scoped>
ul {
  padding-left: 20px;
}
</style>
