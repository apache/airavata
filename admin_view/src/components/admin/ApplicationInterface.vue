<template>

  <div class="main_section">

    <div class="new-application-tab-main">
      <h4>Application Interface</h4>
      <div class="entry boolean-selectors">
        <boolean-radio-button v-bind:heading="'Enable Archiving Working Directory'" v-bind:selectorVal="work_dir" v-bind:selectorId="booleanSelectorIDs[0]" v-on:bool_selector="updateStore"></boolean-radio-button>
        <boolean-radio-button v-bind:heading="'Enable Optional File Inputs'" v-bind:selectorVal="optional_files" v-bind:def="'true'" v-bind:selectorId="booleanSelectorIDs[1]"  v-on:bool_selector="updateStore"></boolean-radio-button>
      </div>
      <div>
        <application-input-field class="interface-main"  v-for="inp_id in getAppInputFieldsId" v-bind:key="inp_id" v-bind:input_id="inp_id" v-on:delete_input_field="deleteAppInterfaceInputField(inp_id);"></application-input-field>
      </div>
      <div class="entry">
        <button class="interface-btn" v-on:click="createAppInterfaceInputField();">Add Application <span>input</span></button>
      </div>
      <div class="entry">
        <div class="heading">Output fields</div>
        <button class="interface-btn">Add Application <span>output</span></button>
      </div>
      <new-application-buttons></new-application-buttons>
    </div>
  </div>
</template>
<script>
  import ApplicationInputField from './ApplicationInputField.vue';
  import BooleanRadioButton from './BooleanRadioButton.vue';
  import NewApplicationButtons from './NewApplicationButtons.vue';

  import { createNamespacedHelpers } from 'vuex'

  const {mapGetters,mapActions} = createNamespacedHelpers('appInterfaceTab')

  export default {
    components:{

      ApplicationInputField,BooleanRadioButton,NewApplicationButtons
    },
    data:function () {
      return {
        'id':0,
        work_dir:{'boolValue':'false'},
        optional_files:{'boolValue':'true'},
        booleanSelectorIDs:['enableArchiveWorkingDirectory','enableOutputFileInputs']
      };
    },
    props:{
    },
    mounted:function () {
      if(!this.isInitialized){
        var id=this.createAppInterfaceInputField();
        console.log('Mounted',id);
        this.initialized(true)
        this.work_dir={'boolValue':this.isEnableArchiveWorkingDirectory().toString()}
        this.optional_files={'boolValue':this.isEnableOutputFileInput().toString()}
      }

    },
    computed:{
      ...mapGetters(['getAppInputFieldsId','isInitialized','isEnableArchiveWorkingDirectory','isEnableOutputFileInput'])
    },
    methods:{
      updateStore:function (fieldName,newValue) {
        if(fieldName == this.booleanSelectorIDs[0] ){
            this.changeEnableArchiveWorkingDirectory(newValue)
        }else if(fieldName == this.booleanSelectorIDs[1]){
          this.changeEnableOutputFileInput(newValue)
          console.log(fieldName)
        }
      },
      ...mapActions(['createAppInterfaceInputField','deleteAppInterfaceInputField','initialized','changeEnableOutputFileInput','changeEnableArchiveWorkingDirectory'])
    }
  };
</script>
<style>
 .interface-btn{
   color: #868E96;
   border: solid 1px #868E96;
   background-color: transparent;
   text-align: center;
   border-radius: 3px;
   padding-top: 5px;
   padding-bottom:5px;
   padding-left:15px;
   padding-right: 15px;
 }
  .interface-btn span{
    font-weight: bold;
  }

 .interface-btn:hover{
   background-color: rgba(0,105,217,1);
   color: white;
 }

 .entry.boolean-selectors{
   display: flex;
 }


</style>

