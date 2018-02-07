<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Job Submission Interfaces</h4>
      <div class="entry">
        <div class="heading">Add Job Submission</div>
        <select v-model="jobSubmission">
          <option value="0">Local</option>
          <option value="1">SSH</option>
          <option value="2">GLOBUS</option>
          <option value="3">UNICORE</option>
          <option value="4">Cloud</option>
        </select>
        <div class="submission-btn">
          <input type="button" class="deployment btn" value="Add Job Submission" v-on:click="addJobSubmission()"/>
        </div>
      </div>
    </div>
    <div class="new-application-tab-main">
      <tab-sub-section v-for="jobSubmission,index in data.jobSubmissionInterfaces" v-bind:key="index">
        <ssh-job-submission v-if="jobSubmission.jobSubmissionProtocol == 1"
                            v-bind:id="jobSubmission.jobSubmissionInterfaceId"></ssh-job-submission>
        <local-job-submission v-else-if="jobSubmission.jobSubmissionProtocol == 0"
                              v-bind:id="jobSubmission.jobSubmissionInterfaceId"></local-job-submission>
        <cloud-job-submission v-else-if="jobSubmission.jobSubmissionProtocol == 4"
                              v-bind:id="jobSubmission.jobSubmissionInterfaceId"></cloud-job-submission>
        <globus-job-submission v-else-if="jobSubmission.jobSubmissionProtocol == 2"
                               v-bind:id="jobSubmission.jobSubmissionInterfaceId"></globus-job-submission>
        <unicore-job-submission v-else-if="jobSubmission.jobSubmissionProtocol == 3"
                                v-bind:id="jobSubmission.jobSubmissionInterfaceId"></unicore-job-submission>
      </tab-sub-section>
    </div>
  </div>
</template>

<script>
  import tabMixin from '../../tabs/tab_mixin'
  import computeResourceTabMixin from './compute_resource_tab_mixin'

  import TabSubSection from '../../tabs/TabSubSection'
  import SshJobSubmission from './job_submission/SshJobSubmission'
  import LocalJobSubmission from './job_submission/LocalJobSubmission'
  import CloudJobSubmission from './job_submission/CloudJobSubmission'
  import UnicoreJobSubmission from './job_submission/UnicoreJobSubmission'
  import GlobusJobSubmission from './job_submission/GlobusJobSubmission'
  import {mapActions, mapGetters, mapMutations} from 'vuex'

  var counter = 0
  export default {
    name: "job-submission-interfaces-tab",
    components: {
      TabSubSection, SshJobSubmission, LocalJobSubmission, CloudJobSubmission, UnicoreJobSubmission, GlobusJobSubmission
    },
    mixins: [tabMixin, computeResourceTabMixin],
    data: function () {
      return {
        data: {},
        jobSubmission: null
      }
    },
    computed: {
      ...mapGetters({counter: 'computeResource/counter'})
    },
    methods: {
      addJobSubmission: function () {
        let id = this.counter()
        console.log("job submission Added", id)
        if (this.jobSubmission == 0) {
          this.addLocalJob(id)
        } else if (this.jobSubmission == 1) {
          this.addSshJob(id)
        } else if (this.jobSubmission == 2) {
          this.addGlobusJob(id)
        } else if (this.jobSubmission == 3) {
          this.addUnicoreJob(id)
        } else if (this.jobSubmission == 4) {
          this.addCloudJob(id)
        }
        if (this.jobSubmission) {
          this.addJobSubmissionStore({id: id, protocol: this.jobSubmission})
        }
      }
      ,
      ...
        mapMutations({
          addCloudJob: 'computeResource/cloudJobSubmission/addEmptyData',
          addGlobusJob: 'computeResource/globusJobSubmission/addEmptyData',
          addLocalJob: 'computeResource/localJobSubmission/addEmptyData',
          addSshJob: 'computeResource/sshJobSubmission/addEmptyData',
          addUnicoreJob: 'computeResource/unicoreJobSubmission/addEmptyData',
          addJobSubmissionStore: 'computeResource/addJobSubmission'
        }),
    }
  }
</script>

<style scoped>
  .submission-btn input {
    margin-top: 10px;
  }
</style>
