<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file" />
      </div>
    </div>
    <div class="user-storage-file-edit-viewer-no-preview">
      No preview available.
    </div>
  </div>
</template>

<script>
import UserStorageDownloadButton from "./UserStorageDownloadButton";

export default {
  name: "user-storage-default-file-edit-viewer",
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
