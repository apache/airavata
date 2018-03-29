<template>
  <div>
    <div class="new-application-tab-main">
      <h5>Allowed Batch Queues</h5>
      <div class="entry">
        <div class="heading">Batch Queues</div>
        <div class="entry" v-for="batchQueue,index in data.allowedBatchQueues">
          <input type="text" v-model="data.allowedBatchQueues[index]"/>
        </div>
      </div>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" value="Add Batch Queue"
               v-on:click="data.allowedBatchQueues.push('')"/>
      </div>
    </div>
    <div class="entry">
      <div class="heading">Batch Queues</div>
      <div v-for="batchQueue,index in batchQueues" v-bind:key="index">
        <input type="checkbox" v-model="batchQueues[index].selected"/>
        <label>{{batchQueue.name}}</label>
        <batch-queue-resource-policy v-if="batchQueues[index].batchQueueResourcePolicy"
                                     v-model="batchQueues[index].batchQueueResourcePolicy"></batch-queue-resource-policy>
      </div>
    </div>
  </div>
</template>

<script>
  import TabSubSection from '../../tabs/TabSubSection'
  import ComputeResourcePolicy from './ComputeResourcePolicy'
  import BatchQueueResourcePolicy from './BatchQueueResourcePolicy'
  import VModelMixin from '../../commons/vmodel_mixin'

  export default {
    name: "compute-resource-policy",
    components: {
      TabSubSection,
      ComputeResourcePolicy,
      BatchQueueResourcePolicy
    },
    data: function () {
      return {
        batchQueues: this.fetchBatchQueues(),
      }
    },
    mixins: [VModelMixin],
    methods: {
      fetchBatchQueues: function () {
        let queues = [{
          name: "cpu",
          selected: false
        }, {
          name: "gpu",
          selected: false
        }];
        queues.forEach(value => {
          for (queue in data.batchQueueResourcePolicies) {
            if (value.name == queue.queuename) {
              value.queue = queue;
              break;
            }
          }
        })
        return queues;
      },
      createBatchQueue: function (queueName) {
        let batchQueuePreference = {
          maxAllowedNodes: null,
          maxAllowedCores: null,
          maxAllowedWalltime: null,
          queuename: queueName
        }
        this.data.batchQueueResourcePolicies.push(batchQueuePreference);
      }
    },
    watch: {
      batchQueues: function (newValue, oldValue) {
        for (let batchQueue in  this.batchQueues) {
          if (batchQueue.selected) {
            if (this.data.batchQueueResourcePolicies) {
              for (let val in this.data.batchQueueResourcePolicies) {
                if (val.queuename == batchQueue.name) {
                  batchQueue.batchQueueResourcePolicy = val;
                }
              }
            }
            if (!batchQueue.batchQueueResourcePolicy) {
              batchQueue.batchQueueResourcePolicy = this.createBatchQueue(batchQueue.name);
            }
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
