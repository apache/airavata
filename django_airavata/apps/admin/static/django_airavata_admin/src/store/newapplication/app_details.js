import Vue from 'vue'
import Utils from '../../utils'

const fieldMapper = {
  name: 'appModuleName',
  version: 'appModuleVersion',
  description: 'appModuleDescription'
}

var initialState = function () {
  return {
    name: '',
    version: '',
    description: ''
  }
}

export default {
  namespaced: true,
  state: initialState(),
  mutations: {
    addAppDetails: function (state, update) {
      for (var prop in update) {
        if (state.hasOwnProperty(prop)) {
          Vue.set(state, prop, update[prop])
        }
      }
    },
    registerAppDetails: function (state) {
      return Utils.post('/api/new/application/module', state)
    },
    resetState: state => {
      Utils.resetData(state, initialState())
    }
  },
  getters: {
    getAppName: (state) => {
      return state.name;
    },
    getAppVersion: (state) => {
      return state.version;
    },
    getAppDescription: (state) => {
      return state.description;
    },
    getAppDetails: (state) => {
      return state;
    }
  },
  actions: {
    updateAppDetails: function (context, update) {
      context.commit('addAppDetails', update)
    },
    registerAppModule: function ({commit,state},callable=(value)=>console.log("App Details",value)) {
      return Utils.post('/api/new/application/module', state,callable=callable)
    },
    resetAll: function (context) {
      context.commit('resetState')
    }
  }
}
