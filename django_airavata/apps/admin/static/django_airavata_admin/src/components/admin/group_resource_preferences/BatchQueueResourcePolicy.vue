<template>
  <div class="row">
    <div class="col">
      <b-form-group label="Maximum Allowed Nodes" label-for="max-allowed-nodes">
        <b-form-input id="max-allowed-nodes" type="number"
          v-model="data.maxAllowedNodes" @input="policyUpdated"
          min="1" :max="batchQueue.maxNodes"
          :placeholder="'Max Nodes: ' + batchQueue.maxNodes">
        </b-form-input>
      </b-form-group>
    </div>
    <div class="col">
      <b-form-group label="Maximum Allowed Cores" label-for="max-allowed-cores">
        <b-form-input id="max-allowed-cores" type="number"
          v-model="data.maxAllowedCores" @input="policyUpdated"
          min="1" :max="batchQueue.maxProcessors"
          :placeholder="'Max Cores: ' + batchQueue.maxProcessors">
        </b-form-input>
      </b-form-group>
    </div>
    <div class="col">
      <b-form-group label="Maximum Allowed Wall Time" label-for="max-allowed-walltime">
        <b-form-input id="max-allowed-walltime" type="number"
          v-model="data.maxAllowedWalltime" @input="policyUpdated"
          min="1" :max="batchQueue.maxRunTime"
          :placeholder="'Max Wall Time: ' + batchQueue.maxRunTime">
        </b-form-input>
      </b-form-group>
    </div>
</template>

<script>
  import { models } from 'django-airavata-api'

  export default {
    name: "batch-queue-resource-policy",
    props: {
      value: {
        required: false,
        type: models.BatchQueueResourcePolicy
      },
      batchQueue: {
        required: true,
        type: models.BatchQueue,
      },
    },
    data: function() {
      const localValue = this.value ? this.value.clone() : new models.BatchQueueResourcePolicy();
      localValue.queuename = this.batchQueue.queueName;
      return {
        data: localValue,
      }
    },
    methods: {
      policyUpdated: function() {
        if (this.data.maxAllowedNodes || this.data.maxAllowedCores || this.data.maxAllowedWalltime) {
          this.$emit('input', this.data);
        } else {
          this.$emit('input', null);
        }
      }
    }
  }
</script>