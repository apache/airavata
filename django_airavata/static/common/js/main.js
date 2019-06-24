import bootstrap from "bootstrap"; // eslint-disable-line no-unused-vars
// import { entry } from "django-airavata-common-ui";
import entry from "./entry";
import $ from "jquery";

import "bootstrap/dist/css/bootstrap.css";
import "@fortawesome/fontawesome-free/css/all.css";
import "../scss/main.scss";
import DropDownNotifications from "./components/DropDownNotifications.vue";

$(function() {
  $('[data-toggle="tooltip"]').tooltip();
});

entry(Vue => {
  new Vue({
    render(h) {
      return h(DropDownNotifications);
    },
    beforeMount() {
      console.log("Mounted notifications dropdown 3");
    }
  }).$mount("#test");
});

$( "#ack-notification" ).on('click', function(event) {
  console.log("Clicked the div");
  console.log(event);
  if ( event.target.attributes.getNamedItem("notification_id") == null){
    return;
  }
  console.log("Ack notification");
  var id = event.target.attributes.getNamedItem("notification_id").value;
  var url = event.target.attributes.getNamedItem("url").value;
  alert( url );

  //http://127.0.0.1:8000/api/ack-notifications/?id=c06578d6-ac62-4e43-898b-1dabf1b1b00b
  $.ajax({
    url: url
    }).then(function() {
      alert("ajax executed");
      $("#"+id).prop('hidden','true');
    });
  event.stopPropagation();

});
// CMS integration
// $('.carousel').carousel({
//   interval: 2000
// })
