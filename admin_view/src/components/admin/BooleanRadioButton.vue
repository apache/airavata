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
    created:function(){
      this.boolValue=this.def.toString();
    },
    data:function () {
      return {
        'boolValue':'false'
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
        type:String,
        default:''
      }
    },
    methods:{
      triggerValueChangeEvent:function (value) {
        var update=[this.selectorId,(value=='true')]
        console.log('Triggered Change event')
        this.$emit('bool_selector',this.selectorId,(value=='true'))
      }
    },
    watch:{
      boolValue:function(newValue){
            this.triggerValueChangeEvent(newValue);
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
