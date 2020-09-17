<template>
  <header class="sidebar-header">
    <h1 class="sidebar-header__title">{{ title }}</h1>
    <a :href="viewAllUrl" class="sidebar-header__action">View all</a>
  </header>
</template>

<script>
export default {
  name: "sidebar-header",
  props: {
    title: {
      type: String,
      default: "Recent Activity",
    },
    viewAllUrl: {
      type: String,
    },
  },
};
</script>
