<template>
  <div>
    <b-form-select
      :id="id"
      v-model="data"
      style="width: auto"
      :state="componentValidState"
      @input="valueChanged"
    >
      <option
        v-for="userfile in userfiles"
        v-bind:key="userfile.file_dpu"
        v-bind:value="userfile.file_dpu"
      >
        {{ userfile.file_name }}
      </option>
    </b-form-select>
  </div>
</template>

<script>
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import { utils as apiUtils } from "django-airavata-api";

export default {
  name: "user-file-input-editor",
  mixins: [InputEditorMixin],
  data() {
    return {
      userfiles: [],
    };
  },
  beforeMount: function () {
    // loads the list of file entries in django UserFiles model
    return apiUtils.FetchUtils.get("/api/get-ufiles").then(
      (res) => (this.userfiles = res["user-files"])
    );
  },
};
</script>

<style scoped></style>
