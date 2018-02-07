<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Reporting</h4>
      <div class="entry">
        <boolean-radio-button v-bind:heading="'Enable Gateway Usage Reporting'"
                              v-bind:selectorId="0"
                              v-bind:def="data.gatewayUsageReporting"
                              v-on:bool_selector="boolValueHandler"></boolean-radio-button>
      </div>
      <div class="entry">
        <div class="heading">Gateway Module Load Command</div>
        <input type="text" v-model="data.gatewayUsageModuleLoadCommand"/>
      </div>
      <div class="entry">
        <div class="heading">Gateway Usage Executable</div>
        <input type="text" v-model="data.gatewayUsageExecutable"/>
      </div>
    </div>
  </div>
</template>

<script>
  import BooleanRadioButton from '../BooleanRadioButton'
  import tabMixin from '../../tabs/tab_mixin'
  import computeResourceTabMixin from './compute_resource_tab_mixin'

  export default {
    name: "reporting-tab",
    components: {
      BooleanRadioButton
    },
    mixins: [tabMixin, computeResourceTabMixin],
    methods: {
      boolValueHandler: function (selectorID, value) {
        this.data.gatewayUsageReporting = value
      }
    }
  }
</script>

<style scoped>

</style>
