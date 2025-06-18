<template>
  <div>
    <div v-if="expiredReservations.length > 0">
      {{ expiredReservations.length }} expired
    </div>
    <div v-if="activeReservations.length > 0">
      {{ activeReservations.length }} active ({{
        activeReservationNames.join(", ")
      }})
    </div>
    <div v-if="upcomingReservations.length > 0">
      {{ upcomingReservations.length }} upcoming
    </div>
  </div>
</template>

<script>
export default {
  name: "compute-resource-reservations-summary",
  props: {
    reservations: {
      type: Array,
      required: true,
    },
  },
  computed: {
    expiredReservations() {
      return this.reservations
        ? this.reservations.filter((r) => r.isExpired)
        : [];
    },
    activeReservations() {
      return this.reservations
        ? this.reservations.filter((r) => r.isActive)
        : [];
    },
    activeReservationNames() {
      return this.activeReservations.map((r) => r.reservationName);
    },
    upcomingReservations() {
      return this.reservations
        ? this.reservations.filter((r) => r.isUpcoming)
        : [];
    },
  },
};
</script>
