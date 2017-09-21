<template>
  <div class="main_section interface-main">
    <div class="input-field-header">
      Input Fields
      <img v-on:click="delete_event_trigger();" src="/static/images/delete.png"/>
    </div>
    <div class="entry">
      <div class="heading">Name</div>
      <input type="text" v-model="data.name"/>
    </div>
    <div class="entry">
      <div class="heading" v-model="data.value">Value</div>
      <input type="text"/>
    </div>
    <div class="entry">
      <div class="heading" v-model="data.type">Type</div>
      <input type="text"/>
    </div>
    <div class="entry">
      <div class="heading">Application argument</div>
      <input v-model="data.appArg" type="text"/>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Data is staged'" v-bind:selectorVal="data.dataStaged"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Required'" v-bind:selectorVal="data.required"></boolean-radio-button>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Required on command line'" v-bind:selectorVal="data.requiredOnCmd"></boolean-radio-button>
    </div>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'
  export default {
    components:{
      BooleanRadioButton
    },
    methods:{
      delete_event_trigger:function(){
        this.$emit('delete_input_field');
      }
    },
    created:function(){
      this.data=this.data;
    },
    props:{
      data:{
        type:Object,
        default:function () {
          return{
            name:'',
            value:'',
            type:'',
            appArg:'',
            dataStaged:{'boolValue':'true'},
            required:{'boolValue':'false'},
            requiredOnCmd:{'boolValue':'false'}
          };
        }
      }
    },
    watch:{
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
