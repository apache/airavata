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
    <div class="entry">
      <div class="heading">Unicore Endpoint URL</div>
      <input type="text" v-model="data.unicoreEndPointURL"/>
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

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/dataMovement')

  export default {
    name: "unicore-data-movement",
    mixins: [dataMovementMixin],
    components: {TabActionConsole},
    computed: {
      ...mapGetters({
        storeData: 'unicoreData'
      })
    },
    methods: {
      ...mapMutations({
        updateData: 'updateUnicore'
      })
    }
  }
</script>

<style scoped>

</style>
