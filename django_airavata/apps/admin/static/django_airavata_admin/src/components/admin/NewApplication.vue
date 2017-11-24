<template>
  <div class="new_app">
    <h3>{{getTitle}}</h3>
    <div class="main">
      <div class="tabs">
        <div class="tab" v-bind:class="tabs['details']"><router-link class="link" :to="{name:'details'}"><label class="lbl">Details</label></router-link></div>
        <div class="tab" v-bind:class="tabs['interface']" v-bind:obj="appInterfaceTabData"><router-link class="link" :to="{name:'interface'}"><label class="lbl">Interface</label></router-link></div>
        <div class="tab" v-bind:class="tabs['deployments']"><router-link class="link" :to="{name:'deployments'}"><label class="lbl">Deployments</label></router-link></div>
        <div class="tab" style="width: 100%"></div>
      </div>
      <transition mode="out-in">
        <router-view :key="$route.path"></router-view>
      </transition>
    </div>
  </div>
</template>
<script>
  import ApplicationDetails from './ApplicationDetails.vue'
  import ApplicationInterface from './ApplicationInterface.vue'

  import { createNamespacedHelpers } from 'vuex'

  const {mapGetters} = createNamespacedHelpers('newApplication')


  export default {
    components: {
      ApplicationDetails,ApplicationInterface
    },
    mounted:function () {
      this.current_active_tab=this.$route.name;
      this.previous_active_tab='';
    },
    data: function () {
      return {
        current_active_tab: 0,
        previous_active_tab: -1,
        appInterfaceTabData:{'inputFields':[]},

      }
    },
    computed: {
      tabs: function () {
        var tabs_active = {
          'details':'',
          'interface':'',
          'deployments':''
        };
        tabs_active[this.current_active_tab] = 'active';
        if (tabs_active.hasOwnProperty(this.previous_active_tab)) {
          tabs_active[this.previous_active_tab] = '';
        }
        return tabs_active;
      },
      ...mapGetters(["getTitle"])
    },
    watch:{
      '$route' (to, from) {
        if(!this.tabs.hasOwnProperty(to.name)){
          this.initialize(false)
        }
        this.previous_active_tab=from.name
        this.current_active_tab=to.name

      }
    }
  }
</script>
<style>
  .new_app {
    margin: 45px;
    width: 70%;
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




</style>
