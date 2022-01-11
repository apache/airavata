<template>
  <b-form>
    <b-form-group
      label="Reservation name"
      label-for="reservation-name"
      :invalid-feedback="nameValidationFeedback"
      :state="nameValidationState"
    >
      <b-form-input
        id="reservation-name"
        v-model="data.reservationName"
        type="text"
        @input="nameInputBegins = true"
        :state="nameValidationState"
      />
    </b-form-group>
    <b-form-group
      label="Start Time"
      label-for="start-time"
      :invalid-feedback="getValidationFeedback('startTime')"
      :state="getValidationState('startTime')"
    >
      <datetime
        id="start-time"
        type="datetime"
        :value="startTimeAsString"
        input-class="form-control"
        :format="{
          year: 'numeric',
          month: '2-digit',
          day: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
          timeZoneName: 'short',
        }"
        :phrases="{ ok: 'Continue', cancel: 'Exit' }"
        :hour-step="1"
        :minute-step="30"
        :week-start="7"
        use12-hour
        auto
        @input="data.startTime = stringToDate($event)"
      ></datetime>
    </b-form-group>
    <b-form-group
      label="End Time"
      label-for="end-time"
      :invalid-feedback="getValidationFeedback('endTime')"
      :state="getValidationState('endTime')"
    >
      <datetime
        id="end-time"
        type="datetime"
        :value="endTimeAsString"
        :input-class="{
          'form-control': true,
          'is-invalid': getValidationState('endTime'),
        }"
        :format="{
          year: 'numeric',
          month: '2-digit',
          day: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
          timeZoneName: 'short',
        }"
        :phrases="{ ok: 'Continue', cancel: 'Exit' }"
        :hour-step="1"
        :minute-step="30"
        :week-start="7"
        :min-datetime="startTimeAsString"
        use12-hour
        auto
        @input="data.endTime = stringToDate($event)"
      ></datetime>
    </b-form-group>
    <b-form-group
      label="Queues"
      label-for="queues"
      :invalid-feedback="getValidationFeedback('queueNames')"
      :state="getValidationState('queueNames')"
    >
      <b-form-checkbox-group
        id="queues"
        v-model="data.queueNames"
        :options="queueNameOptions"
        :state="getValidationState('queueNames')"
      />
    </b-form-group>
  </b-form>
</template>

<script>
import { mixins, utils } from "django-airavata-common-ui";
import { Datetime } from "vue-datetime";
import "vue-datetime/dist/vue-datetime.css";

export default {
  name: "compute-resource-reservation-editor",
  mixins: [mixins.VModelMixin],
  components: {
    datetime: Datetime,
  },
  props: {
    queues: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      nameInputBegins: false,
    };
  },
  created() {
    this.$on("input", this.valuesChanged);
  },
  computed: {
    startTimeAsString() {
      return this.data.startTime.toISOString();
    },
    endTimeAsString() {
      return this.data.endTime.toISOString();
    },
    nameValidationFeedback() {
      return this.getValidationFeedback("reservationName");
    },
    nameValidationState() {
      if (this.nameInputBegins === false) {
        return null;
      }
      return this.getValidationState("reservationName");
    },
    queueNameOptions() {
      return this.queues.slice().sort();
    },
  },
  methods: {
    stringToDate(datetimeString) {
      return new Date(datetimeString);
    },
    getValidationFeedback: function (properties) {
      return utils.getProperty(this.data.validate(), properties);
    },
    getValidationState: function (properties) {
      return this.getValidationFeedback(properties) ? false : null;
    },
    valuesChanged() {
      const validationResults = this.data.validate();
      if (Object.keys(validationResults).length === 0) {
        this.$emit("valid");
      } else {
        this.$emit("invalid");
      }
    },
  },
};
</script>
