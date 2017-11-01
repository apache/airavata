import Vuex from 'vuex';
import Vue from 'vue';

import appDetailsTab from './newapplication/app_details'
import appInterfaceTab from './newapplication/app_interface'
import appDeploymentsTab  from './newapplication/app_deployments'

Vue.use(Vuex);


const store={
  modules:{
    appInterfaceTab,
    appDetailsTab,
    appDeploymentsTab
  }
};

export default new Vuex.Store(store);

