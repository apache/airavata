<template>
  <li class="feed__list-item">
    <span v-if="feedItem.type" class="feed__label text-secondary">{{
      feedItem.type
    }}</span>
    <h2 class="feed__title mb-2">
      <a v-if="feedItem.url" :href="feedItem.url">{{ feedItem.title }}</a>
      <span v-else>{{ feedItem.title }}</span>
    </h2>
    <slot v-bind:feedItem="feedItem">
      <div v-if="feedItem.description">{{ feedItem.description }}</div>
    </slot>
    <div v-if="timestamp" class="feed__item-meta text-secondary mt-1">
      <span>Updated </span> <time>{{ timestamp }}</time>
    </div>
  </li>
</template>

<script>
import moment from "moment";

export default {
  name: "sidebar-feed-item",
  props: {
    /**
     * feedItem properties are
     * - type (String, Optional) the type of feed item (e.g. for Experiments this is the application name)
     * - url (String, Optional) url to link to the full item details
     * - title (String, Required) title of the feed item
     * - timestamp (Date, Optional) timestamp of when feed item was created/updated
     * - description (String, Optional) description of feed item. Displayed when no slot is provided.
     */
    feedItem: Object,
  },
  computed: {
    timestamp() {
      if (this.feedItem.timestamp) {
        return moment(this.feedItem.timestamp).fromNow();
      } else {
        return null;
      }
    },
  },
};
</script>
