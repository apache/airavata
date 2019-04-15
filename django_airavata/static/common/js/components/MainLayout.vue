<template>

  <div class="d-flex flex-grow-1">
    <main
      class="main-content"
      :class="{'main-content--with-sidebar': hasSidebar}"
    >
      <div class="container-fluid">
        <slot />
      </div>
    </main>
    <aside
      v-if="hasSidebar"
      class="sidebar"
    >
      <slot name="sidebar" />
    </aside>
  </div>
</template>

<script>
export default {
  name: "main-layout",
  computed: {
    hasSidebar() {
      return !!this.$slots.sidebar;
    }
  }
};
</script>
