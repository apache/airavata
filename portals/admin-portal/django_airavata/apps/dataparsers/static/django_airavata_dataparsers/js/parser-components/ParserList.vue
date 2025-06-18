<template>
  <div>
    <b-alert
      dismissible
      :variant="alertVariant"
      :show="showDismissibleAlert"
      @dismissed="showDismissibleAlert = false"
      >{{ alertMsg }}</b-alert
    >
    <table class="table table-hover">
      <thead>
        <tr>
          <th>Name</th>
          <th>Image Name</th>
          <th>Execution Command</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <parser-list-item
          v-bind:parser="parser"
          v-for="parser in parsers"
          v-bind:key="parser.id"
        >
        </parser-list-item>
      </tbody>
    </table>
  </div>
</template>

<script>
import ParserListItem from "./ParserListItem.vue";

export default {
  name: "parser-list",
  props: ["parsers"],
  data: function () {
    return {
      owner: "owner",
      alertMsg: null,
      alertVariant: "primary",
      showDismissibleAlert: false,
    };
  },
  components: {
    ParserListItem,
  },
  methods: {},
};
</script>

<style>
#parser-list-actions-header {
  min-width: 150px;
}
</style>
