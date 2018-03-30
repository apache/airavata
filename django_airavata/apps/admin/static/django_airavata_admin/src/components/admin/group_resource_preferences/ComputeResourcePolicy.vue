<template>
  <transition name="fade">
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
  import Vue from 'vue'
  export default {
    name: "compute-resource-policy",
    components: {
      TabSubSection,
      ComputeResourcePolicy,
      BatchQueueResourcePolicy
    },
    data: function () {
      let batchQueues = this.fetchBatchQueues(this.value);
      return {
        batchQueues: batchQueues.queues,
        resourcePolicies: batchQueues.resourcePolicies,
        data: this.value
      }
    },
    mixins: [VModelMixin],
    methods: {
      fetchBatchQueues: function (data) {
        let queues = [{
          name: "cpu",
          selected: false,
          batchQueueResourcePolicy: null
        }, {
          name: "gpu",
          selected: false,
          batchQueueResourcePolicy: null
        }];
        let resourcePolicies = {}
        data.batchQueueResourcePolicies.forEach(value => {
          for (let index in queues) {
            let queue = queues[index];
            if (queue.name == value.queuename) {
              queue.selected = true;
              resourcePolicies[index] = value;
              break;
            }
          }
        })
        return {
          queues: queues,
          resourcePolicies: resourcePolicies
        };
      },
      createBatchQueue: function (queueName) {
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
                  Vue.set(this.resourcePolicies,index,val);
                }
              }
            }
            if (!this.resourcePolicies[index]) {
              Vue.set(this.resourcePolicies,index,this.createBatchQueue(batchQueue.name));
            }
          } else if (this.resourcePolicies[index]) {
            Vue.delete(this.resourcePolicies,index);
          }
        }
      }
    },
    watch: {
      batchQueues: {
        handler: function (newValue) {
          console.log("watch", newValue);
          this.updateBatchQueues(newValue);
        },
        deep: true
      },
      resourcePolicies:{
        handler:function (newValue) {
          console.log("Resource policies",newValue);
          let resourcePolicies=[];
          for(let prop in newValue){
            resourcePolicies.push(this.resourcePolicies[prop]);
            console.log(prop);
          }
          this.data.batchQueueResourcePolicies=resourcePolicies;
        },
        deep:true
      }
    }
  }
</script>

<style scoped>

</style>
