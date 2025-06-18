<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <slot name="title">
          <h1 class="h4 mb-4">{{ title }}</h1>
        </slot>
      </div>
      <div class="col-auto">
        <slot name="additional-buttons"> </slot>
        <slot name="new-item-button">
          <b-btn
            variant="primary"
            @click="addNewItem"
            :disabled="newButtonDisabled"
          >
            {{ newItemButtonText }}
            <i class="fa fa-plus" aria-hidden="true"></i>
          </b-btn>
        </slot>
      </div>
    </div>
    <div v-if="subtitle" class="row">
      <div class="col">
        <h2 class="subtitle text-uppercase text-muted">{{ subtitle }}</h2>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <slot name="new-item-editor"></slot>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <slot name="item-list" :items="itemsList">Item List goes here</slot>
        <pager
          v-if="itemsPaginator"
          :paginator="itemsPaginator"
          next="nextItems"
          v-on:previous="previousItems"
        ></pager>
      </div>
    </div>
  </div>
</template>

<script>
import { utils } from "django-airavata-api";
import Pager from "../components/Pager.vue";

export default {
  props: {
    items: Array,
    itemsPaginator: utils.PaginationIterator,
    title: {
      type: String,
      default: "Items",
    },
    subtitle: {
      type: String,
    },
    newItemButtonText: {
      type: String,
      default: "New Item",
    },
    newButtonDisabled: {
      type: Boolean,
      default: false,
    },
  },
  name: "list-layout",
  data() {
    return {};
  },
  components: {
    pager: Pager,
  },
  methods: {
    nextItems: function () {
      this.itemsPaginator.next();
    },
    previousItems: function () {
      this.itemsPaginator.previous();
    },
    addNewItem: function () {
      this.$emit("add-new-item");
    },
  },
  computed: {
    itemsList: function () {
      return this.itemsPaginator ? this.itemsPaginator.results : this.items;
    },
  },
};
</script>

<style scoped>
.subtitle {
  font-size: 14px;
}
</style>
