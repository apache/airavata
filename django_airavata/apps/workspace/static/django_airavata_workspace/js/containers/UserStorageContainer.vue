<template>
  <div>
    <router-view
      v-if="userStoragePath"
      :user-storage-path="userStoragePath"
      :storage-path="storagePath"
      @upload-success="uploadSuccess"
      @add-directory="addDirectory"
      @delete-dir="deleteDir"
      @delete-file="deleteFile"
      @directory-selected="directorySelected"
      @file-content-changed="fileContentChanged"
    ></router-view>
  </div>
</template>

<script>
import { services, utils } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";

export default {
  name: "user-storage-container",
  computed: {
    storagePath() {
      let _storagePath = /~.*$/.exec(this.$route.fullPath);
      if (_storagePath && _storagePath.length > 0) {
        _storagePath = _storagePath[0];
      } else {
        _storagePath = this.$route.path;
      }

      // Validate to have the ending slash.
      if (!_storagePath.endsWith("/")) {
        _storagePath += "/";
      }

      return _storagePath;
    }
  },
  data() {
    return {
      userStoragePath: null
    };
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get(
        { path },
        { ignoreErrors: true }
      )
        .then(result => {
          this.userStoragePath = result;
        })
        .catch(err => {
          if (err.details.status === 404) {
            this.handleMissingPath(path);
          } else {
            utils.FetchUtils.reportError(err);
          }
        });
    },
    handleMissingPath(path) {
      this.$router.replace("/~/");
      // Display a transient error about the path not existing
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "WARNING",
          message: "Path does not exist: " + path,
          duration: 2
        })
      );
    },
    fileChanged() {
      if (this.file && !this.fileTooLarge) {
        let data = new FormData();
        data.append("file", this.file);
        utils.FetchUtils.post(
          "/api/user-storage/" + this.storagePath,
          data
        ).then(() => {
          // this.file = null;
          this.$refs["file-input"].reset();
          this.loadUserStoragePath(this.storagePath);
        });
      }
    },
    fileContentChanged(fileContent) {
      if (fileContent) {
        utils.FetchUtils.put(
          "/api/user-storage/" + this.storagePath,
          {fileContentText: fileContent}
        ).then(() => {
          this.loadUserStoragePath(this.storagePath);
        });
      }
    },
    uploadSuccess() {
      this.loadUserStoragePath(this.storagePath);
    },
    addDirectory(dirName) {
      if (dirName) {
        let newDirPath = this.storagePath;
        if (!newDirPath.endsWith("/")) {
          newDirPath = newDirPath + "/";
        }
        newDirPath = newDirPath + dirName;
        utils.FetchUtils.post("/api/user-storage/" + newDirPath).then(() => {
          this.loadUserStoragePath(this.storagePath);
        });
      }
    },
    deleteDir(path) {
      utils.FetchUtils.delete("/api/user-storage/~/" + path).then(() => {
        this.loadUserStoragePath(this.storagePath);
      });
    },
    deleteFile(dataProductURI) {
      utils.FetchUtils.delete(
        "/api/delete-file?data-product-uri=" +
          encodeURIComponent(dataProductURI)
      ).then(() => {
        this.loadUserStoragePath(this.storagePath);
      });
    },
    directorySelected(path) {
      this.$router.push("/~/" + path);
    }
  },
  created() {
    if (this.$route.path === "/") {
      this.$router.replace("/~/");
    } else {
      this.loadUserStoragePath(this.storagePath);
    }
  },
  watch: {
    $route() {
      this.loadUserStoragePath(this.storagePath);
    }
  }
};
</script>
