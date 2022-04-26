import { services } from "django-airavata-api";

const state = () => ({
  extendedUserProfileFields: null,
  extendedUserProfileValues: [],
});

const getters = {
  extendedUserProfileFields: (state) => state.extendedUserProfileFields,
  extendedUserProfileValues: (state) => state.extendedUserProfileValues,
  getTextValue: (state) => (id) => {
    const value = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    return value ? value.text_value : null;
  },
  getSingleChoiceValue: (state) => (id) => {
    const value = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (value && value.choices && value.choices.length === 1) {
      return value.choices[0];
    } else {
      return null;
    }
  },
  getMultiChoiceValue: (state) => (id) => {
    const value = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (value && value.choices) {
      return value.choices;
    } else {
      return null;
    }
  },
  getUserAgreementValue: (state) => (id) => {
    const value = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    return value && value.agreement_value;
  },
};

const actions = {
  async loadExtendedUserProfileFields({ commit }) {
    const extendedUserProfileFields = await services.ExtendedUserProfileFieldService.list();
    commit("setExtendedUserProfileFields", { extendedUserProfileFields });
  },
  async loadExtendedUserProfileValues({ commit }) {
    const extendedUserProfileValues = await services.ExtendedUserProfileValueService.list();
    commit("setExtendedUserProfileValues", { extendedUserProfileValues });
  },
  async saveExtendedUserProfileValues({ state, commit }) {
    for (const value of state.extendedUserProfileValues) {
      // Create or update each value
      if (value.id) {
        await services.ExtendedUserProfileValueService.update({
          lookup: value.id,
          data: value,
        });
      } else {
        const extendedUserProfileValue = await services.ExtendedUserProfileValueService.create(
          { data: value }
        );
        commit("updateExtendedUserProfileValue", { extendedUserProfileValue });
      }
    }
  },
};

const mutations = {
  setExtendedUserProfileFields(state, { extendedUserProfileFields }) {
    state.extendedUserProfileFields = extendedUserProfileFields;
  },
  setExtendedUserProfileValues(state, { extendedUserProfileValues }) {
    state.extendedUserProfileValues = extendedUserProfileValues;
  },
  setTextValue(state, { value, id }) {
    const profileValue = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (profileValue) {
      profileValue.text_value = value;
    } else {
      state.extendedUserProfileValues.push({
        value_type: "text",
        ext_user_profile_field: id,
        text_value: value,
      });
    }
  },
  setSingleChoiceValue(state, { value, id }) {
    const profileValue = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (profileValue) {
      profileValue.choices = [value];
      profileValue.other_value = "";
    } else {
      state.extendedUserProfileValues.push({
        value_type: "single_choice",
        ext_user_profile_field: id,
        choices: [value],
      });
    }
  },
  setMultiChoiceValue(state, { value, id }) {
    const profileValue = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (profileValue) {
      profileValue.choices = value;
      profileValue.other_value = "";
    } else {
      state.extendedUserProfileValues.push({
        value_type: "multi_choice",
        ext_user_profile_field: id,
        choices: value,
      });
    }
  },
  setUserAgreementValue(state, { value, id }) {
    const profileValue = state.extendedUserProfileValues.find(
      (v) => v.ext_user_profile_field === id
    );
    if (profileValue) {
      profileValue.agreement_value = value;
    } else {
      state.extendedUserProfileValues.push({
        value_type: "user_agreement",
        ext_user_profile_field: id,
        agreement_value: value,
      });
    }
  },
  updateExperimentInputValue(state, { extendedUserProfileValue }) {
    const index = state.extendedUserProfileValues.findIndex(
      (v) =>
        v.ext_user_profile_field ===
        extendedUserProfileValue.ext_user_profile_field
    );
    state.extendedUserProfileValues.splice(index, 1, extendedUserProfileValue);
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
