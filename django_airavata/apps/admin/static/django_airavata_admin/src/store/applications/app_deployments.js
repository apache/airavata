import { services, models } from 'django-airavata-api'

export default {
  namespaced: true,
  state: {
    currentDeployment: null,
    deployments: null,
  },
  mutations: {
    setCurrentDeployment(state, currentDeployment) {
      state.currentDeployment = currentDeployment;
    },
    setDeployments(state, deployments) {
      state.deployments = deployments;
    }
  },
  actions: {
    loadApplicationDeployments({ commit }, appModuleId) {
      services.ApplicationModuleService.getApplicationDeployments({ lookup: appModuleId })
        .then(appDeployments => {
          commit('setDeployments', appDeployments);
          return appDeployments;
        });
    },
    loadApplicationDeployment({ commit }, appDeploymentId) {
      services.ApplicationDeploymentService.retrieve({ lookup: appDeploymentId })
        .then(appDeployment => {
          commit('setCurrentDeployment', appDeployment);
          return appDeployment;
        });
    },
    createApplicationDeployment({ commit }, appDeployment) {
      return services.ApplicationDeploymentService.create({ data: appDeployment })
        .then(appDeployment => {
          commit('setCurrentDeployment', appDeployment);
          return appDeployment;
        });
    },
    updateApplicationDeployment({ commit }, appDeployment) {
      return services.ApplicationDeploymentService.update({ lookup: appDeployment.appDeploymentId, data: appDeployment })
        .then(appDeployment => {
          commit('setCurrentDeployment', appDeployment);
          return appDeployment;
        })
    }
  }
}
