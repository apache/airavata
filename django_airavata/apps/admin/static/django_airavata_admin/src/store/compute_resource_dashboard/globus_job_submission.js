import Vue from "vue";

var defaultData = function (id = null) {
  return {
    jobSubmissionInterfaceId: null,
    securityProtocol: null,
    globusGateKeeperEndPoint: [null]
  }
}
export default {
  namespaced: true,
  state: {
    data: {}
  },
  mutations: {
    updateStore: function (state, {data, id}) {
      state.data[id] = data
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
    addEmptyData: function (state, id) {
      state.data[id] = defaultData()
    }
  },
  getters: {
    data: (state) => id => {
      return state.data[id]
    },
  },
  actions: {
    save: function ({commit, state, rootState}, id) {
    },
  }
}
