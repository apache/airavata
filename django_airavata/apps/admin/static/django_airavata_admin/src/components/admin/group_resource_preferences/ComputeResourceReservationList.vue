<template>
  <list-layout
    @add-new-item="addNewReservation"
    :items="decoratedReservations"
    title="Reservations"
    new-item-button-text="New Reservation"
    :new-button-disabled="readonly"
  >
    <template slot="new-item-editor">
      <b-card v-if="showNewItemEditor" title="New Reservation">
        <compute-resource-reservation-editor
          v-model="newReservation"
          :queues="queues"
          @valid="newReservationValid = true"
          @invalid="newReservationValid = false"
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
      <b-table
        striped
        hover
        :fields="fields"
        :items="slotProps.items"
        sort-by="startTime"
      >
        <template slot="queueNames" slot-scope="data">
          <ul v-for="queueName in data.item.queueNames" :key="queueName">
            <li>{{ queueName }}</li>
          </ul>
        </template>
        <template slot="action" slot-scope="data">
          <b-link
            v-if="!readonly"
            class="action-link"
            @click="toggleDetails(data)"
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
            {{ data.item.reservationName }}?
          </delete-link>
        </template>
        <template slot="row-details" slot-scope="row">
          <b-card>
            <compute-resource-reservation-editor
              :value="row.item"
              @input="updatedReservation"
              :queues="queues"
            />
            <b-button size="sm" @click="toggleDetails(row)">Close</b-button>
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
    ComputeResourceReservationEditor
  },
  props: {
    reservations: {
      type: Array,
      required: true
    },
    queues: {
      type: Array,
      required: true
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showingDetails: {},
      showNewItemEditor: false,
      newReservation: null,
      newReservationValid: false
    };
  },
  computed: {
    fields() {
      return [
        {
          label: "Name",
          key: "reservationName"
        },
        {
          label: "Queues",
          key: "queueNames"
        },
        {
          label: "Start Time",
          key: "startTime",
          sortable: true,
          formatter: value =>
            utils.dateFormatters.dateTimeInMinutesWithTimeZone.format(value)
        },
        {
          label: "End Time",
          key: "endTime",
          sortable: true,
          formatter: value =>
            utils.dateFormatters.dateTimeInMinutesWithTimeZone.format(value)
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    },
    decoratedReservations() {
      return this.reservations
        ? this.reservations.map(res => {
            const resClone = res.clone();
            resClone._showDetails = this.showingDetails[resClone.key];
            return resClone;
          })
        : [];
    },
    isSaveDisabled() {
      return !this.newReservationValid;
    }
  },
  created() {},
  methods: {
    updatedReservation(newValue) {
      const reservationIndex = this.reservations.findIndex(r => r.key === newValue.key);
      this.$emit("updated", newValue, reservationIndex);
    },
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.key] = !this.showingDetails[
        row.item.key
      ];
    },
    deleteReservation(reservation) {
      const reservationIndex = this.reservations.findIndex(r => r.key === reservation.key);
      this.$emit("deleted", reservationIndex);
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
    }
  }
};
</script>
