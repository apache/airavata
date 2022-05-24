import { services } from "django-airavata-api";

const state = () => ({
  extendedUserProfileFields: null,
});

const getters = {
  extendedUserProfileFields: (state) => state.extendedUserProfileFields,
};

const actions = {
  async loadExtendedUserProfileFields({ commit }) {
    const extendedUserProfileFields = await services.ExtendedUserProfileFieldService.list();
    commit("setExtendedUserProfileFields", { extendedUserProfileFields });
  },
};

const mutations = {
  setExtendedUserProfileFields(state, { extendedUserProfileFields }) {
    state.extendedUserProfileFields = extendedUserProfileFields;
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
