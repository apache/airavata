<template>
  <b-card
    :bg-variant="bgVariant"
    body-bg-variant="white"
    :header-text-variant="headerTextVariant"
    class="statistics-card"
  >
    <div slot="header" class="text-right">
      <div class="statistic-count text-nowrap">
        <abbr :title="count">{{ displayedCount }}</abbr>
      </div>
      <div>{{ title }}</div>
    </div>
    <b-link
      :class="'text-decoration-none text-' + linkVariant"
      @click="$emit('click')"
    >
      <slot name="link-text">
        <div v-for="state in states" :key="state.value">{{ state.name }}</div>
      </slot>
    </b-link>
  </b-card>
</template>

<script>
export default {
  name: "experiment-statistics-card",
  props: {
    bgVariant: {
      type: String,
      default: "light",
    },
    headerTextVariant: {
      type: String,
      default: "dark",
    },
    linkVariant: {
      type: String,
      default: "primary",
    },
    count: {
      type: Number,
      required: true,
    },
    title: {
      type: String,
      required: true,
    },
    states: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    displayedCount() {
      // Round large numbers and display m for 10^6 and k for 10^3
      if (this.count >= Math.pow(10, 6)) {
        return (this.count / Math.pow(10, 6)).toFixed(0) + "m";
      } else if (this.count >= Math.pow(10, 3)) {
        return (this.count / Math.pow(10, 3)).toFixed(0) + "k";
      } else {
        return this.count;
      }
    },
  },
};
</script>

<style scoped>
.statistic-count {
  font-size: 2.8rem;
  overflow: hidden;
}
.statistics-card {
  height: calc(100% - 30px);
}
abbr {
  text-decoration: none;
}
</style>
