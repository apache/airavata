<template>
  <div>
    <router-view
      v-if="userStoragePath"
      :user-storage-path="userStoragePath"
      :storage-path="storagePath"
      @upload-finished="uploadFinished"
      @add-directory="addDirectory"
      @delete-dir="deleteDir"
      @delete-file="deleteFile"
      @directory-selected="directorySelected"
      @file-content-changed="fileContentChanged"
      :allow-preview="false"
    ></router-view>
  </div>
</template>

<script>
import { services, utils } from "django-airavata-api";
import { notifications } from "django-airavata-common-ui";

export default {
  name: "user-storage-container",
  computed: {
    dataProductUri() {
      return this.$route.query.dataProductUri;
    },
  },
  data() {
    return {
      storagePath: null,
      userStoragePath: null,
    };
  },
  methods: {
    async setStoragePath() {
      let _storagePath = null;
      if (this.dataProductUri) {
        /**
         * TODO fix: storage path is set to home when it's a file referenced by dataProductUri because
         * there's no way of retrieving the path and this is to be fixed once a workaround is found.
         */
        _storagePath = "~/";
      } else {
        _storagePath = /~.*$/.exec(this.$route.fullPath);
        if (_storagePath && _storagePath.length > 0) {
          _storagePath = _storagePath[0];
        } else {
          _storagePath = this.$route.path;
        }
      }

      // Validate to have the ending slash.
      if (!_storagePath.endsWith("/")) {
        _storagePath += "/";
      }

      this.storagePath = _storagePath;
    },
    loadUserStoragePath(path) {
      const _catch = (err) => {
        if (err.details.status === 404) {
          this.handleMissingPath(path);
        } else {
          utils.FetchUtils.reportError(err);
        }
      };

      if (this.dataProductUri) {
        /**
         * TODO fix: userStoragePath is set manually when it's a file referenced by dataProductUri because
         * there's no way of retrieving the path and this is to be fixed once a workaround is found.
         */
        return utils.FetchUtils.get(
          `/api/data-products?product-uri=${this.dataProductUri}`
        )
          .then((dataProduct) => {
            this.userStoragePath = {
              isDir: false,
              directories: [],
              files: [
                {
                  createdTime: dataProduct.creationTime,
                  modifiedTime: dataProduct.lastModifiedTime,
                  dataProductURI: this.dataProductUri,
                  downloadURL: dataProduct.downloadURL,
                  mimeType: dataProduct.productMetadata["mime-type"],
                  name: dataProduct.productName,
                  size: dataProduct.productSize,
                },
              ],
              parts: [],
            };
          })
          .catch(_catch);
      } else {
        return services.UserStoragePathService.get(
          { path },
          { ignoreErrors: true }
        )
          .then((result) => {
            this.userStoragePath = result;
          })
          .catch(_catch);
      }
    },
    handleMissingPath(path) {
      this.$router.replace("/~/");
      // Display a transient error about the path not existing
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "WARNING",
          message: "Path does not exist: " + path,
          duration: 2,
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
    fileContentChanged() {
      this.loadUserStoragePath(this.storagePath);
    },
    uploadFinished() {
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
    },
  },
  async created() {
    if (this.$route.path === "/") {
      await this.$router.replace("/~/");
    } else {
      await this.setStoragePath();
      await this.loadUserStoragePath(this.storagePath);
    }
  },
  watch: {
    async $route() {
      await this.setStoragePath();
      await this.loadUserStoragePath(this.storagePath);
    },
  },
};
</script>
