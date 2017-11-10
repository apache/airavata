<template>
  <div class="btns">
    <label>{{msg}}</label>
    <input class="cancel" type="button" value="Cancel" v-on:click="$emit('cancel')"/>
    <input class="save" type="button" value="Save" v-on:click="saveFn"/>
  </div>
</template>
<script>
  export default {
    data:function () {
      return {msg:""}
    },
    props:{
      save:{
        type:Function,
        default:function(){

        }
      },
      cancel:{
        type:Function,
        default:function(){

        }
      }
    },
    methods:{
        saveFn:function () {
          var temp=this.save()
          console.log("Saving",temp)
          var med=this
          setTimeout(function () {
            console.log("TimedOut")
            med.msg=""
          },5000)
      }
    }
  }
</script>
<style>
  .btns{
    margin-top:50px;
    display: inline-block;
    width: 100%;
  }

  .btns input{
    float: right;
    margin-left: 20px;
    text-align: center;
    border-color: #007BFF;
    border-style: solid;
    border-radius: 3px;
    padding-top: 5px;
    padding-bottom:5px;
    padding-left:15px;
    padding-right: 15px;
  }

  .btns input:hover{
    background-color: #3ca41a;
    color: #f1fff3;
  }

  .save{
    background-color: #007BFF;
    color: white;
  }

  .cancel{
    color:  #007BFF;
    background-color: white;
  }

</style>
