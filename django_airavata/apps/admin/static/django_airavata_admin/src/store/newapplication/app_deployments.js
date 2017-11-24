import Vue from 'vue'
import Utils from '../../utils'

var initialState = function () {
  return {
    appModuleId: '',
    computeHostId: '',
    executablePath: '',
    parallelism: '',
    appDeploymentDescription: '',
    moduleLoadCmds: [],
    libPrependPaths: [],
    libAppendPaths: [],
    setEnvironment: [],
    preJobCommands: [],
    postJobCommands: [],
    defaultNodeCount: 1,
    defaultCPUCount: 1,
    defaultQueueName: ''
  }
}
export default {
  namespaced: true,
  state: initialState(),
  mutations: {
    updateAppDeploymentValues: function (state, update) {
      for (var prop in update) {
        if (state.hasOwnProperty(prop)) {
          Vue.set(state, prop, update[prop])
        }
      }
    },
    addEnvironmentValue: function (state,) {

    }
  },
  getters: {
    getAppModule: (state) => state.appModuleId,
    getAppComputeHost: (state) => state.computeHostId,
    getAppExecutablePath: (state) => state.app_exec_path,
    getAppParallelismType: (state) => state.executablePath,
    getAppDeploymentDescription: (state) => state.appDeploymentDescription,
    getModuleLoadCmds: (state) => state.moduleLoadCmds,
    getLibPrependPaths: (state) => state.libPrependPaths,
    getLibAppendPaths: (state) => state.libAppendPaths,
    getSetEnvironment: (state) => state.setEnvironment,
    getPreJobCommands: (state) => state.preJobCommands,
    getPostJobCommands: (state) => state.postJobCommands,
    getDefaultNodeCount: (state) => state.defaultNodeCount,
    getDefaultCPUCount: (state) => state.defaultCPUCount,
    getDefaultQueueName: (state) => state.defaultQueueName,
    getCompleteData: (state) => Object.assign({},state)
  },
  actions: {
    updateAppDeployment: function (context, update) {
      context.commit('updateAppDeploymentValues', update)
    },
    saveApplicationDeployment:function ({commit,state, rootState}) {
        Utils.post('/new/application/deployment',state)
    },
    resetState:function ({commit,state,rootState}) {
      Utils.resetData(state,initialState())
    }
  }
}
