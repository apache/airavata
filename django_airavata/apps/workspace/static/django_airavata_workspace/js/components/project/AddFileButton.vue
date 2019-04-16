<template>
  <div>
    <b-btn v-b-modal="'modal-add-file'" variant="primary" @click="getUserFiles">
      <slot>
        Add Files <i class="fa fa-plus" aria-hidden="true"></i>
      </slot>
    </b-btn>

    <b-modal id="modal-add-file" scrollable hide-footer title="User Files" ref="modalAddFile">

      <b-form-checkbox
            v-model="selected"
            v-for="userfile in userfiles"
            :key="userfile.file_dpu"
            :value="userfile.file_dpu">

        <label class="file-option">{{ userfile.file_name}}</label>

       </b-form-checkbox>

      <footer id="modal-add-file___BV_modal_footer_" class="modal-footer">

        <div class="upload-btn-wrapper">

          <b-btn class="upload_btn" variant="success">Upload</b-btn>

          <input type="file" id="file" class="inputfile" ref="file" multiple  v-on:change="handleFileUpload()"/>

        </div>

        <b-btn variant="danger" @click="handleFileDelete">Delete</b-btn>
        <b-btn variant="primary" @click="closeModal">Close</b-btn>

      </footer>

    </b-modal>
  </div>
</template>

<script>

import { utils as apiUtils } from "django-airavata-api";

  export default {
    name: 'add-file-button',
    data () {
      return {
        userfiles:[],
        selected: [],
        files:'',
        value:''

      }
    },
    components: {
    },
    methods: {
      handleFileUpload(){

        //multiple file upload

        for (const entry of this.$refs.file.files) {

          let formData=new FormData();
          formData.append('file',entry);
          apiUtils.FetchUtils.post(
            "/api/upload-ufiles",
            formData
          ).then(res => (this.userfiles.push(res['file_details'])));

        }

        window.location.reload()

      },

      handleFileDelete: function () {


        //post to delete multiple user files

        for (const entry of this.selected) {

         apiUtils.FetchUtils.post(
           "/api/delete-ufiles",
           entry
         ).then(res => (this.value = res['deleted']));

        }

        window.location.reload()

      },
      getUserFiles(){
        return apiUtils.FetchUtils.get(
          "/api/get-ufiles"
        ).then(res => (this.userfiles =res['user-files']));

      },
      closeModal(){
        this.$refs['modalAddFile'].hide()
      }

    },
    computed: {

    },

  }

</script>

<style scoped>

  .upload-btn-wrapper {
    position: relative;
    overflow: hidden;
    display: inline-block;
  }

  .upload-btn-wrapper input[type=file] {
    font-size: 100px;
    position: absolute;
    left: 0;
    top: 0;
    opacity: 0;
  }

  .modal-body {
    float: left;
    flex-flow: column;
    flex-direction: column;
  }


</style>
