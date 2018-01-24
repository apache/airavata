<template>

  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Application Details</h4>
      <div class="entry">
        <div class="heading">Application Name</div>
        <input type="text" v-model="name"/>
      </div>
      <div class="entry">
        <div class="heading">Application Version</div>
        <input type="text" v-model="version"/>
      </div>
      <div class="entry">
        <div class="heading" >Experiment Description</div>
        <textarea  style="height: 80px;" type="text" v-model="description"/>
      </div>
      <new-application-buttons v-bind:save="registerAppModule" v-bind:cancel="cancelAction" v-bind:sectionName="'Application Details'"></new-application-buttons>
    </div>
  </div>
</template>
<script>
  import NewApplicationButtons from './TabActionConsole.vue';
  import Loading from '../Loading.vue'
  import { createNamespacedHelpers } from 'vuex'

  const {mapGetters,mapActions} = createNamespacedHelpers('newApplication/appDetailsTab')

  export default{
    components:{
      NewApplicationButtons,Loading
    },
    mounted:function () {
      this.mount()
    },
    data:function () {
      return {
          'name':'',
          'version':'',
          'description':''
      };
    },
    methods:{
      mount:function () {
        this.name=this.getAppName()
        this.version=this.getAppVersion()
        this.description=this.getAppDescription()
      },
      updateStore:function (fieldName,newValue) {
        var update={}
        update[fieldName]=newValue
        this.updateAppDetails(update)
      },
      cancelAction:function () {
        this.resetState()
        this.mount()
      },
      ...mapGetters(['getAppName','getAppVersion','getAppDescription']),
      ...mapActions(['updateAppDetails','registerAppModule','resetState'])
    },
    watch:{
      name:function (newValue) {
        this.updateStore('name',newValue)
      },
      version:function (newValue) {
        this.updateStore('version',newValue)
      },
      description:function (newValue) {
        this.updateStore('description',newValue)
      }
    }
  }

</script>
<style>




</style>
