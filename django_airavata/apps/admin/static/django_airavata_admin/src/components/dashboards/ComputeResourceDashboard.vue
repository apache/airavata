<template>
  <div>
    <tabbed-view v-bind:tabNames="tabNames" v-bind:defaultActiveTab="currentActiveTab" v-on:tabchange="tabEventHandler">
      <description-tab v-if="currentActiveTab==0"></description-tab>
      <queues-tab v-if="currentActiveTab==1"></queues-tab>
      <file-system-tab v-if="currentActiveTab==2"></file-system-tab>
      <job-submission-interfaces-tab  v-if="currentActiveTab==3"></job-submission-interfaces-tab>
      <data-movement-interfaces-tab  v-if="currentActiveTab==4"></data-movement-interfaces-tab>
      <reporting-tab  v-if="currentActiveTab==5"></reporting-tab>
    </tabbed-view>
  </div>
</template>

<script>
  import TabbedView from '../tabs/TabbedView'
  import DescriptionTab from '../admin/compute_resource/DescriptionTab'
  import QueuesTab from '../admin/compute_resource/QueuesTab'
  import FileSystemTab from '../admin/compute_resource/FileSystemTab'
  import tabbedViewMixin from '../tabs/tabbed_view_mixin'
  import JobSubmissionInterfacesTab from "../admin/compute_resource/JobSubmissionInterfacesTab";
  import DataMovementInterfacesTab from "../admin/compute_resource/DataMovementInterfacesTab";
  import ReportingTab from "../admin/compute_resource/ReportingTab";

  export default {
    name: "compute-resource-dashboard",
    components: {
      ReportingTab,
      DataMovementInterfacesTab,
      JobSubmissionInterfacesTab,
      TabbedView, DescriptionTab, QueuesTab, FileSystemTab
    },
    mixins: [tabbedViewMixin]
    ,
    data: function () {
      return {
        'tabs': [{
          name: "Description",
          id: 0,
        }, {
          name: "Queues",
          id: 1,
        }, {
          name: "File Systems",
          id: 2
        },{
          name:"Job Submission Interfaces",
          id:3
        },
        {
          name:"Data Movement Interfaces",
          id:4
        },
        {
          name:"Reporting",
          id:5
        }],
        currentActiveTab: 0,
        previousActiveTab: -1
      }
    },
    methods: {
    }
  }
</script>

<style scoped>

</style>
