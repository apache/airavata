import Vuex from "vuex";
import extendedUserProfile from "./modules/extendedUserProfile";

const debug = process.env.NODE_ENV !== "production";

function createStore(Vue) {
  Vue.use(Vuex);
  return new Vuex.Store({
    modules: {
      extendedUserProfile,
    },
    strict: debug,
  });
}

export default createStore;
