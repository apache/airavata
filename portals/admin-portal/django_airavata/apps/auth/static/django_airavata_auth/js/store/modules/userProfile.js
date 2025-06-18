import { services } from "django-airavata-api";

const state = () => ({
  user: null,
});

const getters = {
  user: (state) => state.user,
};

const actions = {
  async loadCurrentUser({ commit }) {
    const user = await services.UserService.current();
    commit("setUser", { user });
  },

  async verifyEmailChange({ commit, state }, { code }) {
    const user = await services.UserService.verifyEmailChange({
      lookup: state.user.id,
      data: { code },
    });
    commit("setUser", { user });
  },

  async updateUser({ commit, state }) {
    const user = await services.UserService.update({
      lookup: state.user.id,
      data: state.user,
    });
    commit("setUser", { user });
  },

  async resendEmailVerification({ state }) {
    await services.UserService.resendEmailVerification({
      lookup: state.user.id,
    });
  },
};

const mutations = {
  setUser(state, { user }) {
    state.user = user;
  },

  setFirstName(state, { first_name }) {
    state.user.first_name = first_name;
  },

  setLastName(state, { last_name }) {
    state.user.last_name = last_name;
  },

  setEmail(state, { email }) {
    state.user.email = email;
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
