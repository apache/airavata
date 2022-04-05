import Vuex from "vuex";
import userProfile from "./modules/userProfile";

const debug = process.env.NODE_ENV !== "production";

function createStore(Vue) {
  Vue.use(Vuex);
  return new Vuex.Store({
    modules: {
      userProfile,
    },
    strict: debug,
  });
}

export default createStore;
