<template>
  <div class="boolean-selector-main">
    <div class="boolean-selector-heading">{{heading}}</div>
    <div class="boolean-selector">
      <div>
        <input type="radio" v-bind:checked="val" v-on:click="val=!val;"/>
        <label>True</label>
      </div>
      <div>
        <input type="radio" v-bind:checked="!val"  v-on:click="val=!val;"/>
        <label>False</label>
      </div>
    </div>
  </div>
</template>
<script>

  export default {
    data:function() {
      return{
        'val':false,
        'selctor-id':'na'
      };
    },
    props:['val','heading','selctor-id'],
    watch:{
      val:function (newVal,oldVal) {
        this.$emit('boolean-selector',this.selector-id,this.val);
      }
    }
  }
</script>
<style>
  .boolean-selector-main{
    display: block;
  }
  .boolean-selector{
    display: flex;
    font-weight: 100;
  }

  .boolean-selector div{
    display: flex;
    margin-right: 25px;
  }

  .boolean-selector div label{
    margin-left: 10px;
    text-align: center;
    font-weight: bold;
    padding-top: 4px;
  }

  .boolean-selector-heading{
    font-size:1.3em;
    font-weight: 400;
    margin-bottom: 10px;

  }
</style>
