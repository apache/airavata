<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message"></div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file"/>
      </div>
    </div>
    <video controls style="width: 100%; min-height: 600px;">
      <source :src="file.downloadURL">
    </video>
  </div>
</template>

<script>
  import UserStorageDownloadButton from "./UserStorageDownloadButton";

  export default {
    name: "user-storage-video-file-edit-viewer",
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

