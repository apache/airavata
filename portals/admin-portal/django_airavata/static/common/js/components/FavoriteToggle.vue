<template>
  <b-link
    class="text-primary"
    @click.stop="toggleFavorite"
    v-b-tooltip
    :title="titleText"
  >
    <i class="fa fa-star favorite-toggle" :class="classes"
      ><span class="sr-only">Toggle favorite</span></i
    >
  </b-link>
</template>

<script>
export default {
  name: "favorite-toggle",
  props: {
    favorite: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    toggleFavorite() {
      if (this.favorite) {
        this.$emit("unfavorite");
      } else {
        this.$emit("favorite");
      }
    },
  },
  computed: {
    classes() {
      if (!this.favorite) {
        return ["not-favorite"];
      } else {
        return [];
      }
    },
    titleText() {
      if (this.favorite) {
        return "Unmark as favorite";
      } else {
        return "Mark as favorite";
      }
    },
  },
};
</script>

<style scoped>
.not-favorite {
  color: #999999;
}
.not-favorite:hover {
  color: inherit;
}
</style>
