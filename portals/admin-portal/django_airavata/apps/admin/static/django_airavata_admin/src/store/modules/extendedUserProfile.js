import { models, services } from "django-airavata-api";

const state = () => ({
  extendedUserProfileFields: null,
  extendedUserProfileValues: null,
  deletedExtendedUserProfileFields: [],
});

const getters = {
  extendedUserProfileFields: (state) => state.extendedUserProfileFields,
  extendedUserProfileValues: (state) => state.extendedUserProfileValues,
};

const actions = {
  async loadExtendedUserProfileFields({ commit }) {
    const extendedUserProfileFields = await services.ExtendedUserProfileFieldService.list();
    commit("setExtendedUserProfileFields", { extendedUserProfileFields });
  },
  async loadExtendedUserProfileValues({ commit }, { username }) {
    const extendedUserProfileValues = await services.ExtendedUserProfileValueService.list(
      { username }
    );
    commit("setExtendedUserProfileValues", { extendedUserProfileValues });
  },
  async saveExtendedUserProfileFields({ commit, dispatch, state }) {
    let order = 1;
    for (const field of state.extendedUserProfileFields) {
      commit("setOrder", { field, order: order++ });
      if (field.supportsChoices) {
        for (let index = 0; index < field.choices.length; index++) {
          const choice = field.choices[index];
          commit("setChoiceOrder", { choice, order: index });
        }
      }
      for (let index = 0; index < field.links.length; index++) {
        const link = field.links[index];
        commit("setLinkOrder", { link, order: index });
      }
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
    if (state.deletedExtendedUserProfileFields.length > 0) {
      for (const field of state.deletedExtendedUserProfileFields) {
        await services.ExtendedUserProfileFieldService.delete({
          lookup: field.id,
        });
      }
      commit("resetDeletedExtendedUserProfileFields");
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
  setExtendedUserProfileValues(state, { extendedUserProfileValues }) {
    state.extendedUserProfileValues = extendedUserProfileValues;
  },
  setName(state, { value, field }) {
    setFieldProp(state, field, "name", value);
  },
  setCheckboxLabel(state, { value, field }) {
    setFieldProp(state, field, "checkbox_label", value);
  },
  setHelpText(state, { value, field }) {
    setFieldProp(state, field, "help_text", value);
  },
  setRequired(state, { value, field }) {
    setFieldProp(state, field, "required", value);
  },
  setOrder(state, { order, field }) {
    setFieldProp(state, field, "order", order);
  },
  setOther(state, { value, field }) {
    setFieldProp(state, field, "other", value);
  },
  addExtendedUserProfileField(state, { field }) {
    if (!state.extendedUserProfileFields) {
      state.extendedUserProfileFields = [];
    }
    state.extendedUserProfileFields.push(field);
  },
  addChoice(state, { field }) {
    field.choices.push(
      new models.ExtendedUserProfileFieldChoice({
        display_text: "",
      })
    );
  },
  setChoiceOrder(state, { choice, order }) {
    choice.order = order;
  },
  updateChoiceDisplayText(state, { choice, display_text }) {
    choice.display_text = display_text;
  },
  updateChoiceIndex(state, { field, choice, index }) {
    const currentIndex = field.choices.indexOf(choice);
    field.choices.splice(currentIndex, 1);
    field.choices.splice(index, 0, choice);
  },
  deleteChoice(state, { field, choice }) {
    const index = field.choices.indexOf(choice);
    field.choices.splice(index, 1);
  },
  addLink(state, { field }) {
    field.links.push(
      new models.ExtendedUserProfileFieldLink({
        label: "",
        url: "",
        display_link: true,
        display_inline: false,
      })
    );
  },
  updateLinkLabel(state, { link, label }) {
    link.label = label;
  },
  updateLinkURL(state, { link, url }) {
    link.url = url;
  },
  updateLinkDisplayLink(state, { link, display_link }) {
    link.display_link = display_link;
  },
  updateLinkDisplayInline(state, { link, display_inline }) {
    link.display_inline = display_inline;
  },
  setLinkOrder(state, { link, order }) {
    link.order = order;
  },
  deleteLink(state, { field, link }) {
    const index = field.links.indexOf(link);
    field.links.splice(index, 1);
  },
  updateFieldIndex(state, { field, index }) {
    const currentIndex = state.extendedUserProfileFields.indexOf(field);
    state.extendedUserProfileFields.splice(currentIndex, 1);
    state.extendedUserProfileFields.splice(index, 0, field);
  },
  deleteField(state, { field }) {
    const index = state.extendedUserProfileFields.indexOf(field);
    state.extendedUserProfileFields.splice(index, 1);
    // later when we save we'll need to sync this delete with the server
    if (field.id) {
      state.deletedExtendedUserProfileFields.push(field);
    }
  },
  resetDeletedExtendedUserProfileFields(state) {
    state.deletedExtendedUserProfileFields = [];
  },
};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
};
