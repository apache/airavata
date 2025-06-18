<template>
  <div class="main-content-wrapper">
    <main
      class="main-content"
      :class="{ 'main-content--with-sidebar': hasSidebar }"
    >
      <notifications-display />
      <div class="container-fluid">
        <slot />
      </div>
    </main>
    <slot name="sidebar" />
  </div>
</template>

<script>
import NotificationsDisplay from "./NotificationsDisplay.vue";

export default {
  name: "main-layout",
  computed: {
    hasSidebar() {
      return this.$slots.sidebar;
    },
  },
  components: {
    NotificationsDisplay,
  },
};
</script>
