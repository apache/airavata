@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
{{ HTML::style('css/datetimepicker.css')}}
@stop

@section('content')



<!-- contains all compute resource choices that might get selected on adding a new one to a gateway -->
@foreach( (array)$computeResources as $index => $cr)
@include('partials/compute-resource-preferences', array('computeResource' => $cr, 'crData' => $crData))
@endforeach

<!-- contains all storage resource choices that might get selected on adding a new one to a gateway -->
@foreach( (array)$storageResources as $index => $sr)
    @include('partials/storage-resource-preferences', array('storageResource' => $sr, 'srData' => $srData))
@endforeach

<div id="wrapper">
    <!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
    @include( 'partials/dashboard-block')
    <div id="page-wrapper">
        <div class="col-md-12">
            @if( Session::has("message"))
            <div class="row">
                <div class="alert alert-success alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert"><span
                            aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    {{ Session::get("message") }}
                </div>
            </div>
            {{ Session::forget("message") }}
            @endif

        @if (Session::has("errorMessages"))
            <div class="row">
                <div class="alert alert-danger alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                                class="sr-only">Close</span></button>
                    {{ Session::get("errorMessages") }}
                </div>
            </div>
            {{ Session::forget("errorMessages") }}
        @endif

        @if (Session::has("successMessages"))
            <div class="row">
                <div class="alert alert-success alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                                class="sr-only">Close</span></button>
                    {{ Session::get("successMessages") }}
                </div>
            </div>
            {{ Session::forget("successMessages") }}
        @endif
        </div>

        @if ($errors->has())
            @foreach ($errors->all() as $error)
                {{ CommonUtilities::print_error_message($error) }}
            @endforeach
        @endif

        <div class="col-md-12">
            <ul class="nav nav-tabs nav-justified" id="tabs" role="tablist">
                <li class="active"><a href="#tab-currentGateway" data-toggle="tab">Gateway - {{ Session::get("gateway_id") }}</a></li>
                @if( Session::has('super-admin'))
                    <li><a href="#tab-allGateways" data-toggle="tab">Created Gateways</a></li>
                    <li><a href="#tab-requestedGateways" data-toggle="tab">Gateway Requests</a></li>
                @endif
            </ul>
        </div>
        <div class="container-fluid">
            <div class="tab-content col-md-12">
                <div class="tab-pane active" id="tab-currentGateway">
                    <div class="panel-group" id="accordion2">
                        <h3>Edit your Gateway Profile</h3>
                        @foreach( $gateways as $indexGP => $gp )
                            @if( $gp->gatewayId == Session::get("gateway_id"))
                                @include('partials/gateway-preferences-block', array("gp" => $gp, "accName" => "accordion2") )
                            @endif
                        @endforeach
                    </div>
                </div>

                @if( Session::has('super-admin'))

                <div class="tab-pane" id="tab-requestedGateways">

                    <div class="row">
                        <a href="{{ URL::to('/') }}/admin/add-gateway">
                            <button type="button" class="btn btn-default toggle-add-tenant"><span
                                        class="glyphicon glyphicon-plus"></span>Add a new gateway
                            </button>
                        </a>
                    </div>
                    <div class="row">
                        <div class="col-md-12 table-responsive">
                            <h3>Gateway Requests</h3>
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                        <th>Gateway Name</th>
                                        <th>Creation Time</th>
                                        <th>Admin Name</th>
                                        <th>Gateway URL</th>
                                        <th>Project Details</th>
                                        <th>Project Abstract</th>
                                        <th>
                                            Status
                                            <select class="gaStatuses">
                                                <option value="ALL">ALL</option>
                                                @foreach( $gatewayApprovalStatuses as $status)
                                                <option value="{{$status}}">{{$status}}</option>
                                                @endforeach
                                            </select>
                                        </th>
                                        <th>
                                        <!-- for View Button -->
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                @foreach( $gateways as $indexGP => $gp )
                                    <tr class="gatewayRow gatewayStatus-{{$gatewayApprovalStatuses[$gp->gatewayApprovalStatus]}}">
                                        <td class="form-gatewayName">{{$gp->gatewayName }}</td>
                                        <?php 

                                            $timeDifference = Session::get("user_timezone");
                                            $addOrSubtract = "-";
                                            if( $timeDifference < 0)
                                                $addOrSubtract = "+";

                                            $creationTime = date('m/d/Y h:i:s A', intval( strtotime( $addOrSubtract . " " . Session::get("user_timezone") . " hours", $gp->requestCreationTime/1000) ) );
                                        ?>
                                        <td>{{ $creationTime}}</td>
                                        <td>{{ $gp->gatewayAdminFirstName }} {{ $gp->gatewayAdminLastName }} </td>
                                        <td class="form-gatewayURL">{{ $gp->gatewayURL }}</td>
                                        <td style="max-width: 400px; word-wrap: break-word;">{{ $gp->reviewProposalDescription}}</td>
                                        <td style="max-width: 400px; word-wrap: break-word;">{{ $gp->gatewayPublicAbstract}}</td>
                                        <td>{{$gatewayApprovalStatuses[$gp->gatewayApprovalStatus] }}</td>
                                        <td>
                                            <input type="button" class="btn btn-primary btn-xs start-approval" id="view-{{ preg_replace('/[\s]/', '-',$gp->gatewayId) }}" data-gatewayobject="{{htmlentities(json_encode( $gp))}}" value="View"/>
                                        </td>
                                    </tr>
                                @endforeach
                                <!-- foreach code ends -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="tab-pane" id="tab-allGateways">


                    <div class="row">
                        <div class="col-md-6">
                            <h3>Check all Gateway Profiles</h3>
                        </div>
                        <div class="col-md-6" style="margin-top:2%">
                            <input type="text" class="col-md-12 filterinput" placeholder="Search by Gateway Name"/>
                        </div>
                    </div>

                    <div class="panel-group super-admin-gateways-view" id="accordion1">
                        @foreach( $gateways as $indexGP => $gp )
                            @if( $gatewayApprovalStatuses[$gp->gatewayApprovalStatus] == "CREATED" ||
                                   $gatewayApprovalStatuses[$gp->gatewayApprovalStatus] == "DEPLOYED" )
                                
                                @include('partials/gateway-preferences-block', array("gp" => $gp, "accName" => "accordion1"))
                                
                            @endif
                        @endforeach
                    </div>
                </div>

                @endif
            </div>
            <!-- ends tabs -->

        </div>
        <!-- /.container-fluid -->

    </div>
    <!-- /#page-wrapper -->

