<template>
  <div class="btns">
    <input class="cancel" type="button" value="Cancel"/>
    <input class="save" type="button" value="Save"/>
  </div>
</template>
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
