import Vuex from 'vuex';
import Vue from 'vue';

import appDetailsTab from './newapplication/app_details'
import appInterfaceTab from './newapplication/app_interface'
import appDeploymentsTab  from './newapplication/app_deployments'
import newApplication from './newapplication/new_application'
import loading from './loading'

Vue.use(Vuex);


const store={
  modules:{
    newApplication,
    loading
  }
};

export default new Vuex.Store(store);

