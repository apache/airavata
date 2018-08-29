import { services } from 'django-airavata-api'

export default {
    namespaced: true,
    state: {
        modules: null,
        currentModule: null,
    },
    mutations: {
        setModules(state, modules) {
            state.modules = modules;
        },
        setModule(state, module) {
            state.currentModule = module;
        }
    },
    actions: {
        loadApplicationModules({ commit }) {
            services.ApplicationModuleService.list()
                .then(appModules => commit('setModules', appModules));
        },
        loadApplicationModule({ commit, state }, appModuleId) {
            if (state.modules) {
                const appModule = state.modules.find(appModule => appModule.appModuleId === appModuleId);
                if (appModule) {
                    commit('setModule', appModule);
                    return;
                }
            }
            // Otherwise fetch from the backend
            services.ApplicationModuleService.retrieve({lookup: appModuleId})
                .then(appModule => commit('setModule', appModule));
        }
    }
}
