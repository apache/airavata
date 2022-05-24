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
  async saveExtendedUserProfileFields({ dispatch, state }) {
    for (const field of state.extendedUserProfileFields) {
      // Create or update each field
      if (field.id) {
        await services.ExtendedUserProfileFieldService.update({
          lookup: field.id,
          data: field,
        });
      } else {
        await services.ExtendedUserProfileFieldService.create({ data: field });
      }
    }
    // Reload the fields
    dispatch("loadExtendedUserProfileFields");
  },
};

function getField(state, field) {
  const extendedUserProfileField = state.extendedUserProfileFields.find(
    (f) => f === field
  );
  return extendedUserProfileField;
}
function setFieldProp(state, field, prop, value) {
  const extendedUserProfileField = getField(state, field);
  extendedUserProfileField[prop] = value;
}

const mutations = {
  setExtendedUserProfileFields(state, { extendedUserProfileFields }) {
    state.extendedUserProfileFields = extendedUserProfileFields;
  },
  setName(state, { value, field }) {
    setFieldProp(state, field, "name", value);
  },
  setHelpText(state, { value, field }) {
    setFieldProp(state, field, "help_text", value);
  },
  setRequired(state, { value, field }) {
    setFieldProp(state, field, "required", value);
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