</div>


<div class="add-compute-resource-block hide">
    <div class="well">
        <form action="{{URL::to('/')}}/gp/add-crp" method="POST">
            <input type="hidden" name="gatewayId" id="gatewayId" value="">

            <div class="input-group">
                <select name="computeResourceId" class="cr-select form-control">
                    <option value="">Select a compute Resource and set its preferences</option>
                    @foreach( (array)$unselectedCRs as $index => $cr)
                    <option value="{{ $cr->computeResourceId}}">{{ $cr->hostName }}</option>
                    @endforeach
                </select>
                <span class="input-group-addon remove-cr" style="cursor:pointer;">x</span>
            </div>
            <div class="cr-pref-space form-horizontal"></div>
        </form>
    </div>
</div>

<div class="add-data-storage-preference-block hide">
    <div class="well">
        <form action="{{URL::to('/')}}/gp/add-srp" method="POST">
            <input type="hidden" name="gatewayId" id="gatewayId" value="">

            <div class="input-group">
                <select name="storageResourceId" class="sr-select form-control">
                    <option value="">Select a Data Storage Resource and set its preferences</option>
                    @foreach( (array)$unselectedSRs as $index => $sr)
                        <option value="{{ $sr->storageResourceId}}">{{ $sr->hostName }}</option>
                    @endforeach
                </select>
                <span class="input-group-addon remove-cr" style="cursor:pointer;">x</span>
            </div>
            <div class="sr-pref-space form-horizontal"></div>
        </form>
    </div>
