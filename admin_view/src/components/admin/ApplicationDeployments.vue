<template>
  <div class="main_section">

    <div class="new-application-tab-main">
      <h4>Application Deployments</h4>
      <div class="entry">
        <div class="heading">Application module</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading">Application compute host</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading">Application executable path</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading">Application Parallelism type</div>
        <input type="text"/>
      </div>
      <div class="entry">
        <div class="heading" >Application deployment description</div>
        <textarea  style="height: 80px;" type="text"/>
      </div>
    </div>
  </div>
</template>
<style>
  .heading{
    font-size:1.0em;
    font-weight: bold;
    margin-bottom: 10px;
  }
</style>

