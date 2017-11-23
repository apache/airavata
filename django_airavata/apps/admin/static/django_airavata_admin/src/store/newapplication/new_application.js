import Vue from 'vue'

import appDetailsTab from './app_details'
import appInterfaceTab from './app_interface'
import appDeploymentsTab  from './app_deployments'


export default {
  namespaced: true,
  state:{
    appInterfaceTabInitialized:false,
    appDetailsTabInitialized:false,
    appDeploymentsTabInitialized:false
  },
  modules:{
    appInterfaceTab,
    appDetailsTab,
    appDeploymentsTab
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
    setModule:function ({context,state,rootState},moduleInformation) {
      rootState.appDetailsTab.name=moduleInformation.name
      rootState.appDetailsTab.version=moduleInformation.version
      rootState.appDetailsTab.description=moduleInformation.description


    }
  }
}