</div>

@if( Session::has("super-admin"))
<!-- Approve a Gateway request -->
<input type="hidden" class="gatewayApprovalStatuses" value="{{ htmlentities( json_encode( $gatewayApprovalStatuses) ) }}"/>
<div class="modal fade" id="approve-gateway" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true" data-backdrop="static" >
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="{{URL::to('/')}}/admin/update-gateway-request" id="update-gateway-request" method="GET">
            
                <div class="modal-header">
                    <button type="button" class="close update-gateway-request-close-modal" data-dismiss="modal" aria-label="Close" ><span
                                aria-hidden="true">&times;</span></button>

                    <h3>View the Gateway Details</h3>

                </div>
                <!--
                <div class="modal-body onTenantLoad">
                    Adding tenant for GatewayId: <span class="gatewayid-for-approval"></span>. Please do not refresh or close this page!
                </div>
                -->
                <div class="modal-body">
                    <!--
                    <h3>Gateway Tenant has been added. Please fill in rest of the required details.</h3>
                    -->
                    <div class="form-group">
                        <h4>Gateway ID: <span class="gatewayid-for-approval"></span></h4>
                    </div>
                    <div class="form-group">
                        <label>Gateway Name</label>
                        <input type="text" readonly="readonly" name="gatewayName" class="form-control gatewayName"/>
                    </div>
                    <div class="form-group">
                        <label>Contact Email Address</label>
                        <input type="text" name="emailAddress" id="emailAddress" class="form-control emailAddress"/>
                    </div>
                    <div class="form-group">
                        <label>Gateway URL</label>
                        <input type="text" name="gatewayURL" id="gatewayURL" class="form-control gatewayURL"/>
                    </div>
                    <div class="form-group">
                        <label>Gateway Admin Username</label>
                        <input type="text" name="identityServerUserName" id="identityServerUserName" class="form-control identityServerUserName"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Gateway Admin Password</label>
                        <input type="password" id="password" name="gatewayAdminPassword" class="form-control identityServerPasswordToken" title="" type="password" data-container="#approve-gateway" data-toggle="popover" data-placement="left" data-content="Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$*"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Admin Password Confirmation</label>
                        <input type="password" name="gatewayAdminPasswordConfirm" class="form-control"/>
                    </div>
                    <div class="form-group">
                        <label>Gateway Admin First Name</label>
                        <input type="text" name="gatewayAdminFirstName" id="gatewayAdminFirstName" class="form-control gatewayAdminFirstName"/>
                    </div>
                    <div class="form-group">
                        <label>Gateway Admin Last Name</label>
                        <input type="text" name="gatewayAdminLastName" id="gatewayAdminLastName" class="form-control gatewayAdminLastName"/>
                    </div>
                    <div class="form-group">
                        <label>Admin Email ID</label>
                        <input type="text" name="gatewayAdminEmail" id="gatewayAdminEmail" class="form-control emailAddress"/>
                    </div>
                    <div class="form-group">
                        <label>Gateway Public Abstract</label>
                        <textarea name="gatewayPublicAbstract" id="gatewayPublicAbstract" class="form-control gatewayPublicAbstract"></textarea>
                    </div>
                    <div class="form-group">
                        <label>Gateway Proposal Description</label>
                        <textarea name="reviewProposalDescription" id="reviewProposalDescription" class="form-control reviewProposalDescription"></textarea>
                    </div>

                    <div class="form-group">
                        <label>Oauth Client Id</label>
                        <input type="text" readonly="readonly" name="oauthClientId" class="form-control oauthClientId"/>
                    </div>

                    <div class="form-group">
                        <label>Oauth Client Secret</label>
                        <input type="text" readonly="readonly" name="oauthClientSecret" class="form-control oauthClientSecret"/>
                    </div>
                    <div class="form-group">
                        <label>SciGaP Admin Comments</label>
                        <textarea style="width:100%; height:80px" width="100%" name="declinedReason" class="declinedReason"></textarea>
                    </div>

                    <div class="form-group">
                        <label>Status</label>
                        <input type="text" readonly="readonly" name="gatewayApprovalStatus" class="form-control gatewayApprovalStatus"/>
                        <!--
                        <select name="gatewayApprovalStatus" class="form-control gatewayApprovalStatus">
                            @foreach( $gatewayApprovalStatuses as $val => $status) 
                            <option value="{{$val}}">{{$status}}</option>
                            @endforeach
                        </select>
                        -->
                    </div>
                    <input type="hidden" class="gatewayid-for-approval" name="gateway_id">
                    <input type="hidden" name="internal_gateway_id">
                </div>
                <div class="modal-footer submit-actions">
                    <button type="submit" name="status" class="btn btn-primary update-gateway notApprovedGateway" value="approveRequest" >Approve Request</button>
                    <button type="submit" name="status" class="btn btn-danger update-gateway notApprovedGateway" value="denyRequest" >Deny Request</button>
                    <button type="submit" name="status" class="btn btn-primary update-gateway approvedGateway" value="updateGateway" >Update Gateway</button>
                    <button type="submit" name="status" class="btn btn-primary update-gateway approvedGateway" value="createTenant" >Create Tenant</button>
                    <button type="submit" name="status" class="btn btn-primary update-gateway createdGateway" value="deployGateway" >Deploy Gateway</button>
                    <button type="submit" name="status" class="btn btn-danger update-gateway createdGateway" value="deactivateGateway" >Deactivate Gateway</button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Deny a Gateway request -->
