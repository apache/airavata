<template>
  <div class="new_app">
    <h3>Create A New Application</h3>
    <div class="main">
      <div class="tabs">
        <div class="tab" v-bind:class="tabs[0]" v-on:click="tab_handler(0)"><router-link class="link" :to="{name:'details'}"><label class="lbl">Details</label></router-link></div>
        <div class="tab" v-bind:class="tabs[1]" v-on:click="tab_handler(1)"><router-link class="link" :to="{name:'interface'}"><label class="lbl">Interface</label></router-link></div>
        <div class="tab" v-bind:class="tabs[2]" v-on:click="tab_handler(2)"><router-link class="link" :to="{name:'deployments'}"><label class="lbl">Deployments</label></router-link></div>
        <div class="tab" style="width: 100%"></div>
      </div>
      <router-view></router-view>
    </div>
  </div>
</template>
<script>
  import ApplicationDetails from './ApplicationDetails.vue'
  import ApplicationInterface from './ApplicationInterface.vue'
  export default {
    components: {
      ApplicationDetails,ApplicationInterface
    },

    data: function () {
      return {
        current_active_tab: 0,
        previous_active_tab: -1
      };
    },
    computed: {
      tabs: function () {
        var tabs_active = new Array(3).fill('');
        tabs_active[this.current_active_tab] = 'active';
        if (this.previous_active_tab > 0 && this.previous_active_tab < 3) {
          tabs_active[this.previous_active_tab] = '';
        }
        return tabs_active;
      }
    },
    methods: {
      tab_handler: function (tab_id) {
        if (this.current_active_tab != tab_id) {
          this.previous_active_tab = this.current_active_tab;
          this.current_active_tab = tab_id;
        }
      }
    }
  }
</script>
<style>
  .new_app {
    margin: 45px;
  }

  .main {
    width: 100%;
    margin-top: 50px;
  }

  .tab {
    text-align: center;
    width: 120px;
    margin-bottom: 15px;
    border-bottom: solid #999999 1px;
    color: #007BFF;
  }

  .tab .lbl:hover {
    cursor: pointer;
  }

  .active .lbl:hover {
    cursor: default;
  }

  .lbl {
    margin: 10px;
    color: inherit;
  }

  .link{
    color: inherit;
  }

  .active {
    color: #333333;
    border-top: solid #999999 1px;
    border-left: solid #999999 1px;
    border-right: solid #999999 1px;
    border-bottom: hidden;
    border-top-right-radius: 3px;
    border-top-left-radius: 3px;
  }

  .tabs {
    display: flex;
    width: 100%;
  }

  .main_section {
    width: 100%;
    display: block;
    margin-top: 50px;
  }


</style>
