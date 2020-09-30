<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file" />
      </div>
    </div>
    <iframe style="width: 100%; min-height: 600px" :src="file.downloadURL" />
  </div>
</template>

<script>
import UserStorageDownloadButton from "./UserStorageDownloadButton";

export default {
  name: "user-storage-pdf-file-edit-viewer",
  props: {
    file: {
      required: true,
    },
  },
  components: {
    UserStorageDownloadButton: UserStorageDownloadButton,
  },
};
</script>