<div class="modal fade" id="deny-gateway" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="{{URL::to('/')}}/admin/update-gateway-request" method="GET">
            
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                    <h3>Deny Gateway Request</h3>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label>SciGaP Admin Comments</label>
                        <textarea style="width:100%; height:80px" width="100%" name="comments"></textarea>
                    </div>
                </div>
                <input type="hidden" class="gatewayid-for-approval" name="gateway_id">
                <input type="hidden" name="internal_gateway_id">
                <div class="modal-footer">
                    <input type="submit" name="status" class="btn btn-danger" value="Deny"/>
                    <input type="cancel"  data-dismiss="modal"  class="btn btn-default" value="Cancel"/>
                </div>
            </form>

        </div>
    </div>
</div>
@endif

<!-- Remove a Compute Resource from a Gateway -->
<div class="modal fade" id="remove-compute-resource-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/gp/remove-cr" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Remove Compute Resource Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control remove-crId" name="rem-crId"/>
                    <input type="hidden" class="form-control cr-gpId" name="gpId"/>

                    Do you really want to remove the Compute Resource, <span class="remove-cr-name"> </span> from the
                    selected Gateway?
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-danger" value="Remove"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>

<!-- Remove a Storage Resource from a Gateway -->
<div class="modal fade" id="remove-storage-resource-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/gp/remove-sr" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Remove Storage Resource Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control remove-srId" name="rem-srId"/>
                    <input type="hidden" class="form-control sr-gpId" name="gpId"/>

                    Do you really want to remove the Storage Resource, <span class="remove-sr-name"> </span> from the
                    selected Gateway?
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-danger" value="Remove"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>

<!-- Add a Gateway -->
<div class="modal fade" id="add-gateway-loading" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true" data-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="text-center">Registering the gateway</h3>
            </div>
            <div class="modal-body text-center">
                <h5>Please DO NOT reload the page. This can take a couple of minutes.</h5>
                <img src="{{URL::to('/')}}/assets/ajax-loader.gif"/>
            </div>
        </div>
    </div>
</div>

@stop


