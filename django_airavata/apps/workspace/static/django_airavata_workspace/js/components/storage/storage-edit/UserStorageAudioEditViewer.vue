<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file"/>
      </div>
    </div>
    <audio controls style="width: 100%;">
      <source :src="file.downloadURL">
    </audio>
  </div>
</template>

<script>
  import UserStorageDownloadButton from "./UserStorageDownloadButton";

  export default {
    name: "user-storage-audio-file-edit-viewer",
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

