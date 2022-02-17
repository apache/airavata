import Vuex from "vuex";
import viewExperiment from "./modules/view-experiment";

const debug = process.env.NODE_ENV !== "production";

function createStore(Vue) {
  Vue.use(Vuex);
  return new Vuex.Store({
    modules: {
      viewExperiment,
    },
    strict: debug,
  });
}

export default createStore;
