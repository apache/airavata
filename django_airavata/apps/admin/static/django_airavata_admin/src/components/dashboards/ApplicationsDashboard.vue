<template>
  <div class="new_app">
    <div class="new_app_header">
      <h4 style="display: inline-block">Application Catalog</h4>
      <label v-on:click="newApplicationHandler()">New Application <span>+</span></label>
    </div>
    <div class="applications">
      <h6 style="color: #666666;">APPLICATIONS</h6>
      <div class="container-fluid">
        <div class="row">
            <application-card v-for="item in modules" v-bind:app-module="item"
                v-bind:key="item.appModuleId" v-on:app-selected="clickHandler(item)">
            </application-card>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import NewApplication from '../admin/NewApplication.vue'
  import Loading from '../Loading.vue'

  import Utils from '../../utils'
  import {mapActions, mapState} from 'vuex'

  import { components as comps } from 'django-airavata-common-ui'

  export default {
    components:{
      NewApplication, Loading,
      'application-card': comps.ApplicationCard,
    },
    mounted:function () {
      this.loadApplications();
    },
    computed: {
      ...mapState('applications/modules', ['modules']),
    },
    methods:{
      clickHandler: function (item) {
        this.setTitle("Edit Application")
        this.resetApplication()
        this.setModule(item)
        this.$router.push({name: 'application_module', params: {id: item.appModuleId}})
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
      ...mapActions({loadApplications: 'applications/modules/loadApplicationModules', setModule:'newApplication/setModule',setTitle:'newApplication/setTitle',restInterface:'newApplication/appInterfaceTab/resetState',resetDetails:'newApplication/appDetailsTab/resetState',resetDeployment:'newApplication/appDeploymentsTab/resetState'}),
    }
  }
</script>
<style scoped>
  .new_app {
    margin: 45px;
    width: 100%;
    background-color: #f7f7f7;
  }

  .new_app_header{
    width: 100%;
    display: inline;
  }

  .new_app_header label{
    background-color: #2e73bc;
    color: white;
    border: solid #2e73bc 1px ;
    border-radius: 3px;
    float: right;
    padding-right: 15px;
    padding-left: 15px;
    padding-bottom: 8px;
    padding-top: 3px;
    text-align: center;
  }

  .new_app_header label:hover{
    cursor: pointer;
  }

  .new_app_header label span{
    font-weight: 900;
    font-size: 25px;
  }

  .applications{
    margin-top: 50px;
  }

  .ssh,.generate input{
      text-align: center;
  }
</style>
