<template>
  <img style="width: 100%;" :src="file.downloadURL"/>
</template>

<script>

  export default {
    name: "user-storage-image-file-edit-viewer",
    props: {
      file: {
        required: true
      }
    }
  };

</script>

