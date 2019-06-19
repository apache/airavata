<template>

  <b-card
    :bg-variant="bgVariant"
    body-bg-variant="white"
    :header-text-variant="headerTextVariant"
    class="statistics-card"
  >
    <div
      slot="header"
      class="text-right"
    >
      <div class="statistic-count">{{ count }}</div>
      <div>{{ title }}</div>
    </div>
    <b-link
      :class="'text-decoration-none text-' + linkVariant"
      @click="$emit('click')"
    >
      <slot name="link-text">
        <div
          v-for="state in states"
          :key="state.value"
        >{{ state.name }}</div>
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
      default: "light"
    },
    headerTextVariant: {
      type: String,
      default: "dark"
    },
    linkVariant: {
      type: String,
      default: "primary"
    },
    count: {
      type: Number,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    states: {
      type: Array,
      default: () => []
    }
  }
};
</script>

<style scoped>
.statistic-count {
  font-size: 3rem;
}
.statistics-card {
  height: calc(100% - 30px);
}
</style>
