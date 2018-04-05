import Vue from "vue";
import DjangoAiravataAPI from 'django-airavata-api'

var defaultData = function () {
  return {
    jobSubmissionInterfaceId: null,
    securityProtocol: null,
    unicoreEndPointURL: null
  }
}
export default {
  namespaced: true,
  state: {
    data: {},
    fetch: false,
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
      if (state.fetch && state.data[id] == null) {
        return DjangoAiravataAPI.services.UnicoreJobSubmissionService.retrieve(id).then(value => {
          state.data[id] = value;
          return Promise.resolve(value);
        });
      }
      return state.data[id]
    },
  },
  actions: {
    save: function ({commit, state, rootState}, id) {
    },
  }
}
