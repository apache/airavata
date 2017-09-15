<template>
  <div class="main_section interface-main">
    <div class="input-field-header">
      Input Fields
      <img src="/static/images/delete.png"/>
    </div>
    <div class="entry">
      <div class="heading">Name</div>
      <input type="text"/>
    </div>
    <div class="entry">
      <div class="heading">Value</div>
      <input type="text"/>
    </div>
    <div class="entry">
      <div class="heading">Type</div>
      <input type="text"/>
    </div>
    <div class="entry">
      <div class="heading">Application argument</div>
      <input type="text"/>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Data is staged'" v-bind:selectorVal="dataStaged"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Required'"></boolean-radio-button>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Required on command line'"></boolean-radio-button>
    </div>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'
  export default {
    components:{
      BooleanRadioButton
    },
    methods:{},
    props:{
      data:{
        type:Object,
        default:function () {
          return{
          };
        }
      },
      dataStaged:{
        type:Object,
        default:function () {
          return {'boolValue':'true'};
        }
      }
    },
    watch:{
      dataStaged:function (newValue, oldValue) {
        console.log('Data Changed:'+newValue+' '+oldValue);
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
