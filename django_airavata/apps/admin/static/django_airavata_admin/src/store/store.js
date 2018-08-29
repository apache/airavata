import Vuex from 'vuex';
import Vue from 'vue';

import newApplication from './newapplication/new_application'
import computeResource from './compute_resource_dashboard/compute_resource'
import applications from './applications/index'

import loading from './loading'

Vue.use(Vuex);


const store={
  modules:{
    newApplication,
    loading,
    computeResource,
    applications,
  }
};

export default new Vuex.Store(store);

