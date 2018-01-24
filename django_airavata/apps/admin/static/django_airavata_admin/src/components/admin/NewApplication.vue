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
      <transition name="fade">
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




</style>
