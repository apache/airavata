<template>
  <div class="autocomplete-text-input">
    <b-input-group>
      <b-input-group-text slot="prepend">
        <i class="fa fa-search"></i>
      </b-input-group-text>
      <b-form-input
        type="text"
        :value="searchValue"
        :placeholder="placeholder"
        @input="updateSearchValue"
        @keydown.native.enter="enter"
        @keydown.native.down="down"
        @keydown.native.up="up"
      ></b-form-input>
    </b-input-group>
    <b-list-group class="autocomplete-suggestion-list" v-if="open">
      <b-list-group-item
        v-for="(suggestion, index) in filtered"
        v-bind:class="{ active: isActive(index) }"
        href="#"
        @click="suggestionClick(index)"
        v-bind:key="suggestion.id"
      >
        <slot name="suggestion" :suggestion="suggestion">
          {{ suggestion.name }}
        </slot>
      </b-list-group-item>
    </b-list-group>
  </div>
</template>

<script>
export default {
  name: "autocomplete-text-input",
  props: {
    suggestions: {
      type: Array,
      required: true,
    },
    placeholder: {
      type: String,
      default: "Type to get suggestions...",
    },
    maxMatches: {
      type: Number,
      default: 5,
    },
  },
  data() {
    return {
      open: false,
      current: 0,
      searchValue: "",
    };
  },

  computed: {
    filtered() {
      return this.suggestions
        .filter((data) => {
          // Case insensitive search
          return (
            data.name.toLowerCase().indexOf(this.searchValue.toLowerCase()) >= 0
          );
        })
        .slice(0, this.maxMatches);
    },
  },
  methods: {
    updateSearchValue(value) {
      if (this.open === false) {
        this.open = true;
        this.current = 0;
      }
      if (value === "") {
        this.open = false;
      }
      this.searchValue = value;
      this.$emit("search-changed", value);
    },
    enter() {
      if (this.filtered.length === 0) {
        return;
      }
      this.emitSelectedItem(this.current);
      this.searchValue = "";
      this.open = false;
    },
    up() {
      if (this.current > 0) {
        this.current--;
      }
    },
    down() {
      if (this.current < this.filtered.length - 1) {
        this.current++;
      }
    },
    isActive(index) {
      return index === this.current;
    },
    suggestionClick(index) {
      this.emitSelectedItem(index);
      this.searchValue = "";
      this.open = false;
    },
    emitSelectedItem(index) {
      this.$emit("selected", this.filtered[index]);
    },
  },
};
</script>

<style scoped>
.autocomplete-text-input {
  position: relative;
}
.autocomplete-suggestion-list {
  width: 100%;
  position: absolute;
  z-index: 3;
}
</style>
