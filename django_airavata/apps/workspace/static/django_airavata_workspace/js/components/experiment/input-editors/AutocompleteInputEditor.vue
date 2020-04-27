<template>
  <!-- TODO: replace with better display and x to clear out selected value -->
  <div v-if="value">
    {{ text }}
    <b-link @click="cancel">Cancel</b-link>
  </div>
  <div v-else>
    <autocomplete-text-input
      :suggestions="suggestions"
      @selected="selected"
      @search-changed="searchChanged"
      :max-matches="10"
    />
  </div>
</template>

<script>
import { utils } from "django-airavata-api";
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "autocomplete-input-editor",
  mixins: [InputEditorMixin],
  components: {
    "autocomplete-text-input": components.AutocompleteTextInput
  },
  props: {
    value: {
      type: String
    }
  },
  data() {
    return {
      text: null,
      searchString: "",
      searchResults: null,
      lastUpdate: Date.now()
    };
  },
  computed: {
    suggestions() {
      return this.searchResults
        ? this.searchResults.results.map(r => {
            return {
              id: r.value,
              name: r.text
            };
          })
        : [];
    },
    autocompleteUrl() {
      if (
        this.experimentInput.editorConfig &&
        "url" in this.experimentInput.editorConfig
      ) {
        return this.experimentInput.editorConfig.url;
      } else {
        // eslint-disable-next-line no-console
        console.warn(
          "editor config is missing 'url'. Make sure input " +
            this.experimentInput.name +
            " has metadata configuration something like:\n" +
            JSON.stringify(
              {
                editor: {
                  "ui-component-id": "autocomplete-input-editor",
                  config: {
                    url: "/some/custom/search/"
                  }
                }
              },
              null,
              4
            )
        );
        return null;
      }
    }
  },
  methods: {
    loadTextForValue(value) {
      if (this.autocompleteUrl) {
        return utils.FetchUtils.get(this.autocompleteUrl, {
          id: value
        }).then(resp => resp.text);
      } else {
        return Promise.resolve(null);
      }
    },
    cancel() {
      this.data = null;
      this.valueChanged();
    },
    selected(suggestion) {
      this.data = suggestion.id;
      this.text = suggestion.name;
      this.valueChanged();
    },
    searchChanged(newValue) {
      this.searchString = newValue;
      const currentTime = Date.now();
      if (this.autocompleteUrl) {
        utils.FetchUtils.get(
          this.autocompleteUrl,
          {
            search: this.searchString
          },
          { showSpinner: false }
        ).then(resp => {
          // Prevent older responses from overwriting newer ones
          if (currentTime > this.lastUpdate) {
            this.searchResults = resp;
            this.lastUpdate = currentTime;
          }
        });
      }
    }
  },
  created() {
    if (this.value) {
      this.loadTextForValue(this.value).then(text => (this.text = text));
    }
  }
};
</script>
