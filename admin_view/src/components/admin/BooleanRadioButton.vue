<template>
  <div class="boolean-selector-main">
    <div class="boolean-selector-heading">{{heading}}</div>
    <div class="boolean-selector">
      <div>
        <input type="radio" v-model="selectorVal.boolValue" value="true"/>
        <label>True</label>
      </div>
      <div>
        <input type="radio" v-model="selectorVal.boolValue" value="false"/>
        <label>False</label>
      </div>
    </div>
  </div>
</template>
<script>

  export default {
    mounted:function(){
      console.log('Selctor Value: '+ this.selectorVal.boolValue);
      this.selectorVal.boolValue=this.selectorVal.boolValue;
    },
    props:{
      selectorVal:{
        type:Object,
        default:function () {
          return {
            'boolValue':'false',
          };
        }
      },
      heading:{
        type:String
      },
      selectorId:{
        type:String,
        default:'na'
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
    font-size:1.1em;
    font-weight: 400;
    margin-bottom: 10px;
    color: black;
    width: 100%;

  }
</style>
