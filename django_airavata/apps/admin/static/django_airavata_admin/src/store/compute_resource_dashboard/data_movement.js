import Vue from "vue";
import DjangoAiravataAPI from 'django-airavata-api'

var localDataMovement = function () {
  return {
    dataMovementInterfaceId: null
  }
}

var unicoreDataMovement = function () {
  return {
    dataMovementInterfaceId: null,
    securityProtocol: null,
    unicoreEndPointURL: null
  }
}
var gridFTPDataMovement = function () {
  return {
    dataMovementInterfaceId: null,
    securityProtocol: null,
    gridFTPEndPoints: []
  }
}

var scpDataMovement = function () {
  return {
    dataMovementInterfaceId: null,
    securityProtocol: null,
    alternativeSCPHostName: null,
    sshPort: 22
  }
}

export default {
  namespaced: true,
  state: {
    local: {},
    unicore: {},
    scp: {},
    grid: {},
    fetch: true,
  },
  mutations: {
    updateLocal: function (state, {data, id}) {
      state.local[id] = data
    },
    updateUnicore: function (state, {data, id}) {
      state.unicore[id] = data
    },
    updateSCP: function (state, {data, id}) {
      state.scp[id] = data
    },
    updateGrid: function (state, {data, id}) {
      state.grid[id] = data
    },
    resetStore: function (state, {id, resetFields = []}) {
      if (resetFields.length == 0) {
        state.data[id] = defaultData()
      } else {
        let defData = defaultData();
        for (let resetField of resetFields) {
          Vue.set(state.data[id], resetField, defData[resetField])
        }
      }
    },
    addLocal: function (state, id) {
      state.local[id] = localDataMovement()
    },
    addUnicore: function (state, id) {
      state.unicore[id] = unicoreDataMovement()
    },
    addGrid: function (state, id) {
      state.grid[id] = gridFTPDataMovement()
    },
    addScp: function (state, id) {
      state.scp[id] = scpDataMovement()
    }
  },
  getters: {
    localData: (state) => id => {
      if (state.fetch && state.local[id]) {
      }
      return state.local[id]
    },
    unicoreData: (state) => id => {
      if (state.fetch && !(id in state.unicore)) {
        state.unicore[id] = DjangoAiravataAPI.services.UnicoreDataMovementService.retrieve(id)
      }
      return state.unicore[id]
    },
    gridData: (state) => id => {
      if (state.fetch && !(id in state.grid)) {
        console.log("Fetch")
        state.grid[id] = DjangoAiravataAPI.services.GridFTPDataMovementService.retrieve(id)
      }
      return state.grid[id]
    },
    scpData: (state) => id => {
      if (state.fetch && !(id in state.scp)) {
        state.scp[id] = DjangoAiravataAPI.services.SCPDataMovementService.retrieve(id)
      }
      return state.scp[id]
    },

  },
  actions: {
    save: function ({commit, state, rootState}, id) {
    },
  }
}
