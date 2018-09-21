import Vuex from "vuex";
import Vue from "vue";

import newApplication from "./newapplication/new_application";
import computeResource from "./compute_resource_dashboard/compute_resource";

import loading from "./loading";

Vue.use(Vuex);

const store = {
  modules: {
    newApplication,
    loading,
    computeResource
  }
};

export default new Vuex.Store(store);
