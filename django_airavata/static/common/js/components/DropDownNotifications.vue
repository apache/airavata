<script>
  import { models, services } from "django-airavata-api";
  import HumanDate from "./HumanDate.vue";

  export default {
    name : "notice-ack",
    data () {
      return {
        notices: null,
        cars: ["Saab", "Volvo", "BMW"],
        p: "hell",
        items: [
                { message: 'Foo' },
                { message: 'Bar' }
               ]
      };
    },
    components: {
      'human-date': HumanDate
    },
    created() {
      services.ManageNotificationService.list().then(
        notices => (this.notices = notices)
      );
    },
    methods: {
      greet: function (event) {
        // `this` inside methods point to the Vue instance
        alert('Hello')

      }
    }
  };

</script>
