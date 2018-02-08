<template>
  <div>
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
    <resource-job-manager v-bind:updateData="updateResourceJobManager"
                          v-bind:data="data.resourceJobManager" v-bind:id="id"></resource-job-manager>
  </div>
</template>


<script>
  import JobSubmissionMixin from './job_submission_mixin'
  import {createNamespacedHelpers} from 'vuex'
  import ResourceJobManager from './ResourceJobManager'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/localJobSubmission')
  export default {
    name: "local-job-submission",
    mixins: [JobSubmissionMixin],
    components: {ResourceJobManager},
    methods: {
      ...mapActions(["save"]), ...mapMutations(["updateStore", "resetStore", "updateResourceJobManager"])
    }, computed: {
      ...mapGetters({'storeData': "data"})
    }
  }
</script>

<style scoped>

</style>
