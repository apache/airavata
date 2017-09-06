<template>
  <div class="main_section">
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
    <div class="btns">
      <input class="cancel" type="button" value="Cancel"/>
      <input class="save" type="button" value="Save"/>
    </div>
  </div>
</template>
<script>

  export default{
    data:function () {
      return {

      };
    }
  }

</script>
<style>
  .entry{
    margin-top:20px;
    display: block;

  }

  .entry .heading{
    font-size:1.0em;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .entry input,textarea{
    width: 100%;
    border-radius: 2px;
    border: solid #dddddd 1px;;
  }

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
