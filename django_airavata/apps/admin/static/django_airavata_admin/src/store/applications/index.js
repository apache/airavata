import interfaces from './app_interfaces'
import modules from './app_modules'
import deployments from './app_deployments'

export default {
  namespaced: true,
  modules: {
    interfaces,
    modules,
    deployments
  }
}
