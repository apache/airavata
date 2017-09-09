<template>
  <div class="main_section interface-main">
    <div class="input-field-header">Input Fields</div>
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
    <boolean-radio-button v-bind:heading="'Data is staged'"></boolean-radio-button>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'
  export default {
    components:{
      BooleanRadioButton
    },
    methods:{}
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

  .interface-main{
    width: 65%;
    border: solid 1px #dddddd;
    border-radius: 4px;
  }

  .main_section.interface-main .entry{
    margin-bottom: 40px;
    margin-left:15px;
    margin-right: 15px;
  }



</style>
