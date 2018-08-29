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
            return services.ApplicationModuleService.list()
                .then(appModules => {
                    commit('setModules', appModules);
                    return appModules;
                });
        },
        loadApplicationModule({ commit, state }, appModuleId) {
            if (state.modules) {
                const appModule = state.modules.find(appModule => appModule.appModuleId === appModuleId);
                if (appModule) {
                    commit('setModule', appModule);
                    return Promise.resolve(appModule);
                }
            }
            // Otherwise fetch from the backend
            return services.ApplicationModuleService.retrieve({lookup: appModuleId})
                .then(appModule => {
                    commit('setModule', appModule);
                    return appModule;
                });
        },
        createApplicationModule({ commit, state }, appModule) {
            return services.ApplicationModuleService.create({data: appModule})
                .then(appModule => {
                    commit('setModule', appModule);
                    const appModulesCopy = state.modules.slice();
                    appModulesCopy.push(appModule);
                    commit('setModules', appModulesCopy);
                    return appModule;
                });
        },
        updateApplicationModule({ commit, state }, appModule) {
            return services.ApplicationModuleService.update({lookup: appModule.appModuleId, data: appModule})
                .then(appModule => {
                    commit('setModule', appModule);
                    const appModules = state.modules.filter(mod => mod.appModuleId !== appModule.appModuleId)
                    appModules.push(appModule);
                    commit('setModules', appModules);
                    return appModule;
                })
        }
    }
}
