<template>
  <div class="btns">
    <transition name="fade"><label v-if="msg" class="msg" v-bind:class="status">{{msg}}</label></transition>
    <input class="vbtn vbtn-cancel" type="button" value="Cancel" v-on:click="cancel" v-if="enableCancel"/>
    <input class="vbtn vbtn-default" type="button" value="Save" v-on:click="saveFn"/>
  </div>
</template>
<script>
  export default {
    data: function () {
      return {msg: null}
    },
    props: {
      save: {
        type: Function,
        default: function () {
          console.warn("SAVE Function has not been set")
        }
      },
      cancel: {
        type: Function,
        default: function () {
          console.warn("CANCEL Function has not been set")
        }
      },
      sectionName:{
        type:String,
        default:""
      },
      enableCancel:{
        type:Boolean,
        default:true
      }
    },
    methods: {
      saveFn: function () {
        this.save({
          success: (value) => {
            console.log("Save Value",value)
            this.msg = "Saved "+this.sectionName+" Successfully"
            this.status="msg-success"
            var tempThis=this
            setTimeout(function () {
              console.log("TimedOut")
              tempThis.msg = null
            }, 5000)
          },
          failure: (response) => {
            console.log("Failure Value",response)
            this.msg = this.msg = "Saving "+this.sectionName+" Failed"
            this.status="msg-failure"
            var tempThis=this
            setTimeout(function () {
              console.log("TimedOut")
              tempThis.msg = null
            }, 5000)
          }
        })
      }
    }
  }
</script>
<style>
  .btns input {
    float: right;
  }
  .msg{
    width: 60%;
    height: 30px;
    text-align: center;
    border-radius: 5px;
  }
  .msg-success{
    background-color: #3ca41a;
    color: #f1fff3;
    transition-timing-function: ea  ;
  }

  .msg-failure{
    background-color: #ff0b03;
    color: white;
  }
</style>

