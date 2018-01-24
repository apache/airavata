<template>

  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Application Interface</h4>
      <div class="entry boolean-selectors">
        <boolean-radio-button v-bind:heading="'Enable Archiving Working Directory'" v-bind:selectorVal="work_dir"
                              v-bind:def="isEnableArchiveWorkingDirectory" v-bind:selectorId="booleanSelectorIDs[0]"
                              v-on:bool_selector="updateStore"></boolean-radio-button>
        <boolean-radio-button v-bind:heading="'Enable Optional File Inputs'" v-bind:selectorVal="optional_files"
                              v-bind:def="isEnableOutputFileInput" v-bind:selectorId="booleanSelectorIDs[1]"
                              v-on:bool_selector="updateStore"></boolean-radio-button>
      </div>
      <div>
        <application-input-field class="interface-main" v-for="inp_id in getAppInputFieldIds" v-bind:key="inp_id"
                                 v-bind:input_id="inp_id"
                                 v-on:delete_input_field="deleteAppInterfaceInputField(inp_id);"></application-input-field>
      </div>
      <div class="entry">
        <button class="interface-btn" v-on:click="createAppInterfaceInputField();">Add Application <span>input</span>
        </button>
      </div>
      <div>
        <application-output-field class="interface-main" v-for="out_id in getAppOutputFieldIds" v-bind:key="out_id"
                                  v-bind:output_id="out_id"
                                  v-on:delete_output_field="deleteAppInterfaceOutputField(out_id);"></application-output-field>
      </div>
      <div class="entry">
        <div class="heading">Output fields</div>
        <button class="interface-btn" v-on:click="createAppInterfaceOutputField()">Add Application <span>output</span>
        </button>
      </div>
      <new-application-buttons v-bind:save="saveApplicationInterface" v-bind:cancel="cancelAction"
                               v-bind:sectionName="'Application Interface'"></new-application-buttons>
    </div>
  </div>
</template>
<script>
  import ApplicationInputField from './ApplicationInputField.vue';
  import BooleanRadioButton from './BooleanRadioButton.vue';
  import NewApplicationButtons from './TabActionConsole.vue';
  import ApplicationOutputField from './ApplicationOutputField.vue'
  import Loading from '../Loading.vue'

  import {createNamespacedHelpers} from 'vuex'
  import Vue from 'vue'

  const {mapGetters, mapActions} = createNamespacedHelpers('newApplication/appInterfaceTab')

  export default {
    components: {
      ApplicationInputField, BooleanRadioButton, NewApplicationButtons, ApplicationOutputField, Loading
    },
    data: function () {
      return {
        'id': 0,
        work_dir: {'boolValue': null},
        optional_files: {'boolValue': null},
        booleanSelectorIDs: ['archiveWorkingDirectory', 'hasOptionalFileInputs']
      };
    },
    props: {},
    mounted: function () {
      this.initializeAppInterface(this.mount)
    }
    ,
    computed: {
      ...
        mapGetters(['getAppInputFieldIds', 'getAppOutputFieldIds', 'isInitialized', 'isEnableArchiveWorkingDirectory', 'isEnableOutputFileInput'])
    }
    ,
    methods: {
      cancelAction: function () {
        this.resetState()
        console.log("Cancel called")
        this.mount()
      },
      updateStore: function (fieldName, newValue) {
        if (fieldName == this.booleanSelectorIDs[0]) {
          this.changeArchiveWorkingDirectory(newValue)
        } else if (fieldName == this.booleanSelectorIDs[1]) {
          this.changeEnableOutputFileInput(newValue)
        }
      }
      ,
      mount: function () {
        Vue.set(this, 'work_dir', {'boolValue': this.isEnableArchiveWorkingDirectory})
        this.optional_files = {'boolValue': this.isEnableOutputFileInput}
      },
      ...
        mapActions(['saveApplicationInterface', 'createAppInterfaceInputField', 'deleteAppInterfaceInputField', 'createAppInterfaceOutputField', 'deleteAppInterfaceOutputField', 'initializeAppInterface', 'changeEnableOutputFileInput', 'changeArchiveWorkingDirectory', 'resetState'])
    }
  }
  ;
</script>
<style>
  .interface-btn {
    color: #868E96;
    border: solid 1px #868E96;
    background-color: transparent;
    text-align: center;
    border-radius: 3px;
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 15px;
    padding-right: 15px;
  }

  .interface-btn span {
    font-weight: bold;
  }

  .interface-btn:hover {
    background-color: rgba(0, 105, 217, 1);
    color: white;
  }

  .entry.boolean-selectors {
    display: flex;
  }


</style>

