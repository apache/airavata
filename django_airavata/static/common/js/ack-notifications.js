import $ from "jquery";

$( "#ack-notification" ).on('click', function(event) {

  if ( event.target.attributes.getNamedItem("notification_id") == null){
    return;
  }

  var id = event.target.attributes.getNamedItem("notification_id").value;
  var url = event.target.attributes.getNamedItem("url").value;

  $.ajax({
    url: url
    }).then(function() {

      $("#"+id).prop('hidden','true');
      var num = parseInt($("#unread_notification_count").attr("data-count")) - 1;
      //using .attr since the .data method only updates the values in the cache
      //and they are not reflected on the screen.
      $("#unread_notification_count").attr("data-count", num);

    });
  event.stopPropagation();
});

export default {

};
