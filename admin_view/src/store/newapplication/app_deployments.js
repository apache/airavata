import Vue from 'vue'

export default {
  state:{
    app_module:'',
    app_compute_host:'',
    app_exec_path:'',
    app_parall_type:'',
    app_deployment_descr:''
  },
  mutations:{
    updateAppDeploymentValues:function (state,update) {
      for(var prop in update){
        if(state.hasOwnProperty(prop)){
          Vue.set(state,prop,update[prop])
        }
      }
    }
  },
  getters:{
    getAppModule:(state)=>state.app_module,
    getAppComputeHost:(state)=>state.app_compute_host,
    getAppExecutablePath:(state)=>state.app_exec_path,
    getAppParallelismType:(state)=>state.app_parall_type,
    getAppDeploymentDescription:(state)=>state.app_deployment_descr
  },
  actions:{
    updateAppDeployment:function (context, update) {
      context.commit('updateAppDeploymentValues',update)

    }
  }
}
