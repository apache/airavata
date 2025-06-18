<template>
  <ol v-if="feedItems && feedItems.length > 0" class="feed">
    <sidebar-feed-item
      v-for="feedItem in feedItems"
      :feed-item="feedItem"
      :key="feedItem.id"
    >
      <div slot-scope="slotProps">
        <slot name="description" :feedItem="slotProps.feedItem" />
      </div>
    </sidebar-feed-item>
  </ol>
</template>

<script>
import SidebarFeedItem from "./SidebarFeedItem.vue";

export default {
  name: "sidebar-feed",
  props: {
    feedItems: {
      type: Array,
      default: function () {
        return [];
      },
    },
  },
  components: {
    SidebarFeedItem,
  },
};
</script>
