<template>
  <div>
    User storage edit {{storagePath}}
  </div>
</template>
<script>

  export default {
    name: "user-storage-file-edit-viewer",
    props: {
      userStoragePath: {
        required: true
      },
      storagePath: {
        required: true
      }
    },
    components: {},
    computed: {},
    methods: {}
  };
</script>

