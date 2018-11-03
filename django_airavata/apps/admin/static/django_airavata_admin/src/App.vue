<template>
  <div class="vmain">
    <notifications-display />
    <Loading />
    <transition name="fade">
      <router-view>
      </router-view>
    </transition>
  </div>
</template>

<script>
import ExperimentsDashboard from "./components/dashboards/ExperimentDashboard.vue";
import CredentialStore from "./components/dashboards/CredentialStoreDashboard.vue";
import Loading from "./components/Loading.vue";
import ComputeResourceDashboard from "./components/dashboards/ComputeResourceDashboard";
import ComputeResourcePreferenceDashboard from "./components/dashboards/ComputeResourcePreferenceDashboard";
import { components } from 'django-airavata-common-ui'

export default {
  components: {
    ExperimentsDashboard,
    // AdminDashboard,
    CredentialStore,
    Loading,
    ComputeResourceDashboard,
    ComputeResourcePreferenceDashboard,
    "notifications-display": components.NotificationsDisplay
  }
};
</script>

