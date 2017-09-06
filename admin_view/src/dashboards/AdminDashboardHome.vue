<template>
  <div class="new_app">
    <div class="new_app_header">
      <h4 style="display: inline-block">Application Catalog</h4>
      <router-link :to="{name:'newapp'}"><button v-on="this.$emit('new_application')">New Application <span>+</span></button></router-link>
    </div>
    <div class="applications">
      <h6 style="color: #666666;">APPLICATIONS</h6>
      <div class="container-fluid">
        <div class="row">
        <DashboardItem
          v-for="item in applications" v-bind:dashboard_item="item" v-bind:key="item.title">
        </DashboardItem>
          </div>
      </div>

    </div>
  </div>
</template>
<script>
  import DashboardItem from '../components/DashboardItem.vue'
  import NewApplication from '../components/admin/NewApplication.vue'
  export default {
    data:function () {
      return {
        "applications":[]
      };
    },
    components:{
      DashboardItem,NewApplication
    },
    mounted:function () {
      this.fetchApplications();
    },
    methods:{
      fetchApplications:function () {
        var convert=function (applications) {

        };
        this.$http.get('/api/applications').then(response => {
          this.applications=response.body;
        }, response => {
          this.applications=[{
            "appModuleId": "",
            "appModuleName": "No Applications Found",
            "appModuleDescription": "",
            "appModuleVersion": ""
          }]
        });
      },

    }
  }
</script>
<style>
  .new_app {
    margin: 45px;
    width: 100%;
  }

  .new_app_header{
    width: 100%;
    display: inline;
  }

  .new_app_header button{
    background-color: #2e73bc;
    color: white;
    border: solid #2e73bc 1px ;
    border-radius: 3px;
    float: right;
    padding-right: 15px;
    padding-left: 15px;
    padding-bottom: 3px;
    padding-top: 3px;
  }

  .new_app_header button:hover{
    cursor: pointer;
  }

  .new_app_header button span{
    font-weight: 900;
    font-size: larger;
  }

  .applications{
    margin-top: 50px;
  }
</style>
