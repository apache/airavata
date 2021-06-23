<template>
  <div class="pager">
    <span class="pager-element" v-if="hasPrevious">
      <a href="#" class="action-link" v-on:click.prevent="getPrevious"
        ><i class="fa fa-chevron-left" aria-hidden="true"></i> Previous</a
      >
    </span>
    <span class="pager-element"> Showing {{ first }} - {{ last }} </span>
    <span class="pager-element" v-if="hasNext">
      <a href="#" class="action-link" v-on:click.prevent="getNext"
        >Next <i class="fa fa-chevron-right" aria-hidden="true"></i
      ></a>
    </span>
  </div>
</template>

<script>
import { utils } from "django-airavata-api";

export default {
  props: {
    paginator: utils.PaginationIterator,
  },
  name: "pager",
  methods: {
    getNext: function () {
      this.$emit("next");
    },
    getPrevious: function () {
      this.$emit("previous");
    },
  },
  computed: {
    hasNext: function () {
      return this.paginator && this.paginator.hasNext();
    },
    hasPrevious: function () {
      return this.paginator && this.paginator.hasPrevious();
    },
    first: function () {
      return this.paginator ? this.paginator.offset + 1 : null;
    },
    last: function () {
      if (this.paginator) {
        if (this.paginator.count) {
          return Math.min(
            this.paginator.offset + this.paginator.limit,
            this.paginator.count
          );
        } else {
          return this.paginator.offset + this.paginator.results.length;
        }
      } else {
        return null;
      }
    },
  },
};
</script>

<style scoped>
.pager {
  text-align: right;
}
.pager-element + .pager-element {
  margin-left: 5px;
}
</style>
