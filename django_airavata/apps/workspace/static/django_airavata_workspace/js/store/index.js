import Vue from "vue";
import Vuex from "vuex";
import viewExperiment from "./modules/view-experiment";

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== "production";

export default new Vuex.Store({
  modules: {
    viewExperiment,
  },
  strict: debug,
});
