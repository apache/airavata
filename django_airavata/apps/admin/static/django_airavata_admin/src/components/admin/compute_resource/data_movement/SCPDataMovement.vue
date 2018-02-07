<template>
  <div>
    <div class="entry">
      <div class="heading">Select Security Protocol</div>
      <select v-model="data.securityProtocol">
        <option value="0">USERNAME_PASSWORD</option>
        <option value="1">SSH_KEYS</option>
        <option value="2">GSI</option>
        <option value="3">KERBEROS</option>
        <option value="4">OAUTH</option>
        <option value="5">LOCAL</option>
      </select>
    </div>
    <div class="entry">
      <div class="heading">Alternative Host Name</div>
      <input type="text" v-model="data.alternativeSCPHostName"/>
    </div>
    <div class="entry">
      <div class="heading">SSH Port</div>
      <input type="number" v-model="data.sshPort"/>
    </div>
  </div>
</template>

<script>
  import {createNamespacedHelpers} from 'vuex'
  import dataMovementMixin from './data_movement_mixin'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/dataMovement')
  export default {
    name: "s-c-p-data-movement",
    mixins: [dataMovementMixin],
    computed: {
      ...mapGetters({
        storeData: 'scpData'
      })
    },
    methods: {
      ...mapMutations({
        updateStore: 'updateSCP'
      })
    }
  }
</script>

<style scoped>

</style>
