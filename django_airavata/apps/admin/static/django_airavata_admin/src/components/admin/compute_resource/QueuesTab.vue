<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Queues</h4>
      <tab-sub-section v-for="batchQueue,index in data.batchQueues" v-bind:key="index">
        <div class="entry">
          <div class="heading">Queue Name</div>
          <input type="text" v-model="data.batchQueues[index].queueName"/>
        </div>
        <div class="entry">
          <div class="heading">Queue Description</div>
          <textarea v-model="data.batchQueues[index].queueDescription"/>
        </div>
        <div class="entry">
          <div class="heading">Queue Max Run Time (In Minutes)</div>
          <input type="number" v-model="data.batchQueues[index].maxRunTime"/>
        </div>
        <div class="entry">
          <div class="heading">Queue Max Nodes</div>
          <input type="number" v-model="data.batchQueues[index].maxNodes"/>
        </div>
        <div class="entry">
          <div class="heading">Queue Max Processors</div>
          <input type="number" v-model="data.batchQueues[index].maxProcessors"/>
        </div>
        <div class="entry">
          <div class="heading">Max Jobs in Queue</div>
          <input type="number" v-model="data.batchQueues[index].maxJobsInQueue"/>
        </div>
        <div class="entry">
          <div class="heading">Max Memory For Queue( In MB )</div>
          <input type="number" v-model="data.batchQueues[index].maxMemory"/>
        </div>
        <div class="entry">
          <div class="heading">CPUs Per Node</div>
          <input type="number" v-model="data.batchQueues[index].cpuPerNode"/>
        </div>
        <div class="entry">
          <div class="heading">Default Node Count</div>
          <input type="number" v-model="data.batchQueues[index].defaultNodeCount"/>
        </div>
        <div class="entry">
          <div class="heading">Default CPU Count</div>
          <input type="number" v-model="data.batchQueues[index].defaultCPUCount"/>
        </div>
        <div class="entry">
          <div class="heading">Default Wall Time</div>
          <input type="number" v-model="data.batchQueues[index].defaultWalltime"/>
        </div>
        <div class="entry">
          <div class="heading">Queue Specific Macros</div>
          <input type="text" v-model="data.batchQueues[index].queueSpecificMacros"/>
        </div>
        <div class="entry">
          <boolean-radio-button v-bind:heading="'Default Queue for the Resource'" v-bind:selectorId="index"
                                v-bind:def="data.batchQueues[index].isDefaultQueue"
                                v-on:bool_selector="boolValueHandler"></boolean-radio-button>
        </div>
      </tab-sub-section>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" v-if="editable" value="Add Queue"
               v-on:click="data.batchQueues.push(createBatchQueue)"/>
      </div>
    </div>
     <div class="new-application-tab-main">
      <tab-action-console v-if="editable" v-bind:save="save" v-bind:cancel="cancel"
                          v-bind:sectionName="'Queues'"></tab-action-console>
    </div>
  </div>
</template>

<script>
  import tabMixin from '../../tabs/tab_mixin'
  import computeResourceTabMixin from './compute_resource_tab_mixin'
  import TabActionConsole from '../TabActionConsole'
  import TabSubSection from '../../tabs/TabSubSection'
  import BooleanRadioButton from '../BooleanRadioButton'

  export default {
    name: "queues-tab",
    mixins: [tabMixin, computeResourceTabMixin],
    components: {
      TabSubSection, TabActionConsole, BooleanRadioButton
    },
    data:function () {
      return {
        fields:['batchQueues']
      }
    },
    methods: {
      boolValueHandler: function (selectorID, value) {
        this.data.batchQueues[selectorID].isDefaultQueue = value
      }
    }
  }
</script>

<style scoped>

</style>
