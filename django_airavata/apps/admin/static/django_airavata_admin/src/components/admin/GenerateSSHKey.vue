<template>
  <div class="generate">
    <input type="text" placeholder="Add Description" v-model="description"/>
    <button class="vbtn vbtn-default" v-on:click="createSHHKeys()">Generate</button>
  </div>
</template>
<script>
  import Utils from '../../utils'
export default {
    data:function(){
      return {
        "description":null
      }
    },
  methods:{
      createSHHKeys:function () {
        Utils.post('/api/credentials/ssh/key/create',{'description':this.description},{success:(value)=>{
          this.$emit("created")
          this.description=""
        }
        ,failure:(value)=>this.$emit("failed")})
      }
  }
}
</script>
<style>
  .generate {
    padding: 10px;
    display: inline-flex;
    width: 100%;
    height: 100%;
  }
</style>
