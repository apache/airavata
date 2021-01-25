function getAccodrionCode()
{
	return '<div class="panel-group" id="accordion"> \
  <div class="panel panel-default"> \
    <div class="panel-heading">\
      <h4 class="panel-title">\
        <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">\
          Collapsible Group Item #1\
        </a>\
      </h4>\
    </div>\
    <div id="collapseOne" class="panel-collapse collapse in">\
      <div class="panel-body">\
        Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably havent heard of them accusamus labore sustainable VHS.\
      </div>\
    </div>\
  </div>';
}

function disableInputs( elem){
  elem.find("input").each( function( i,e){
      if( $(e).attr("type")=='submit' || $(e).attr("type")=='button' || $(e).attr("type")=='checkbox' )
          $(e).attr("disabled", "true");
       else
          $(e).prop("readonly", "true");
  });
  elem.find("textarea").prop("readonly", "true");
  elem.find("select").attr("disabled", "true");
  elem.find(".hide").prop("readonly", "true");
  elem.find("button").attr("disabled", "true");
  //remove previously created input/output blocks
  elem.find(".app-inputs").html("");
  elem.find(".app-outputs").html("");
  $(".glyphicon").hide();
}

$(document).ready( function(){

  //making the previously opened tab open again on page reload/modifications.
  var urlArray = window.location.href.split("#");
  var openTab = urlArray[1];
  $('a[href="#' + openTab + '"]').click();
  //tab open code ends here.

  /* list additions for compute resources create ui */
	$(".add-queue").click( function(){
		$(this).before( $(".queue-block").html() );
	});

  /* list additions for compute resources create ui */
  $(".add-alias").click( function(){
    $(this).before( '<input class="form-control" maxlength="30" name="hostaliases[]"/>');
  });

  /* list additions for compute resources create ui */
  $(".add-ip").click( function(){
    $(this).before( '<input class="form-control" maxlength="30" name="ips[]"/>');
  })

  /* list additions for storage resources create ui */
  $(".add-datamovementinterface").click( function(){
    $(this).before( '<input class="form-control" maxlength="30" name="dataMovementInterfaces[]"/>');
  });

  /* 
   * code that relates to Job Submission Protocol Interface starts here.
  */

  $(".add-job-submission").click( function(){
    /*$(".job-submission-info").removeClass("hide").append( "<div class='job-protocol-block col-md-12'>" + $(".select-job-protocol").html() + "</div><hr/>");
  	
  	$('html, body').animate({
        scrollTop: $(this).position().top + 500
    }, 200);
    */
    $(".add-jsi-body").html( "<div class='job-protocol-block col-md-12'>" + $(".select-job-protocol").html() + "</div><hr/>");
    $("#add-jsi").modal("show");
  });

  $("body").on("change", ".selected-job-protocol", function(){

    var selectedVal = $(this).children("option:selected").html().toLowerCase();

    $(this).parent().find("div[class*='resourcemanager-']").remove();

    var parentResDiv = "<div class='resourcemanager-" + selectedVal + "'>"
                + "<hr/>" 
                + "Resource Manager: <h4>" + selectedVal + "</h4>"
                + "<hr/>";

    if( selectedVal == "local")
    {
      $(this).after(  parentResDiv + $(".resource-manager-block").html() + "</div>" );
    }
    else if( selectedVal == "ssh_fork" || selectedVal == "ssh")
    {
      $(this).after(  parentResDiv 
                      + $(".ssh-block").html()
                      + $(".resource-manager-block").html() 
                      + "</div>" );
      $(this).parent().find(".addedScpValue").removeClass("hide");
    }
    else if( selectedVal == "globus")
    {
    	alert("Globus Protocol is not being setup right now. Please choose another option.");
    	/*	
      $(this).parent().append(  parentResDiv 
                      + $(".ssh-block").html()
                      + "<h5>Globus Gate Keeper End Point</h5>" 
                      + "<input class='form-control' name='globusGateKeeperEndPoint'/>"
                      + "</div>" );
         */
    }
    else if( selectedVal == "unicore")
    {
      $(this).parent().append(  parentResDiv 
                      + $(".ssh-block").html()
                      + "<h5>Unicore End Point Url</h5>" 
                      + "<input class='form-control' name='unicoreEndPointURL'/>"
                      + "</div>" );
    }
    else if( selectedVal == "cloud")
    {
      //alert("Cloud Protool is not being setup right now. Please choose another option.");
      
      $(this).parent().append(  parentResDiv 
                      + $(".ssh-block").html()
                      + $(".cloud-block").html()
                      + "<div>"
                      );
      
    }
    else{
      alert("Something went wrong. Please try again");
      $(".jspSubmit").addClass("hide");
    }

    //temporary till all protocols are not setup
    if( selectedVal == "local" || selectedVal == "ssh_fork" || selectedVal == "ssh" || selectedVal == "unicore" || selectedVal == "cloud")
      $(".jspSubmit").removeClass("hide");
    else
      $(".jspSubmit").addClass("hide");

  });
  
  /* 
   * code that relates to Job Submission Protocol Interface ends here.
  */


  /* 
   * code that relates to Data Movement Interface starts here.
  */

  $(".add-data-movement").click( function(){
    //$(".data-movement-info").removeClass("hide").append( "<div class='data-movement-block col-md-12'>" + $(".select-data-movement").html() + "</div><hr/>");
    $(".add-dmi-body").html("<div class='data-movement-block col-md-12'>" + $(".select-data-movement").html() + "</div><hr/>");
    $("#add-dmi").modal("show");
  });


  $("body").on("change", ".selected-data-movement-protocol", function(){

    var selectedVal = $(this).children("option:selected").html().toLowerCase();

    $(this).parent().find("div[class*='dataprotocol-']").remove();
    var parentDataDiv = "<div class='dataprotocol-" + selectedVal + "'>"
                + "<hr/>" 
                + "Data Management Protocol: <h4>" + selectedVal + "</h4>"
                + "<hr/>";
    if( selectedVal == "local")
    {
      // to find out what goes here.
    }
    else if( selectedVal == "scp" )
    {
      $(this).after(  parentDataDiv 
                      + $(".ssh-block").html()
                      + "</div>" );
      $(this).parent().find(".addedScpValue").removeClass("hide");
    }
    else if( selectedVal == "sftp")
    {
      alert( "SFTP has not been setup yet. Please choose another Data Movement Protocol");
    }
    else if( selectedVal == "gridftp")
    {
      $(this).after(  parentDataDiv 
                      + $(".ssh-block").html()
                      + $(".dm-gridftp").html()
                      + "</div>" );
    }
    else if( selectedVal == "unicore_storage_service")
    {
      $(this).after(  parentDataDiv 
                      + $(".ssh-block").html()
                      + "<h5>Unicore End Point Url</h5>" 
                      + "<input class='form-control' name='unicoreEndPointURL'/>"
                      + "</div>" );
    }
    else{
      alert("Something went wrong. Please try again");
      $(".dmpSubmit").addClass("hide");
    }
    $(".dmpSubmit").removeClass("hide");

  });

  $("body").on("click", ".add-gridFTPEndPoint", function(){
        $(this).before( '<input class="form-control" maxlength="30" name="gridFTPEndPoints[]"/>');
  });

   $(".delete-jsi").click( function(){
      $(".delete-jsi-confirm").val( $(this).data("jsi-id"));
   });

  $(".delete-jsi-confirm").click( function(){

    var jsiId = $(this).data("jsi-id");
    $.ajax({
      type: "POST",
      url: $(".base-url").val() + "/cr/delete-jsi",
      data: { 
              crId : $(".crId").val(), 
              jsiId : jsiId 
            }
    })
    .complete(function( data ) {
      $("#confirm-delete-jsi").modal("hide");
      if( data.responseText == 1)
      {
        $(".job-protocol-block").each( function(i, elem){
          var toBeRemovedChild = $(elem).find(".delete-jsi");
          if( toBeRemovedChild.data("jsi-id") == jsiId )
          {
            $(elem).before("<div class='alert alert-success'>The Job Submission Interface has been successfully deleted.</div>");
            $(elem).fadeOut().remove();
          }
        });
      }
    });

  });

  $(".delete-dmi").click( function(){
      $(".delete-dmi-confirm").val($(this).data("dmi-id"));
   });

  $(".delete-dmi-confirm").click( function(){

    var dmiId = $(this).data("dmi-id");
    $.ajax({
      type: "POST",
      url: $(".base-url").val() + "/cr/delete-dmi",
      data: {
              crId : $(".crId").val(), 
              dmiId : dmiId 
            }
    })
    .complete(function( data ) {
      $("#confirm-delete-dmi").modal("hide");
      if( data.responseText == 1)
      {
        $(".data-movement-block").each( function(i, elem){
          var toBeRemovedChild = $(elem).find(".delete-dmi");
          if( toBeRemovedChild.data("dmi-id") == dmiId )
          {
            $(elem).before("<div class='alert alert-success'>The Data Movement Interface has been successfully deleted.</div>");
            $(elem).fadeOut().remove();
          }
        });
      }

    });
  });

  $("#jsi-priority-form").submit( function( event ){
    event.preventDefault();
    $.ajax({
        url: $(this).attr("action"),
        type: 'post',
        data: $('#jsi-priority-form').serialize(),
        success: function(data) {
                  if( data == 1)
                  {
                    $(".priority-updated").removeClass("hide");
                    $(".priority-updated").fadeIn();
                    setTimeout( function(){
                      $(".priority-updated").addClass("hide");
                      $("#update-jsi-priority").modal("hide");

                    }, 3000);
                    
                  }
        }
    });
  });

  $("#dmi-priority-form").submit( function( event ){
    event.preventDefault();
    $.ajax({
        url: $(this).attr("action"),
        type: 'post',
        data: $('#dmi-priority-form').serialize(),
        success: function(data) {
                  if( data == 1)
                  {
                    $(".priority-updated").removeClass("hide");
                    $(".priority-updated").fadeIn();
                    setTimeout( function(){
                      $(".priority-updated").addClass("hide");
                      $("#update-dmi-priority").modal("hide");

                    }, 3000);
                    
                  }
        }
    });
  });

  $(".add-queue-block").on("click", ".create-queue-form", function(){
    var newQueueName = $(this).parent().parent().find(".create-queue-name").val();

    if (newQueueName.length > 0) {
        children = ($("#accordion").children());

        queueNameExists = false;
        children.each( function(index, elem){
          var existingQueueName = $(elem).find(".existing-queue-name").html();
          if( existingQueueName == newQueueName)
          {
            queueNameExists = true;
            return false;
          }
        });

        if( queueNameExists)
          alert( "This queue name already exists. Please choose another name.");
        else
          $(this).parent().parent().parent().submit();
        
    } else {
        alert("Please enter queue name before submitting");
    }

  });

  $(".enable-gateway-check").change( function(){
    var reportingCheckbox = this;
    if( reportingCheckbox.checked ){
      $(reportingCheckbox).val(1);
      $(".gateway-commands").find("input").each( function( i,e){
            $(e).removeAttr("disabled");
            $(e).removeAttr("readonly");
      });
    }
    else{
      $(reportingCheckbox).val(0);
      disableInputs( $(".gateway-commands"));
    }

  })

  
});