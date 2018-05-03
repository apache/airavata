<template>
  <transition name="fade">
    <div>
      <div class="entry">
        <div class="heading">Batch Queues</div>
        <div v-for="batchQueue,index in batchQueues" v-bind:key="index">
          <input type="checkbox" v-model="batchQueues[index].selected"/>
          <label>{{batchQueue.name}}</label>
          <batch-queue-resource-policy v-if="resourcePolicies[index]"
                                       v-model="resourcePolicies[index]"></batch-queue-resource-policy>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
  import TabSubSection from '../../tabs/TabSubSection'
  import ComputeResourcePolicy from './ComputeResourcePolicy'
  import BatchQueueResourcePolicy from './BatchQueueResourcePolicy'
  import VModelMixin from '../../commons/vmodel_mixin'
  import DjangoAiravataAPI from 'django-airavata-api'

  import Vue from 'vue';

  export default {
    name: "compute-resource-policy",
    components: {
      TabSubSection,
      ComputeResourcePolicy,
      BatchQueueResourcePolicy
    },
    data: function () {
      return {
        resourcePolicies: [],
        batchQueues: [],
        defaultBatchQueueValues:[],
        data: this.value
      }
    },
    mounted: function () {
      this.fetchBatchQueues(this.data.computeResourceId);
    },
    mixins: [VModelMixin],
    methods: {
      fetchBatchQueues: function (computeResourceId) {
        if (computeResourceId !== null) {
          DjangoAiravataAPI.utils.FetchUtils.get("/api/compute/resource/details", {id: computeResourceId}).then((computeResource) => {
            let defaultBatchQueueValues=[];
            this.batchQueues = computeResource.batchQueues.map((batchQueue) => {
              defaultBatchQueueValues.push({
                maxAllowedCores:batchQueue.defaultCPUCount,
                maxAllowedNodes:batchQueue.defaultNodeCount,
                maxAllowedWalltime:batchQueue.defaultWalltime,
                queuename:batchQueue.queueName
              });
              this.defaultBatchQueueValues=defaultBatchQueueValues;
              return {
                name: batchQueue.queueName,
                selected: false,
                batchQueueResourcePolicy: null
              }
            });
            this.data.batchQueueResourcePolicies.forEach(value => {
              for (let index in queues) {
                let queue = queues[index];
                if (queue.name == value.queuename) {
                  queue.selected = true;
                  this.resourcePolicies[index] = value;
                  break;
                }
              }
            });
          });
        }
      },
      createBatchQueue: function (index) {
        if(index >=0){
          return this.defaultBatchQueueValues[index];
        }
        let batchQueuePreference = {
          maxAllowedNodes: null,
          maxAllowedCores: null,
          maxAllowedWalltime: null,
          queuename: queueName
        }
        return batchQueuePreference;
      },
      updateBatchQueues: function (data) {
        for (let index in  this.batchQueues) {
          let batchQueue = this.batchQueues[index];
          if (batchQueue.selected) {
            if (data.batchQueueResourcePolicies && !this.resourcePolicies[index]) {
              for (let val of data.batchQueueResourcePolicies) {
                if (val.queuename == batchQueue.name) {
                  Vue.set(this.resourcePolicies, index, val);
                }
              }
            }
            if (!this.resourcePolicies[index]) {
              Vue.set(this.resourcePolicies, index, this.createBatchQueue(index));
            }
            if (this.data.allowedBatchQueues.indexOf(batchQueue.name) < 0) {
              this.data.allowedBatchQueues.push(batchQueue.name)
            }
          } else if (this.resourcePolicies[index]) {
            Vue.delete(this.resourcePolicies, index);
            this.data.allowedBatchQueues = this.data.allowedBatchQueues.filter((batchQueueName) => batchQueueName != batchQueue.name);
          }
        }
      }
    },
    watch: {
      batchQueues: {
        handler: function (newValue) {
          this.updateBatchQueues(newValue);
        },
        deep: true
      },
      resourcePolicies: {
        handler: function (newValue) {
          let resourcePolicies = [];
          for (let prop in newValue) {
            resourcePolicies.push(this.resourcePolicies[prop]);
          }
          this.data.batchQueueResourcePolicies = resourcePolicies;
        },
        deep: true
      },
      'data.computeResourceId': function (newValue) {
        this.fetchBatchQueues(newValue);
      }
    }
  }
</script>

<style scoped>

</style>
