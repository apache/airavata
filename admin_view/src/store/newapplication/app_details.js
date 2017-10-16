import Vue from 'vue'
import Utils from '../../utils'

const fieldMapper={
  name:'appModuleName',
  version:'appModuleVersion',
  description:'appModuleDescription'
}

export default{
  namespaced: true,
  state:{
    name:'',
    version:'',
    description:''
  },
  mutations:{
    addAppDetails:function (state,update) {
      for(var prop in update){
        if(state.hasOwnProperty(prop)){
          Vue.set(state,prop,update[prop])
        }
      }
    },
    registerAppDetails:function (state) {
      return Utils.post('/api/new/application/module',state)
    }
  },
  getters:{
    getAppName:(state)=>{
      return state.name;
    },
    getAppVersion:(state)=>{
      return state.version;
    },
    getAppDescription:(state)=>{
      return state.description;
    },
    getAppDetails:(state)=>{
      return state;
    }
  },
  actions:{
    updateAppDetails:function (context,update) {
      context.commit('addAppDetails',update)
    },
    registerAppModule:function (context) {
      context.commit("registerAppDetails")
    }
  }
}
