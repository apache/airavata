<template>
  <div>
    <div class="entry">
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
        <div class="heading">GLOBUS Entry Points</div>
        <div class="entry" v-for="entryPoint,index in data.globusGateKeeperEndPoint">
          <input type="text" v-model="data.globusGateKeeperEndPoint[index]"/>
        </div>
        <div class="deployment-entry">
          <input type="button" class="deployment btn" v-if="view" value="Add Entry Point"
                 v-on:click="data.globusGateKeeperEndPoint.push('')"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import JobSubmissionMixin from './job_submission_mixin'
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/globusJobSubmission')
  export default {
    name: "globus-job-submission",
    mixins: [JobSubmissionMixin],
    methods: {
      ...mapActions(["save"]), ...mapMutations(["updateStore", "resetStore"])
    }, computed: {
      ...mapGetters({'storeData': "data"})
    }
  }
</script>

<style scoped>

</style>
