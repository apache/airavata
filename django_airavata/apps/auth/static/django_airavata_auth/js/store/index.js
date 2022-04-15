import Vuex from "vuex";
import userProfile from "./modules/userProfile";
import extendedUserProfile from "./modules/extendedUserProfile";

const debug = process.env.NODE_ENV !== "production";

function createStore(Vue) {
  Vue.use(Vuex);
  return new Vuex.Store({
    modules: {
      userProfile,
      extendedUserProfile,
    },
    strict: debug,
  });
}

export default createStore;
