<template>
  <b-button :href="file.downloadURL" :download="file.name">Download</b-button>
</template>

<script>
export default {
  name: "user-storage-download-button",
  props: {
    file: {
      required: true,
    },
  },
};
</script>
