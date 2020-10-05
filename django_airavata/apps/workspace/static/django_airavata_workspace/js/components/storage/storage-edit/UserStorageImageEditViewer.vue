<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file"/>
      </div>
    </div>
    <img style="width: 100%;" :src="file.downloadURL"/>
  </div>
</template>

<script>
  import UserStorageDownloadButton from "./UserStorageDownloadButton";

  export default {
    name: "user-storage-image-file-edit-viewer",
    props: {
      file: {
        required: true
      }
    },
    components: {
      UserStorageDownloadButton: UserStorageDownloadButton
    }
  };
</script>

