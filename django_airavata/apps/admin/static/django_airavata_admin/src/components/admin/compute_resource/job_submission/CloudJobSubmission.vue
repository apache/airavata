<template>
  <div>
    <div class="entry">
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
        <div class="heading">Node Id</div>
        <input type="text" v-model="data.nodeId"/>
      </div>
      <div class="entry">
        <div class="heading">Executable Type</div>
        <input type="text" v-model="data.executableType"/>
      </div>
      <div class="entry">
        <div class="heading">User Account Name</div>
        <input type="text" v-model="data.userAccountName"/>
      </div>
      <div class="entry">
        <div class="heading">Provider Name</div>
        <select v-model="data.providerName">
          <option value="0">EC2</option>
          <option value="1">AWSEC2</option>
          <option value="2">RACKSPACE</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script>
  import JobSubmissionMixin from './job_submission_mixin'
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/cloudJobSubmission')
  export default {
    name: "cloud-job-submission",
    mixins: [JobSubmissionMixin],
    methods: {
      ...mapActions(["save"]), ...mapMutations(["updateStore", "resetStore"])
    }, computed: {
      ...mapGetters({'storeData':"data"})
    }
  }
</script>

<style scoped>

</style>
