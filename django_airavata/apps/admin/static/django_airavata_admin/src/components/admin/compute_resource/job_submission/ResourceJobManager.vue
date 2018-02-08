<template>
  <div>
    <div class="entry">
      <div class="entry">
        <div class="heading">Resource Job Manager Type</div>
        <select v-model="data.resourceJobManagerType" v-bind:disabled="editable?'':'disabled'">
          <option value="0">FORK</option>
          <option value="1">PBS</option>
          <option value="2">SLURM</option>
          <option value="3">LSF</option>
          <option value="4">UGE</option>
          <option value="5">CLOUD</option>
          <option value="6">AIRAVATA_CUSTOM</option>
        </select>
      </div>
      <div class="entry">
        <div class="heading">Push Monitoring Endpoint</div>
        <input type="text" v-model="data.pushMonitoringEndpoint"/>
      </div>
      <div class="entry">
        <div class="heading">Job Manager Bin Path</div>
        <input type="text" v-model="data.jobManagerBinPath"/>
      </div>
      <div class="deployment-entry">
        <h4>Job Manager Command</h4>
        <div class="name_value" v-for="cmd in jobManagerCommands">
          <input type="text" placeholder="Name" v-model="cmd.name"/>
          <input type="text" placeholder="Value" v-model="cmd.value"/>
        </div>
        <input type="button" class="deployment btn" value="Add Command"
               v-on:click="jobManagerCommands.push({name:'',value:''})" v-if="editable"/>
      </div>
      <div class="deployment-entry">
        <h4>Parallelism Prefixes</h4>
        <div class="name_value" v-for="cmd in parallelismPrefix">
          <input type="text" placeholder="Name" v-model="cmd.name"/>
          <input type="text" placeholder="Value" v-model="cmd.value"/>
        </div>
        <input type="button" class="deployment btn" value="Add Command"
               v-on:click="parallelismPrefix.push({name:'',value:''})" v-if="editable"/>
      </div>
    </div>
  </div>
</template>

<script>
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters} = createNamespacedHelpers('computeResource')
  export default {
    name: "resource-job-manager",
    data: function () {
      return {
        jobManagerCommands: [],
        parallelismPrefix: []
      }
    },
    mounted: function () {

    },
    props: {
      data: {
        type: Object
      },
      updateData: {
        type: Function
      },
      id: {
        default: null
      }
    },
    beforeDestroy: function () {
      let reducer = (accumulator, currentValue) => {
        accumulator[currentValue.name] = currentValue.value;
        return accumulator;
      }
      this.data.jobManagerCommands = this.jobManagerCommands.reduce(reducer, {});
      this.data.parallelismPrefix = this.parallelismPrefix.reduce(reducer, {});
      console.log(this.data);
      this.updateData({data: this.data, id: this.id})
    }, computed: {
      ...mapGetters({
        editable: 'editable',
      })
    },
    watch: {
      data: function (newValue) {
        console.log("hello")
        console.log("Before mount", Object.keys(this.data.jobManagerCommands))
        this.jobManagerCommands = Object.keys(this.data.jobManagerCommands).map((key) => {
          return {name: key, value: this.data.jobManagerCommands[key]}
        })
        this.parallelismPrefix = Object.keys(this.data.parallelismPrefix).map((key) => {
          return {name: key, value: this.data.parallelismPrefix[key]}
        })
      }
    }
  }
</script>

<style scoped>

</style>
