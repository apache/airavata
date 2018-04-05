<template>
  <div class="batch-queue-resource-policy">
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
    mixins: [VModelMixin]
  }
</script>

<style scoped>
  .batch-queue-resource-policy {
    border: solid #dddddd 1px;
    border-radius: 4px;
    padding: 31px;
    width: 80%;
    font-size: small;
    margin-left: 25px;
    margin-bottom: 30px;
  }

  .batch-queue-resource-policy input {
    height: 30px;
  }
</style>
