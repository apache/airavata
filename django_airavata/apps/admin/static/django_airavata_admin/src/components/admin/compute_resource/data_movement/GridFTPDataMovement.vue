<template>
  <div>
    <div class="entry">
      <div class="heading">Select Security Protocol</div>
      <select v-model="data.securityProtocol" v-bind:disabled="editable?'':'disabled'">
        <option value="0">USERNAME_PASSWORD</option>
        <option value="1">SSH_KEYS</option>
        <option value="2">GSI</option>
        <option value="3">KERBEROS</option>
        <option value="4">OAUTH</option>
        <option value="5">LOCAL</option>
      </select>
    </div>
    <div class="new-application-tab-main">
      <div class="entry">
        <div class="heading">GridFTP Endpoints</div>
        <div class="entry" v-for="endpoint,index in data.gridFTPEndPoints">
          <input type="text" v-model="data.gridFTPEndPoints[index]"/>
        </div>
      </div>
      <div class="deployment-entry" v-if="editable">
        <input type="button" class="deployment btn" value="Add Endpoint"
               v-on:click="data.gridFTPEndPoints.push('')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <tab-action-console v-if="editable" v-bind:save="save" v-bind:cancel="cancel"
                          v-bind:sectionName="'Queues'" v-bind:enableCancel="false"></tab-action-console>
    </div>
  </div>
</template>

<script>
  import {createNamespacedHelpers} from 'vuex'
  import dataMovementMixin from './data_movement_mixin'
  import TabActionConsole from '../../TabActionConsole'

  const {mapGetters, mapMutations} = createNamespacedHelpers('computeResource/dataMovement')

  export default {
    name: "grid-f-t-p-data-movement",
    mixins: [dataMovementMixin],
    components: {TabActionConsole},
    computed: {
      ...mapGetters({
        storeData: 'gridData'
      })
    },
    methods: {
      ...mapMutations({
        updateData: 'updateGrid'
      })
    }
  }
</script>

<style scoped>

</style>
