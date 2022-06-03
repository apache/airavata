import { models, services } from "django-airavata-api";

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
  async saveExtendedUserProfileFields({ commit, dispatch, state }) {
    let order = 1;
    for (const field of state.extendedUserProfileFields) {
      commit("setOrder", {field, order: order++});
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
  async addExtendedUserProfileField({ state, commit }, { field_type }) {
    const field = new models.ExtendedUserProfileField({
      field_type,
      name: `New Field ${state.extendedUserProfileFields.length + 1}`,
      description: "",
      help_text: "",
      required: true,
      links: [],
      other: false,
      choices: [],
      checkbox_label: "",
    });
    commit("addExtendedUserProfileField", { field });
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
  setOrder(state, {order, field}) {
    setFieldProp(state, field, 'order', order);
  },
  addExtendedUserProfileField(state, { field }) {
    if (!state.extendedUserProfileFields) {
      state.extendedUserProfileFields = [];
    }
    state.extendedUserProfileFields.push(field);
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
