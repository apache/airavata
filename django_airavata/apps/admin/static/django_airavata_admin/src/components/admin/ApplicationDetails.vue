<template>

  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Application Details</h4>
      <div class="entry">
        <div class="heading">Application Name</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading">Application Version</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading" >Experiment Description</div>
        <textarea  style="height: 80px;" type="text"/>
      </div>
      <new-application-buttons></new-application-buttons>
    </div>
  </div>
</template>
<script>
  import NewApplicationButtons from './NewApplicationButtons.vue';
  export default{
    components:{
      NewApplicationButtons
    },
    data:function () {
      return {

      };
    }
  }

</script>
<style>




</style>
