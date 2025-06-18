import Vue from "vue";
import GatewayNoticesContainer from "./components/GatewayNoticesContainer";

new Vue({
  render(h) {
    return h(GatewayNoticesContainer, {
      props: {
        unreadCount: this.unreadCount,
        notices: this.notices,
      },
    });
  },
  data() {
    return {
      unreadCount: null,
      notices: null,
    };
  },
  beforeMount() {
    this.unreadCount = parseInt(this.$el.dataset.unreadCount);
    this.notices = JSON.parse(this.$el.dataset.notices);
  },
}).$mount("#gateway-notices");
