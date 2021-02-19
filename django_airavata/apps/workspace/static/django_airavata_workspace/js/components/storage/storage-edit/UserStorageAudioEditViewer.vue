<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :data-product-uri="dataProductUri" :file-name="fileName"/>
      </div>
    </div>
    <audio controls style="width: 100%">
      <source :src="downloadUrl"/>
    </audio>
  </div>
</template>

<script>
import UserStorageDownloadButton from "./UserStorageDownloadButton";

export default {
  name: "user-storage-audio-file-edit-viewer",
  props: {
    fileName: {
      required: true,
    },
    dataProductUri: {
      required: true,
    },
    mimeType: {
      required: true,
    },
    downloadUrl: {
      required: true,
    }
  },
  components: {
    UserStorageDownloadButton: UserStorageDownloadButton,
  },
};
</script>
