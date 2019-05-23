<template>
  <router-view :path="storagePath"></router-view>
</template>

<script>
export default {
  name: "user-storage-container",
  computed: {
    storagePath() {
      if (this.$route.path.startsWith("/")) {
        return this.$route.path.substring(1);
      } else {
        return this.$route.path;
      }
    }
  },
  watch: {
    // '$route' (to, from) {

    // }
  }
};
</script>
