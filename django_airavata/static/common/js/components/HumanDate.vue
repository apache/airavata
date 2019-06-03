<template>
  <span :title="date.toString()">{{ fromNow }}</span>
</template>
<script>
import moment from "moment";
export default {
  name: "human-date",
  props: {
    date: Date
  },
  computed: {
    fromNow() {
      return moment(this.date).fromNow();
    }
  }
};
</script>

