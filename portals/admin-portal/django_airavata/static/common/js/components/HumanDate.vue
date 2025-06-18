<template>
  <abbr :title="date.toString()">{{ fromNow }}</abbr>
</template>
<script>
import moment from "moment";
export default {
  name: "human-date",
  props: {
    date: Date,
  },
  computed: {
    fromNow() {
      return moment(this.date).fromNow();
    },
  },
};
</script>
