<template>
  <div class="boolean-selector-main">
    <div class="boolean-selector-heading">{{heading}}</div>
    <div class="boolean-selector">
      <div>
        <input type="radio" v-model="boolValue" value="true"/>
        <label>True</label>
      </div>
      <div>
        <input type="radio" v-model="boolValue" value="false"/>
        <label>False</label>
      </div>
    </div>
  </div>
</template>
<script>

  export default {
    mounted:function(){
      this.initialized=false
      this.boolValue=this.def!=null?this.def.toString():null
    },
    data:function () {
      return {
        'boolValue':null,
        'initialized':true
      }
    },
    props:{
      def:{
        type:Boolean,
        default:function () {
          return false;
        }
      },
      heading:{
        type:String
      },
      selectorId:{
        default:''
      }
    },
    methods:{
      triggerValueChangeEvent:function (value,oldValue) {
        if (this.initialized) {
          var val = value == null ? null : (value == 'true')
          this.$emit('bool_selector', this.selectorId, val)
        } else {
          this.initialized = true
        }

      }
    },
    watch:{
      boolValue:function(newValue){
          this.triggerValueChangeEvent(newValue);
      },
      def:function (newValue) {
        this.initialized=false
        if(this.boolValue!=newValue){
          this.boolValue=newValue
        }
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
