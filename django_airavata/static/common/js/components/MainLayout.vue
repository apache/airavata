<template>

  <div class="main-content-wrapper">
    <main
      class="main-content"
      :class="{'main-content--with-sidebar': hasSidebar}"
    >
      <div class="container-fluid">
        <slot />
      </div>
    </main>
    <slot name="sidebar" />
  </div>
</template>

<script>
export default {
  name: "main-layout",
  computed: {
    hasSidebar() {
      return this.$slots.sidebar;
    }
  }
};
</script>
