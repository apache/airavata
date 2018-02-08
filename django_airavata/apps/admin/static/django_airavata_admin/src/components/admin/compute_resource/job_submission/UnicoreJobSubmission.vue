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
        <div class="heading">Endpoint URL</div>
        <input type="text" v-model="data.unicoreEndPointURL"/>
      </div>
    </div>
  </div>
</template>


<script>
  import JobSubmissionMixin from './job_submission_mixin'
  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions, mapMutations} = createNamespacedHelpers('computeResource/unicoreJobSubmission')
  export default {
    name: "unicore-job-submission",
    mixins: [JobSubmissionMixin],
    methods: {
      ...mapActions(["save"]), ...mapMutations(["updateStore", "resetStore"])
    },  computed: {
      ...mapGetters({'storeData':"data"})
    }
  }
</script>

<style scoped>

</style>
