<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Parsers</h1>
      </div>
      <div id="col-new-group" class="col-sm-2">
        <b-button href="create" :variant="'primary'"
          >Create New Parser&nbsp;&nbsp;<i
            class="fa fa-plus"
            aria-hidden="true"
          ></i
        ></b-button>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <parser-list v-bind:parsers="parsers"></parser-list>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ParserList from "../parser-components/ParserList.vue";

import { services } from "django-airavata-api";

export default {
  name: "parsers-manage-container",
  props: [],
  data() {
    return {
      parsers: null,
    };
  },
  components: {
    "parser-list": ParserList,
  },
  methods: {
    nextParsers: function () {
      this.parserPaginator.next();
    },
    previousParsers: function () {
      this.parserPaginator.previous();
    },
  },
  computed: {
    // parsers: function() {
    //     return this.parserPaginator ? this.parserPaginator.results : null;
    // },
  },
  beforeMount: function () {
    services.ParserService.list().then((result) => (this.parsers = result));
  },
};
</script>

<style>
#col-new-group {
  text-align: right;
}
#modal-new-group {
  text-align: left;
}
</style>
