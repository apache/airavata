<template>
  <b-form-group
    label="Allowed Queues"
    v-if="localComputeResourcePolicy"
    :invalid-feedback="validationFeedback.allowedBatchQueues.invalidFeedback"
    :state="validationFeedback.allowedBatchQueues.state"
  >
    <div v-for="batchQueue in batchQueues" :key="batchQueue.queueName">
      <b-form-checkbox
        :checked="
          localComputeResourcePolicy.allowedBatchQueues.includes(
            batchQueue.queueName
          )
        "
        :disabled="readonly"
        @input="batchQueueChecked(batchQueue, $event)"
      >
        {{ batchQueue.queueName }}
      </b-form-checkbox>
      <batch-queue-resource-policy
        v-if="
          localComputeResourcePolicy.allowedBatchQueues.includes(
            batchQueue.queueName
          )
        "
        :batch-queue="batchQueue"
        :readonly="readonly"
        :value="
          localBatchQueueResourcePolicies.find(
            (pol) => pol.queuename === batchQueue.queueName
          )
        "
        @input="updatedBatchQueueResourcePolicy(batchQueue, $event)"
        @valid="recordValidBatchQueueResourcePolicy(batchQueue)"
        @invalid="recordInvalidBatchQueueResourcePolicy(batchQueue)"
      />
    </div>
  </b-form-group>
</template>

<script>
import BatchQueueResourcePolicy from "./BatchQueueResourcePolicy.vue";

import { models } from "django-airavata-api";
import { errors } from "django-airavata-common-ui";

export default {
  name: "compute-resource-policy-editor",
  props: {
    batchQueues: {
      type: Array,
    },
    computeResourcePolicy: {
      type: models.ComputeResourcePolicy,
    },
    batchQueueResourcePolicies: {
      type: Array,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    BatchQueueResourcePolicy,
  },
  data: function () {
    return {
      localComputeResourcePolicy: this.computeResourcePolicy
        ? this.computeResourcePolicy.clone()
        : null,
      localBatchQueueResourcePolicies: this.batchQueueResourcePolicies
        ? this.batchQueueResourcePolicies.map((pol) => pol.clone())
        : [],
      validationErrors: null,
      invalidBatchQueueResourcePolicies: [],
    };
  },
  computed: {
    computeResourcePolicyValidation() {
      return this.localComputeResourcePolicy.validate();
    },
    validationFeedback() {
      return errors.ValidationErrors.createValidationFeedback(
        this.localComputeResourcePolicy,
        this.computeResourcePolicyValidation
      );
    },
    valid() {
      return (
        this.allowedInvalidBatchQueueResourcePolicies.length === 0 &&
        Object.keys(this.computeResourcePolicyValidation).length === 0
      );
    },
    allowedInvalidBatchQueueResourcePolicies() {
      return this.invalidBatchQueueResourcePolicies.filter((queueName) =>
        this.localComputeResourcePolicy.allowedBatchQueues.includes(queueName)
      );
    },
  },
  methods: {
    batchQueueChecked: function (batchQueue, checked) {
      if (checked) {
        this.localComputeResourcePolicy.allowedBatchQueues.push(
          batchQueue.queueName
        );
      } else {
        const queueIndex = this.localComputeResourcePolicy.allowedBatchQueues.indexOf(
          batchQueue.queueName
        );
        this.localComputeResourcePolicy.allowedBatchQueues.splice(
          queueIndex,
          1
        );
        // Remove batchQueueResourcePolicy if it exists
        const policyIndex = this.localBatchQueueResourcePolicies.findIndex(
          (pol) => pol.queuename === batchQueue.queueName
        );
        if (policyIndex >= 0) {
          this.localBatchQueueResourcePolicies.splice(policyIndex, 1);
        }
        this.$emit(
          "batch-queue-resource-policies-updated",
          this.localBatchQueueResourcePolicies
        );
      }
      this.validate();
      this.$emit(
        "compute-resource-policy-updated",
        this.localComputeResourcePolicy
      );
    },
    updatedBatchQueueResourcePolicy: function (
      batchQueue,
      batchQueueResourcePolicy
    ) {
      const queueName = batchQueue.queueName;
      if (batchQueueResourcePolicy) {
        const existingPolicy = this.localBatchQueueResourcePolicies.find(
          (pol) => pol.queuename === queueName
        );
        if (existingPolicy) {
          Object.assign(existingPolicy, batchQueueResourcePolicy);
        } else {
          this.localComputeResourcePolicy.populateParentIdsOnBatchQueueResourcePolicy(
            batchQueueResourcePolicy
          );
          this.localBatchQueueResourcePolicies.push(batchQueueResourcePolicy);
        }
      } else {
        const existingPolicyIndex = this.localBatchQueueResourcePolicies.findIndex(
          (pol) => pol.queuename === queueName
        );
        if (existingPolicyIndex >= 0) {
          this.localBatchQueueResourcePolicies.splice(existingPolicyIndex, 1);
        }
      }
      this.$emit(
        "batch-queue-resource-policies-updated",
        this.localBatchQueueResourcePolicies
      );
    },
    recordValidBatchQueueResourcePolicy(batchQueue) {
      if (
        this.invalidBatchQueueResourcePolicies.includes(batchQueue.queueName)
      ) {
        const index = this.invalidBatchQueueResourcePolicies.indexOf(
          batchQueue.queueName
        );
        this.invalidBatchQueueResourcePolicies.splice(index, 1);
      }
      this.validate(); // propagate validation
    },
    recordInvalidBatchQueueResourcePolicy(batchQueue) {
      if (
        !this.invalidBatchQueueResourcePolicies.includes(batchQueue.queueName)
      ) {
        this.invalidBatchQueueResourcePolicies.push(batchQueue.queueName);
      }
      this.validate(); // propagate validation
    },
    validate() {
      if (this.valid) {
        this.$emit("valid");
      } else {
        this.$emit("invalid");
      }
    },
  },
  watch: {
    computeResourcePolicy(value) {
      this.localComputeResourcePolicy = value.clone();
    },
    batchQueueResourcePolicies(value) {
      this.localBatchQueueResourcePolicies = value
        ? value.map((p) => p.clone())
        : [];
    },
  },
};
</script>

<style></style>
