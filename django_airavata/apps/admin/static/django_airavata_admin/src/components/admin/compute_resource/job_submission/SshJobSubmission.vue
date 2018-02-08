<template>
  <div>
    <div class="entry">
      <div class="entry">
        <div class="heading">Select Security Protocol</div>
        <select v-model="data.securityProtocol"  v-bind:disabled="editable?'':'disabled'">
          <option value="0">USERNAME_PASSWORD</option>
          <option value="1">SSH_KEYS</option>
          <option value="2">GSI</option>
          <option value="3">KERBEROS</option>
          <option value="4">OAUTH</option>
          <option value="5">LOCAL</option>
        </select>
      </div>
      <div class="entry">
        <div class="heading">Alternate SSH Host Name</div>
        <input type="text" v-model="data.alternativeSSHHostName"/>
      </div>
      <div class="entry">
        <div class="heading">SSH Port</div>
        <input type="number" v-model="data.sshPort "/>
      </div>
      <div class="entry">
        <div class="heading">Monitor Mode</div>
        <select v-model="data.monitorMode"  v-bind:disabled="editable?'':'disabled'">
          <option value="0">POLL_JOB_MANAGER</option>
          <option value="1">CLOUD_JOB_MONITOR</option>
          <option value="2">JOB_EMAIL_NOTIFICATION_MONITOR</option>
          <option value="3">XSEDE_AMQP_SUBSCRIBE</option>
          <option value="4">FORK</option>
          <option value="5">LOCAL</option>
        </select>
      </div>
      <resource-job-manager v-bind:updateData="updateResourceJobManager"
                            v-bind:data="data.resourceJobManager" v-bind:id="id"></resource-job-manager>
    </div>
  </div>
</template>


<script>
  import ResourceJobManager from './ResourceJobManager'

  import JobSubmissionMixin from './job_submission_mixin'
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/sshJobSubmission')
  export default {
    name: "ssh-job-submission",
    mixins: [JobSubmissionMixin],
    components: {ResourceJobManager},
    methods: {
      ...mapActions(["save"]), ...mapMutations(["updateStore", "resetStore", "updateResourceJobManager"])
    }, computed: {
      ...mapGetters({'storeData':"data"})
    }
  }
</script>

<style scoped>

</style>
