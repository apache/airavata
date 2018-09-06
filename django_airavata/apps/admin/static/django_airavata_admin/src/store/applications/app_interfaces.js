import { services, models } from 'django-airavata-api'

export default {
  namespaced: true,
  state: {
    currentInterface: null,
  },
  mutations: {
    setCurrentInterface(state, currentInterface) {
      state.currentInterface = currentInterface;
    }
  },
  actions: {
    loadApplicationInterface({ commit }, appModuleId) {
      // Otherwise fetch from the backend
      return services.ApplicationModuleService.getApplicationInterface({ lookup: appModuleId }, { ignoreErrors: true })
        .then(appInterface => {
          commit('setCurrentInterface', appInterface);
          return appInterface;
        })
        .catch(error => {
          if (error.details.status === 404) {
            // If there is no interface, just create a new instance
            const appInterface = new models.ApplicationInterfaceDefinition();
            appInterface.addStandardOutAndStandardErrorOutputs();
            commit('setCurrentInterface', appInterface);
            return Promise.resolve(null);
          } else {
            throw error;
          }
        });
    },
    createApplicationInterface({ commit }, appInterface) {
      return services.ApplicationInterfaceService.create({ data: appInterface })
        .then(appInterface => {
          commit('setCurrentInterface', appInterface);
          return appInterface;
        });
    },
    updateApplicationInterface({ commit }, appInterface) {
      return services.ApplicationInterfaceService.update({ lookup: appInterface.applicationInterfaceId, data: appInterface })
        .then(appInterface => {
          commit('setCurrentInterface', appInterface);
          return appInterface;
        })
    }
  }
}
