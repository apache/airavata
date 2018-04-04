import Vue from 'vue'

import appDetailsTab from './app_details'
import appInterfaceTab from './app_interface'
import appDeploymentsTab  from './app_deployments'


export default {
  namespaced: true,
  state:{
    appInterfaceTabInitialized:false,
    appDetailsTabInitialized:false,
    appDeploymentsTabInitialized:false,
    title:null
  },
  modules:{
    appInterfaceTab,
    appDetailsTab,
    appDeploymentsTab
  },
  getters:{
    getTitle:function (state) {
      return state.title
    }
  },
  mutations:{
    setInitialization:function (state,update) {
      for(prop in update){
        if(state.hasOwnProperty(prop)){
          Vue.set(state,prop,update[prop])
        }
      }
    }
  },
  actions:{
    setModule:function ({commit,state,rootState},moduleInformation) {
      rootState.newApplication.appDetailsTab.name=moduleInformation.appModuleName
      rootState.newApplication.appDetailsTab.version=moduleInformation.appModuleVersion
      rootState.newApplication.appDetailsTab.description=moduleInformation.appModuleDescription
      rootState.newApplication.appDetailsTab.appModuleId=moduleInformation.appModuleId
      state.appInterfaceTabInitialized=true
      state.appDeploymentsTabInitialized=true

    },
    setTitle:function ({context,state,rootState},title) {
      state.title=title
    },
    resetStates:function () {

    }
  }
}
