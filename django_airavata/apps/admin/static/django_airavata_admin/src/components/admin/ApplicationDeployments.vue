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
        <div class="heading">Application deployment description</div>
        <textarea style="height: 80px;" type="text"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <h4>Module load commands</h4>
      <div class="entry">
        <div class="heading">Module load commands</div>
        <input type="text"/>
      </div>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" value="Add command"/>
      </div>
    </div>

    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Library Prepend Paths</h4>
        <div class="name_value">
          <input type="text" placeholder="Name"/>
          <input type="text" placeholder="Value"/>
        </div>
        <input type="button" class="deployment btn" value="Add path"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Library Append Paths</h4>
        <div class="name_value">
          <input type="text" placeholder="Name"/>
          <input type="text" placeholder="Value"/>
        </div>
        <input type="button" class="deployment btn" value="Add path"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Environments</h4>
        <div class="name_value">
          <input type="text" placeholder="Name"/>
          <input type="text" placeholder="Value"/>
        </div>
        <input type="button" class="deployment btn" value="Add environment"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Pre Job Commands</h4>
        <div class="entry">
          <input type="text"/>
        </div>
        <input type="button" class="deployment btn" value="Add Pre Job command"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Post Job Commands</h4>
        <div class="entry">
          <input type="text"/>
        </div>
        <input type="button" class="deployment btn" value="Add Post Job command"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Defaults</h4>
        <div class="entry">
          <div class="heading">Default Node Count</div>
          <input type="number" value="1" min="0"/>
        </div>
        <div class="entry">
          <div class="heading">Default CPU Count</div>
          <input type="number" value="1" min="0"/>
        </div>
        <div class="entry">
          <div class="heading">Default Queue Name</div>
          <select>
            <option value="cpu">CPU</option>
            <option value="gpu">GPU</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>
<script>

</script>
<style>
  .heading {
    font-size: 1.0em;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .deployment.btn {
    float: right;
    text-align: center;
    border-color: #007BFF;
    border-style: solid;
    border-radius: 3px;
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 15px;
    padding-right: 15px;
    color: #007BFF;
    background-color: white;
    width: auto;
    float: left;
  }

  .deployment.btn:hover {
    color: white;
    background-color: rgba(0, 105, 217, 1);
  }

  .deployment-entry {
    display: inline-block;
    margin-top: 15px;
    width: 100%;
  }

  .name_value {
    display: inline-flex;
    width: 100%;
    margin-bottom: 5px;
  }

  .name_value input {
    width: 50%;
    display: inline-flex;
    margin-right: 5px;
  }

  .deployment-entry .entry {
    margin-bottom: 5px;
  }
</style>

