<template>
  <div class="vmain">
    <notifications-display />
    <transition name="fade">
      <router-view>
      </router-view>
    </transition>
  </div>
</template>

<script>
import { components } from 'django-airavata-common-ui'

export default {
  components: {
    "notifications-display": components.NotificationsDisplay
  }
};
</script>