@section('scripts')
@parent
{{ HTML::script('js/gateway.js') }}
{{ HTML::script('js/moment.js')}}
{{ HTML::script('js/datetimepicker-3.1.3.js')}}

<script>
    //make first tab of accordion open by default.
    //temporary fix
    $("#accordion2 .accordion-toggle").first().addClass("in").removeClass("collapsed");

    $(".credential-store-token-change > form").submit( function(e){
        $(this).prepend( "<img id='loading-gif' src='{{URL::to('/')}}/assets/ajax-loader.gif'/>");
        e.preventDefault();
        cstField = $("#gateway-credential-store-token");
        $.ajax({
            url: "{{URL::to('/')}}/gp/credential-store-token-change",
            method: "POST",
            data: { cst : cstField.val(), gateway_id: cstField.data("gpid") }
        }).done( function( data){
            $("#loading-gif").remove();
            alert( data);
        });
       
    });

    $(".set-cr-preference").submit( function( ev){
        var crForm = $(this);
        crForm.find(".loading-gif").removeClass("hide");

        ev.preventDefault();
        var datastring = crForm.serialize();
        $.ajax({
            type: "POST",
            url: "{{URL::to('/')}}/gp/update-crp",
            data: datastring,
            success: function(data) {
                if( data == 1)
                    crForm.find(".alert-success").removeClass("hide");
                else
                    crForm.find(".alert-danger").removeClass("hide");
            }
        }).complete( function(){
            crForm.find(".loading-gif").addClass("hide");
            setTimeout( function(){
                crForm.find(".alert-success").addClass("hide");
                crForm.find(".alert-danger").addClass("hide");
            }, 5000);
        });

    });

    $(".set-sr-preference").submit( function( ev){
        var srForm = $(this);
        srForm.find(".loading-gif").removeClass("hide");

        ev.preventDefault();
        var datastring = srForm.serialize();
        $.ajax({
            type: "POST",
            url: "{{URL::to('/')}}/gp/update-srp",
            data: datastring,
            success: function(data) {
                if( data == 1)
                    srForm.find(".alert-success").removeClass("hide");
                else
                    srForm.find(".alert-danger").removeClass("hide");
            }
        }).complete( function(){
            srForm.find(".loading-gif").addClass("hide");
            setTimeout( function(){
                srForm.find(".alert-success").addClass("hide");
                srForm.find(".alert-danger").addClass("hide");
            }, 5000);
        });

    });


    /*$(".add-tenant").slideUp();

    $(".toggle-add-tenant").click(function () {
        $('html, body').animate({
            scrollTop: $(".toggle-add-tenant").offset().top
        }, 500);
        $(".add-tenant").slideDown();
    });*/

    /*$("#add-tenant-form").submit(function (event) {
        event.preventDefault();
        event.stopPropagation();
        var formData = $("#add-tenant-form").serialize();
        $("#add-gateway-loading").modal("show");
        $(".loading-gif").removeClass("hide");
        $.ajax({
            type: "POST",
            data: formData,
            url: '{{ URL::to("/") }}/admin/add-gateway',
            success: function (data) {
                if( data.gateway == $(".gatewayName").val() ){
                    $(".gateway-success").html("Gateway has been added. The page will be reloaded in a moment.").removeClass("hide");
                    setTimeout( function(){
                        location.reload();
                    }, 3000);
                }
                else if( data == 0){
                    $(".gateway-error").html( "An unknown error occurred while trying to create the gateway.")
                                        .removeClass("hide");
                }
                else{
                    errors = data;
                    $(".gateway-error").html("").removeClass("hide");
                    for( input in data)
                    {
                        $(".gateway-error").append(" -- " + input + " : " + data[input] + "<br/><br/>");
                    }
                }
            },
            error: function( data){
                var error = $.parseJSON( data.responseText);
                $(".gateway-error").html(error.error.message).removeClass("hide");
            }
        }).complete(function () {
            $("#add-gateway-loading").modal("hide");
            $(".loading-gif").addClass("hide");
        });
    });*/

    disableInputs( $(".super-admin-gateways-view"));

    function disableInputs( elem){
      elem.find("input").each( function( i,e){
          if( $(e).attr("type")=='submit' || $(e).attr("type")=='button' || $(e).attr("type")=='checkbox')
              $(e).attr("disabled", "true");
           else
              $(e).prop("readonly", "true");
        });
        elem.find("textarea").prop("readonly", "true");
        elem.find("select").attr("disabled", "true");
        elem.find(".hide").prop("readonly", "true");
        elem.find("button").attr("disabled", "true");
        elem.find(".glyphicon").hide();
    }

    $(".start-approval").click( function(){
        $(".fail-alert").remove();
        $(".success-alert").remove();

        var gatewayObject = $(this).data("gatewayobject");
        var gatewayId = gatewayObject.gatewayId;
        if( gatewayId == undefined){
            gatewayObject = $.parseJSON( $(this).data("gatewayobject"));
            gatewayId = gatewayObject.gatewayId;
        }

        var gatewayApprovalStatuses = $.parseJSON( $(".gatewayApprovalStatuses").val() );

        $(".onTenantLoad").removeClass("hide");
        $(".gatewayid-for-approval").val( gatewayId).html(  gatewayId);
        $("#approve-gateway").find("input[name=internal_gateway_id]").val( gatewayObject.airavataInternalGatewayId );
        $(".gatewayName").val( gatewayObject.gatewayName);
        $(".gatewayAcronym").val( gatewayObject.gatewayAcronym);
        //$(".domain").val( gatewayObject.domain);
        $(".gatewayURL").val( gatewayObject.gatewayURL);
        $(".gatewayPublicAbstract").val( gatewayObject.gatewayPublicAbstract);
        $(".reviewProposalDescription").val( gatewayObject.reviewProposalDescription);
        $(".gatewayAdminFirstName").val( gatewayObject.gatewayAdminFirstName);
        $(".gatewayAdminLastName").val( gatewayObject.gatewayAdminLastName);
        if (gatewayObject.identityServerPasswordToken) {
            $(".identityServerPasswordToken").attr("placeholder", "Current token: " + gatewayObject.identityServerPasswordToken);
        }
        $(".emailAddress").val( gatewayObject.emailAddress);
        $(".identityServerUserName").val( gatewayObject.identityServerUserName);
        $(".oauthClientId").val( gatewayObject.oauthClientId);
        $(".oauthClientSecret").val( gatewayObject.oauthClientSecret);
        $(".declinedReason").val( gatewayObject.declinedReason);
        $(".declinedReason").html( gatewayObject.declinedReason);
        $(".gatewayApprovalStatus").val(gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus]);
        $(".onTenantComplete").addClass("hide");
        editableInputs( $("#update-gateway-request"), true);

        if( gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "REQUESTED"){
            $(".approvedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "updateGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "createTenant") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".createdGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "deployGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "deactivateGateway") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".notApprovedGateway").removeClass("hide"); {
                $('#emailAddress').attr('readonly', false);
                $('#gatewayURL').attr('readonly', false);
                $('#identityServerUserName').attr('readonly', false);
                $('#gatewayAdminFirstName').attr('readonly', false);
                $('#gatewayAdminLastName').attr('readonly', false);
                $('#gatewayAdminEmail').attr('readonly', false);
                $('#gatewayPublicAbstract').attr('readonly', false);
                $('#reviewProposalDescription').attr('readonly', false);
            }
        }
        else if( gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "APPROVED"){
            $(".notApprovedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "approveRequest") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "denyRequest") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".createdGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "deployGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "deactivateGateway") {
                    $(thisButton).addClass("hide");
                }
            });
            // Disallow creating tenant until password is set
            $("button[value=createTenant]").prop("disabled", !gatewayObject.identityServerPasswordToken);
            $(".approvedGateway").removeClass("hide"); {
                $('#emailAddress').attr('readonly', false);
                $('#gatewayURL').attr('readonly', false);
                $('#identityServerUserName').attr('readonly', false);
                $('#gatewayAdminFirstName').attr('readonly', false);
                $('#gatewayAdminLastName').attr('readonly', false);
                $('#gatewayAdminEmail').attr('readonly', false);
                $('#gatewayPublicAbstract').attr('readonly', false);
                $('#reviewProposalDescription').attr('readonly', false);
            }
        }
        else if( gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "CREATED"){
            $(".approvedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "updateGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "createTenant") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".notApprovedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "approveRequest") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "denyRequest") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".createdGateway").removeClass("hide"); {
                $('#emailAddress').attr('readonly', true);
                $('#gatewayURL').attr('readonly', true);
                $('#identityServerUserName').attr('readonly', true);
                $('#gatewayAdminFirstName').attr('readonly', true);
                $('#gatewayAdminLastName').attr('readonly', true);
                $('#gatewayAdminEmail').attr('readonly', true);
                $('#gatewayPublicAbstract').attr('readonly', true);
                $('#reviewProposalDescription').attr('readonly', true);
            }
        }
        else if( gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "DEPLOYED"){
            $(".notApprovedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "approveRequest") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "denyRequest") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".approvedGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "updateGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "createTenant") {
                    $(thisButton).addClass("hide");
                }
            });
            $(".createdGateway").each(function (i, thisButton) {
                if ($(thisButton).val() == "deployGateway") {
                    $(thisButton).addClass("hide");
                }
                if ($(thisButton).val() == "deactivateGateway") {
                    $(thisButton).removeClass("hide");
                }
                $('#emailAddress').attr('readonly', true);
                $('#gatewayURL').attr('readonly', true);
                $('#identityServerUserName').attr('readonly', true);
                $('#gatewayAdminFirstName').attr('readonly', true);
                $('#gatewayAdminLastName').attr('readonly', true);
                $('#gatewayAdminEmail').attr('readonly', true);
                $('#gatewayPublicAbstract').attr('readonly', true);
                $('#reviewProposalDescription').attr('readonly', true);
            });
        }
        else if( gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "CANCELLED" ||
            gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "DENIED" ||
            gatewayApprovalStatuses[ gatewayObject.gatewayApprovalStatus] == "DEACTIVATED"){

            editableInputs( $("#update-gateway-request"), false);
            $(".update-gateway-request-close-modal").removeAttr("disabled");
        }
        $("#approve-gateway").modal("show");
    });

    $(".update-gateway").click( function( ev){
        ev.preventDefault();
         $(this).prepend( "<img class='loading-gif' src='{{{ URL::to('/') }}}/assets/ajax-loader.gif'/>");

        $(".fail-alert").remove();
        $(".success-alert").remove();
        var updateVal = $(this).val();
        var updateGatewayData = $("#update-gateway-request").serializeArray();
            
        updateGatewayData.push({name: updateVal, value: true});

        dataObj = {}; // object containing all updatable gateway object elements

        for (i=0; i<updateGatewayData.length; i++) {
          dataObj[updateGatewayData[i].name] = updateGatewayData[i].value;
        }


        $.ajax({
            url: "{{URL::to('/')}}/admin/update-gateway-request",
            method: "GET",
            data: updateGatewayData,
            dataType: 'json'
        }).done( function( data){
            $(".loading-gif").remove();
            if( data.errors ){
                var messages = data.validationMessages;
                var errorMessages = [];
                for (var field in data.validationMessages) {
                    Array.prototype.push.apply(errorMessages, data.validationMessages[field]);
                }
                var errorMessagesList = $("<ul></ul>");
                errorMessages.forEach((errorMessage) => {
                    $("<li></li>").text(errorMessage).appendTo(errorMessagesList);
                });
                if( updateVal == "createTenant"){
                    $(".submit-actions")
                        .before("<div class='alert alert-danger fail-alert'>All fields are required to create the gateway! Please make sure you've first updated all the Gateway details accurately and try again.</div>")
                        .append(errorMessagesList);
                }
                else{
                    $("<div class='alert alert-danger fail-alert'>Error updating Gateway.</div>")
                        .insertBefore(".submit-actions")
                        .append(errorMessagesList);
                }
            }
            else{
                /*if( updateVal == "createTenant"){
                    $(".submit-actions").before("<div class='alert alert-success success-alert'>Tenant has been created!");
                    $(".notCreatedGateway").addClass("hide");

                    $(".createdGateway").removeClass("hide");
                }
                else{
                    $(".submit-actions").before("<div class='alert alert-success success-alert'>Gateway has been updated successfully.");
                }*/

                window.location.reload();

                //refresh data next time if same popup is opened.
                var gatewayIdWithoutSpaces = dataObj['gateway_id'].replace(/\s+/g, '-');
                $("#view-" +  gatewayIdWithoutSpaces).data("gatewayobject", data.gateway);
                $("#view-" + gatewayIdWithoutSpaces ).parent().parent().find(".form-gatewayName").html( dataObj['gatewayName']);
                $("#view-" + gatewayIdWithoutSpaces ).parent().parent().find(".form-gatewayURL").html( dataObj['gatewayURL']);
            }
        });

    });

    $(".gaStatuses option[value=REQUESTED]").prop("selected", true);
    $(".gatewayRow").slideUp();
    $(".gatewayStatus-REQUESTED").slideDown();

    $(".gaStatuses").on( 'change', function(){

        var statusToShow = $(this).val();
        if( statusToShow == "ALL"){
            $(".gatewayRow").slideDown();
        }
        else
        {
            $(".gatewayRow").slideUp();
            var gatewayApprovalStatuses = $.parseJSON( $(".gatewayApprovalStatuses").val() );
            $(".gatewayStatus-" + statusToShow ).slideDown();
        }
    });

    $(".deny-approval").click( function(){
        $(".gatewayid-for-approval").val( $(this).data("gatewayid")).html(  $(this).data("gatewayid"));
        $("#deny-gateway").find("input[name=internal_gateway_id]").val( gatewayObject.airavataInternalGatewayId );
        $("#deny-gateway").modal("show");
    });


    /* making datetimepicker work for reservation start and end date kept in compute-resource-preferences blade*/

    $('.datetimepicker1').datetimepicker({
        pick12HourFormat: false
        //pickTime: false
    });
    $('.datetimepicker2').datetimepicker({
        pick12HourFormat: false
        //pickTime: false
    });

    $(".datetimepicker1 input").focus( function(){
        $(this).parent().find(".glyphicon-calendar").click();
    });
    $(".datetimepicker2 input").focus( function(){
        $(this).parent().find(".glyphicon-calendar").click();
    });

    $(".datetimepicker1").on("dp.change", function (e) {
        $('.datetimepicker2').data("DateTimePicker").setMinDate(e.date);
        $(this).find(".glyphicon-calendar").click();
    });
    $(".datetimepicker2").on("dp.change", function (e) {
        $('.datetimepicker1').data("DateTimePicker").setMaxDate(e.date);
        $(this).find(".glyphicon-calendar").click();
    });


    $("[data-toggle=popover]").popover({
        'trigger': 'focus'
    });

    function editableInputs( elem, yes){
        if( yes){
            elem.find("input").removeAttr("disabled");
            elem.find("textarea").removeAttr("disabled");
            elem.find("select").removeAttr("disabled");
            elem.find("button").removeAttr("disabled");
        }
        else{
            elem.find("input").attr("disabled", "true");
            elem.find("textarea").prop("disabled", "true");
            elem.find("select").attr("disabled", "true");
            elem.find("button").attr("disabled", "true");
        }
    }

</script>
@stop