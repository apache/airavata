<template>
  <div class="main_section">
    <h4>Application Interface</h4>
    <application-input-field></application-input-field>
    <div class="entry">
      <button class="interface-btn">Add Application <span>input</span></button>
    </div>
  </div>
</template>
<script>
  import ApplicationInputField from './ApplicationInputField.vue'
  export default {
    components:{
      ApplicationInputField
    },
    data:function () {
      return {
        'inputFields':[{}]
      };
    },
    methods:{
      addApplicationInput:function () {

      }
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
</style>

