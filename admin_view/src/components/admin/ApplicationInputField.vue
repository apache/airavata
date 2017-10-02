<template>
  <div class="main_section interface-main" >
    <div class="input-field-header">
      Input Fields
      <img v-on:click="delete_event_trigger();" src="/static/images/delete.png"/>
    </div>
    <div class="entry">
      <div class="heading">Name</div>
      <input type="text" v-model="name"/>
    </div>
    <div class="entry">
      <div class="heading" >Value</div>
      <input type="text" v-model="value"/>
    </div>
    <div class="entry">
      <div class="heading" >Type</div>
      <input type="text" v-model="type"/>
    </div>
    <div class="entry">
      <div class="heading">Application argument</div>
      <input v-model="appArg" type="text"/>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Data is staged'" v-bind:selectorId="dataStaged" v-on:bool_selector="boolValueHandler"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Required'" v-bind:selectorId="required" v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Required on command line'" v-bind:selectorId="requiredOnCmd" v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'

  import { createNamespacedHelpers } from 'vuex'

  const {mapGetters,mapActions} = createNamespacedHelpers('appInterfaceTab')

  export default {
    components:{
      BooleanRadioButton
    },
    created:function () {
    },
    methods:{
      delete_event_trigger:function(){
        this.$emit('delete_input_field');
      },
      boolValueHandler:function (selectorID,value) {
        console.log('Event Capture',selectorID,value);
        this.updateStore(selectorID,value)
      },
      syncDataFromStore:function () {
        console.log(this.input_id)
        var val=this.getAppInputField(this.input_id)
        this.name=val['name']
        this.value=val['value']
        this.type=val['type']
        this.appArg=val['appArg']
      },
      updateStore:function (fieldName,newValue) {
        var param={
          'id':this.input_id,
        };
        var update={}
        update[fieldName]=newValue
        param['update']=update
        this.updateFieldValues(param)
      },
      ...mapActions(['updateFieldValues'])
    },
    mounted:function(){
      this.syncDataFromStore()
    },
    data:function () {
      return{
        'dataStaged':'dataStaged',
        'required':'required',
        'requiredOnCmd':'requiredOnCmd',
        name:'',
        value:'',
        type:'',
        appArg:''
      }
    },
    props:['input_id'],
    computed:{
      ...mapGetters(['getAppInputField'])
    },
    watch:{
      name:function (newValue) {
        this.updateStore('name',newValue)
      },
      value:function (newValue) {
        this.updateStore('value',newValue)

      },
      type:function (newValue) {
        this.updateStore('type',newValue)
      },
      appArg:function (newValue) {
        this.updateStore('appArg',newValue)
      }
    }
  }
</script>

<style>
  .input-field-header{
    background-color: #F8F8F8;
    width: 100%;
    padding: 15px;
    border: solid 1px #dddddd;
    text-align: left;
  }

  .input-field-header img{
    float: right;
    }



  .main_section.interface-main .entry{
    margin-bottom: 40px;
    margin-left:15px;
    margin-right: 15px;
  }

  .entry.boolean-selectors{
    display: flex;
  }

  .entry.boolean-selectors div{
    margin-right: 60px;
  }

  .interface-main{
    border: solid 1px #dddddd;
    border-radius: 4px;
  }

</style>
