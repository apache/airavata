<template>
  <div style="position:relative">
    <span class="selected-cards" style="position:relative">
      <b-button variant="warning" v-for="item in selected" v-bind:key="item.id" @click="removeClick(item)">
        {{ item.name }} <b-badge variant="light"><a href="#">x</a></b-badge>
      </b-button>
    </span>
    <hr>
    <autocomplete-text-input :suggestions="suggestions" @selected="suggestionSelected" />
  </div>
</template>

<script>
import AutocompleteTextInput from "./AutocompleteTextInput.vue";
import VModelMixin from "../mixins/VModelMixin";

export default {
  name: "autocomplete",
  mixins: [VModelMixin],
  props: {
    value: {
      type: Array
    },

    suggestions: {
      type: Array,
      required: true
    }
  },
  components: {
    AutocompleteTextInput
  },
  computed: {
    selected() {
      return this.suggestions.filter(suggestion => {
        return this.data.indexOf(suggestion.id) >= 0;
      });
    }
  },
  methods: {
    suggestionSelected(suggestion) {
      if (this.data.indexOf(suggestion.id) == -1) {
        this.data.push(suggestion.id);
      }
    },
    removeClick(data) {
      var index = this.data.indexOf(data.id);
      this.data.splice(index, 1);
    }
  }
};
</script>
<style>
.selected-cards > button {
  margin-bottom: 10px;
  margin-right: 10px;
}
.selected-cards > button:last-child {
  margin-right: 0px;
}
</style>
