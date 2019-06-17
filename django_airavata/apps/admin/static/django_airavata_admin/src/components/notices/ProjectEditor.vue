<template>
  <div>
    <div class="d-flex">
      <slot name="title">
        <h1 class="h4 mb-4 mr-auto">
          New Notice



        </h1>

      </slot>
      <slot name="buttons">
      </slot>
    </div>
    <b-form
      @submit="onSubmit"
      @input="onUserInput"
      novalidate
    >

        <b-form-group
          label="Notice title"
          label-for="notice-title"
          :feedback="nameFeedback"
          :state="nameState"
        >
          <b-form-input
            id="notice-title"
            type="text"
            v-model="data.title"
            required
            placeholder="notice title"
            :state="nameState"
          ></b-form-input>
        </b-form-group>

        <b-form-group
          label="Notice Message"
          label-for="notice-message"
        >
          <b-form-textarea
            id="notice-message"
            type="text"
            v-model="data.notificationMessage"
            required
            placeholder="Notice Message"
            :rows="3"
          ></b-form-textarea>
        </b-form-group>

        <b-form-group
          label="Publish Date"
          label-for="publish-date"
        >
        <datetime
          type="datetime"
          v-model="inputPublishedTime"
          input-class="my-class"
          value-zone="America/New_York"
          zone="America/New_York"
          :format="{ year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: '2-digit', timeZoneName: 'short' }"
          :phrases="{ok: 'Continue', cancel: 'Exit'}"
          :hour-step="1"
          :minute-step="15"
          :min-datetime="today"
          :week-start="7"
          use12-hour
          auto
          ></datetime>
        </b-form-group>

        <b-form-group
          label="Expiration Date"
          label-for="expiration-date"
        >
        <datetime
          type="datetime"
          v-model="inputExpirationTime"
          input-class="my-class"
          value-zone="America/New_York"
          zone="America/New_York"
          :format="{ year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: '2-digit', timeZoneName: 'short' }"
          :phrases="{ok: 'Continue', cancel: 'Exit'}"
          :hour-step="1"
          :minute-step="15"
          :min-datetime="inputPublishedTime"
          :week-start="7"
          use12-hour
          auto
          ></datetime>

        </b-form-group>

        <b-form-group
          label="Priority"
          label-for="priority"
        >
          <b-form-select
          id = "priority"
          v-model="data.priority"
          >
          <option v-for="(selectOption, indexOpt) in select.options"
            :key="indexOpt"
            :value="selectOption"
          >
            {{ selectOption }} - {{ selectOption === select.selected }}
          </option>
        </b-form-select>

      </b-form-group>
    </b-form>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";
import { Datetime } from 'vue-datetime';
import moment from "moment";
import 'vue-datetime/dist/vue-datetime.css'

export default {
  name: "project-editor",
  components: {
    datetime: Datetime
  },
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.Notification,
      required: true
    }
  },
  created() {
    console.log("Editor has been created...");
    console.log(this.hasOwnProperty('value'));
    if(this.value.notificationId != null){
      console.log("Updating the date");
      console.log(new moment(this.value.publishedTime.toGMTString()).format());
      this.inputPublishedTime = new moment(this.value.publishedTime.toGMTString()).format()
      this.inputExpirationTime = new moment(this.value.expirationTime.toGMTString()).format()
      console.log(this.data.priority);
      this.select.selected = this.data.priority.name;
    }
  },
  mounted() {
    this.$on("input", this.validate);
    this.validate();
  },
  data() {
    return {
      userBeginsInput: false,
      inputPublishedTime: null,
      inputExpirationTime: null,
      today: new moment().format(),
      select : {
          selected: "LOW",
          options: [
              { value: "LOW", text: 'LOW' },
              { value: "NORMAL", text: 'NORMAL' },
              { value: "HIGH", text: 'HIGH' }
            ]
      }
    };
  },
  computed: {
    nameFeedback() {
      if (this.userBeginsInput && this.validation.name) {
        return this.validation.name.join("; ");
      } else {
        return null;
      }
    },
    nameState() {
      if (this.validation.name) {
        if (this.userBeginsInput) {
          return false;
        } else {
          return null;
        }
      } else {
        return true;
      }
    },
    validation() {
      const v = this.data.validate();
      return v ? v : {};
    }
  },
  methods: {
    validate() {
      if (Object.keys(this.validation).length > 0) {
        this.$emit("invalid");
      } else {
        this.$emit("valid");
      }
    },
    onUserInput() {
      this.userBeginsInput = true;
    },
    onSubmit(event) {
      event.preventDefault();
      this.$emit("save");
    },
    reset() {
      this.userBeginsInput = false;
    }
  },
  watch: {
    inputExpirationTime() {
      this.data.expirationTime = this.inputExpirationTime;
      console.log("Expiration time: " + this.inputExpirationTime);

    },
    inputPublishedTime() {
      this.data.publishedTime = this.inputPublishedTime;
      console.log("Today date: " + this.today);
      console.log("Published time: " + this.inputPublishedTime);
    },
    value() {
      this.validate();
    }
  }
};
</script>
