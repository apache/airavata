<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-5">Application Catalog</h1>
      </div>
      <div id="col-new-application" class="col">
        <button class="btn btn-primary" type="button"
            v-on:click="newApplicationHandler()">
          New Application <i aria-hidden="true" class="fa fa-plus"></i>
        </button>
      </div>
    </div>
    <h2 class="h6 mb-2 text-muted text-uppercase">Applications</h2>
    <div class="row">
        <application-card v-for="item in applications" v-bind:app-module="item"
            v-bind:key="item.appModuleId" v-on:app-selected="clickHandler(item)">
        </application-card>
    </div>
  </div>
</template>
<script>
  import NewApplication from '../admin/NewApplication.vue'
  import Loading from '../Loading.vue'

  import Utils from '../../utils'
  import {mapActions} from 'vuex'

  import { components as comps } from 'django-airavata-common-ui'

  export default {
    data:function () {
      return {
        "applications":[]
      };
    },
    components:{
      NewApplication, Loading,
      'application-card': comps.ApplicationCard,
    },
    mounted:function () {
      this.fetchApplications();
    },
    methods:{
      fetchApplications:function () {
          Utils.get('/api/applications',{success:(value)=>this.applications=value,failure:value => {
          this.applications=[{
            "appModuleId": "",
            "appModuleName": "No Applications Found",
            "appModuleDescription": "",
            "appModuleVersion": ""
          }]}})
      },
      clickHandler: function (item) {
        this.setTitle("Edit Application")
        this.resetApplication()
        this.setModule(item)
        this.$router.push({name: 'details'})
      },
      resetApplication:function () {
        console.log("Resetting")
        this.restInterface()
        this.resetDetails()
        this.resetDeployment()
      },
      newApplicationHandler:function () {
        this.setTitle('Create New Application')
        this.resetApplication()
        this.$router.push({name: 'details'})
      }
      ,
      ...mapActions({setModule:'newApplication/setModule',setTitle:'newApplication/setTitle',restInterface:'newApplication/appInterfaceTab/resetState',resetDetails:'newApplication/appDetailsTab/resetState',resetDeployment:'newApplication/appDeploymentsTab/resetState'}),
    }
  }
</script>
<style>
  #col-new-application {
      text-align: right;
  }
</style>
