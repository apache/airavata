let batchQueues=function () {
  return {
      queueDescription: '',
      maxRunTime: 0,
      maxNodes: 0,
      maxProcessors: 0,
      maxJobsInQueue: 0,
      maxMemory: 0,
      cpuPerNode: 0,
      defaultNodeCount: 0,
      defaultCPUCount: 0,
      defaultWalltime: 0,
      queueSpecificMacros: '',
      isDefaultQueue: false
    }
}

let defaultData = function () {
  return {
    hostName: '',
    hostAliases: [''],
    ipAddresses: [''],
    resourceDescription: '',
    maxMemoryPerNode: 0,
    batchQueues: [batchQueues()]
  }
}

export default {
  namespaced: true,
  state: {
    data: defaultData(),
    fetch: false,
    onlyView: false
  },
  mutations: {
    updateStore: function (state, data) {
      state.data = data
    },
    resetStore: function (state, {resetFields = []}) {
      if (resetFields.empty()) {
        state.data = defaultData()
      } else {
        let defData = defaultData();
        for (let resetField of resetFields) {
          state.data = defData[resetField]
        }
      }
    }
  },
  getters: {
    data: (state) => {
      if (state.fetch) {
        state.fetch = false
      }
      return state.data
    },
    view: (state) => state.onlyView,
    createBatchQueue:(state)=>batchQueues()
  },
  actions: {
    save: function ({commit, state, rootState}) {
    },
  }
}
