<template>
  <list-layout
    @add-new-item="addNewReservation"
    :items="decoratedReservations"
    title="Reservations"
    new-item-button-text="New Reservation"
    :newButtonDisabled="readonly"
  >
    <template slot="additional-buttons">
      <delete-button
        class="mr-2"
        @delete="deleteAllExpiredReservations"
        label="Delete All Expired"
        :disabled="expiredReservations.length === 0"
      >
        Are you sure you want to delete all expired reservations?
      </delete-button>
    </template>
    <template slot="new-item-editor">
      <b-card v-if="showNewItemEditor" title="New Reservation">
        <compute-resource-reservation-editor
          v-model="newReservation"
          :queues="queues"
          @valid="
            newReservationValid = true;
            validate();
          "
          @invalid="
            newReservationValid = false;
            validate();
          "
        />
        <div class="row">
          <div class="col">
            <b-button
              variant="primary"
              @click="saveNewReservation"
              :disabled="isSaveDisabled"
            >
              Add
            </b-button>
            <b-button variant="secondary" @click="cancelNewReservation">
              Cancel
            </b-button>
          </div>
        </div>
      </b-card>
    </template>
    <template slot="item-list" slot-scope="slotProps">
      <b-table hover :fields="fields" :items="slotProps.items">
        <template slot="cell(reservationName)" slot-scope="data">
          {{ data.value }}
          <b-badge v-if="data.item.isExpired">Expired</b-badge>
          <b-badge v-if="data.item.isActive" variant="success">Active</b-badge>
          <b-badge v-if="data.item.isUpcoming" variant="info">Upcoming</b-badge>
        </template>
        <template slot="cell(queueNames)" slot-scope="data">
          <ul v-for="queueName in data.item.queueNames" :key="queueName">
            <li>{{ queueName }}</li>
          </ul>
        </template>
        <template slot="cell(action)" slot-scope="data">
          <b-link
            v-if="!readonly"
            class="action-link"
            @click="toggleDetails(data)"
            :disabled="isReservationInvalid(data.item.key)"
          >
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </b-link>
          <delete-link
            v-if="!readonly"
            class="action-link"
            @delete="deleteReservation(data.item)"
          >
            Are you sure you want to delete reservation
            <strong>{{ data.item.reservationName }}</strong
            >?
          </delete-link>
        </template>
        <template slot="row-details" slot-scope="row">
          <b-card>
            <compute-resource-reservation-editor
              :value="row.item"
              @input="updatedReservation"
              :queues="queues"
              @valid="removeInvalidReservation(row.item.key)"
              @invalid="recordInvalidReservation(row.item.key)"
            />
            <b-button
              size="sm"
              @click="toggleDetails(row)"
              :disabled="isReservationInvalid(row.item.key)"
              >Close</b-button
            >
          </b-card>
        </template>
      </b-table>
    </template>
  </list-layout>
</template>

<script>
import { models } from "django-airavata-api";
import { components, layouts, utils } from "django-airavata-common-ui";
import ComputeResourceReservationEditor from "./ComputeResourceReservationEditor";

export default {
  name: "compute-resource-reservation-list",
  components: {
    "delete-link": components.DeleteLink,
    "human-date": components.HumanDate,
    "list-layout": layouts.ListLayout,
    ComputeResourceReservationEditor,
    "delete-button": components.DeleteButton,
  },
  props: {
    reservations: {
      type: Array,
      required: true,
    },
    queues: {
      type: Array,
      required: true,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showingDetails: {},
      showNewItemEditor: false,
      newReservation: null,
      newReservationValid: false,
      invalidReservations: [], // list of ComputeResourceReservation.key
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Name",
          key: "reservationName",
        },
        {
          label: "Queues",
          key: "queueNames",
        },
        {
          label: "Start Time",
          key: "startTime",
          formatter: (value) =>
            utils.dateFormatters.dateTimeInMinutesWithTimeZone.format(value),
        },
        {
          label: "End Time",
          key: "endTime",
          formatter: (value) =>
            utils.dateFormatters.dateTimeInMinutesWithTimeZone.format(value),
        },
        {
          label: "Action",
          key: "action",
        },
      ];
    },
    decoratedReservations() {
      return this.reservations
        ? this.reservations.map((res) => {
            const resClone = res.clone();
            resClone._showDetails = this.showingDetails[resClone.key];
            return resClone;
          })
        : [];
    },
    isSaveDisabled() {
      return !this.newReservationValid;
    },
    valid() {
      return (
        (!this.showNewItemEditor || this.newReservationValid) &&
        this.invalidReservations.length === 0
      );
    },
    expiredReservations() {
      return this.reservations
        ? this.reservations.filter((r) => r.isExpired)
        : [];
    },
  },
  created() {},
  methods: {
    updatedReservation(newValue) {
      this.$emit("updated", newValue);
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.key] = !this.showingDetails[row.item.key];
    },
    deleteReservation(reservation) {
      this.removeInvalidReservation(reservation.key);
      this.$emit("deleted", reservation);
    },
    addNewReservation() {
      this.newReservation = new models.ComputeResourceReservation();
      this.newReservationValid = false;
      this.newReservation.queueNames = this.queues.slice();
      this.showNewItemEditor = true;
    },
    saveNewReservation() {
      this.$emit("added", this.newReservation);
      this.showNewItemEditor = false;
    },
    cancelNewReservation() {
      this.showNewItemEditor = false;
    },
    recordInvalidReservation(reservationKey) {
      if (this.invalidReservations.indexOf(reservationKey) < 0) {
        this.invalidReservations.push(reservationKey);
      }
      this.validate();
    },
    removeInvalidReservation(reservationKey) {
      const index = this.invalidReservations.indexOf(reservationKey);
      if (index >= 0) {
        this.invalidReservations.splice(index, 1);
      }
      this.validate();
    },
    isReservationInvalid(reservationKey) {
      return this.invalidReservations.indexOf(reservationKey) >= 0;
    },
    validate() {
      if (this.valid) {
        this.$emit("valid");
      } else {
        this.$emit("invalid");
      }
    },
    deleteAllExpiredReservations() {
      this.expiredReservations.forEach(this.deleteReservation);
    },
  },
};
</script>
