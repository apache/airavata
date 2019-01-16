import bootstrap from "bootstrap"; // eslint-disable-line no-unused-vars
import $ from "jquery";

import "bootstrap/dist/css/bootstrap.css";
import "@fortawesome/fontawesome-free/css/all.css";
import "../scss/main.scss";

$(function() {
  $('[data-toggle="tooltip"]').tooltip();
});

// CMS integration
// $('.carousel').carousel({
//   interval: 2000
// })
