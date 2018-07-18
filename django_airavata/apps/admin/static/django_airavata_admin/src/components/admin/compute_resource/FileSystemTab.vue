<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>File System</h4>
      <div class="entry">
        <div class="heading">HOME</div>
        <input type="text" v-model="data.fileSystems[0]"/>
      </div>
      <div class="entry">
        <div class="heading">WORK</div>
        <input type="text" v-model="data.fileSystems[1]"/>
      </div>
      <div class="entry">
        <div class="heading">LOCALTMP</div>
        <input type="text" v-model="data.fileSystems[2]"/>
      </div>
      <div class="entry">
        <div class="heading">SCRATCH</div>
        <input type="text" v-model="data.fileSystems[3]"/>
      </div>
      <div class="entry">
        <div class="heading">ARCHIVE</div>
        <input type="text" v-model="data.fileSystems[4]"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <tab-action-console v-if="editable" v-bind:save="save" v-bind:cancel="cancel"
                          v-bind:sectionName="'File Systems'"></tab-action-console>
    </div>
  </div>
</template>

<script>
  import tabMixin from '../../tabs/tab_mixin'
  import computeResourceTabMixin from './compute_resource_tab_mixin'
  import TabActionConsole from '../TabActionConsole'

  export default {
    name: "file-system-tab",
    components: {TabActionConsole},
    mixins: [tabMixin, computeResourceTabMixin],
    data: function () {
      return {
        fields: [
          "fileSystems",
        ]
      }
    }
  }
</script>

<style scoped>

</style>
