<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Description</h4>
      <div class="entry">
        <div class="heading">Host Name</div>
        <input type="text" v-model="data.hostName"/>
      </div>
      <div class="entry">
        <div class="heading">Host Aliases</div>
        <div class="entry" v-for="hostAlias,index in data.hostAliases">
          <input type="text" v-model="data.hostAliases[index]"/>
        </div>
      </div>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" v-if="editable" value="Add Aliases"
               v-on:click="data.hostAliases.push('')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="entry">
        <div class="heading">IP Addresses</div>
        <div class="entry" v-for="ipAddress,index in data.ipAddresses">
          <input type="text" v-model="data.ipAddresses[index]"/>
        </div>
      </div>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" v-if="editable" value="Add IP Addresses"
               v-on:click="data.ipAddresses.push('')"/>
      </div>
    </div>

    <div class="new-application-tab-main">
      <div class="entry">
        <div class="heading">Resource Description</div>
        <textarea style="height: 80px;" type="text" v-model="data.resourceDescription"/>
      </div>
      <div class="entry">
        <div class="heading">Maximum Memory Per Node ( In MB )</div>
        <input type="number" value="1" min="0" v-model="data.maxMemoryPerNode"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <tab-action-console v-if="editable" v-bind:save="save" v-bind:cancel="cancel"
                          v-bind:sectionName="'Description'"></tab-action-console>
    </div>
  </div>
</template>

<script>
  import tabMixin from '../../tabs/tab_mixin'
  import computeResourceTabMixin from './compute_resource_tab_mixin'
  import TabActionConsole from '../TabActionConsole'


  export default {
    components: {TabActionConsole},
    name: "description-tab",
    mixins: [tabMixin, computeResourceTabMixin],
    data: function () {
      return {
        fields: [
          "hostName",
          'hostAliases',
          'ipAddresses',
          'resourceDescription',
          'maxMemoryPerNode',
        ]
      }
    }

  }
</script>

<style scoped>
  .heading {
    font-size: 1.0em;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .name_value {
    display: inline-flex;
    width: 100%;
    margin-bottom: 5px;
  }

  .name_value input {
    width: 50%;
    display: inline-flex;
    margin-right: 5px;
  }
</style>
