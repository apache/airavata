<template>
  <div>
    <div class="entry">
      <div class="heading">Maximum Allowed Nodes</div>
      <input v-model="data.maxAllowedNodes" type="number"/>
    </div>
    <div class="entry">
      <div class="heading">Maximum Allowed Cores</div>
      <input v-model="data.maxAllowedCores" type="text"/>
    </div>
    <div class="entry">
      <div class="heading">Maximum Allowed Wall Time</div>
      <input v-model="data.maxAllowedWalltime" type="text"/>
    </div>
  </div>
</template>

<script>
  import VModelMixin from '../../commons/vmodel_mixin'

  export default {
    name: "batch-queue-resource-policy",
    mixins: [VModelMixin],
    props:{
      batchQueues:{
        type:Array,
        default:[]
      }
    },
    mixins:[VModelMixin]
  }
</script>

<style scoped>

</style>
