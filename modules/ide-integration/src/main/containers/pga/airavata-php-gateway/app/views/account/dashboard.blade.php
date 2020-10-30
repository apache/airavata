@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
@stop

@section('content')
<div class="container">
    <div class="col-md-12">
        @if( Session::has("message"))
        <div class="row">
            <div class="alert alert-success alert-dismissible" role="alert">
                <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
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


        @if( Session::has('new-gateway-provider') )
            <div style="margin-top:50px;" class="col-md-12">
            @if( Session::has("existing-gateway-provider") )
                <h3>List of Requested Gateways</h3>
                <table class="table table-bordered">
                    <thead>
                        <tr class="text-center">
                            <th style="vertical-align: top; text-align: center">Gateway Name</th>
                            <th style="vertical-align: top; text-align: center">Creation Time</th>
                            <th style="vertical-align: top; text-align: center">Gateway URL</th>
                            <th style="vertical-align: top; text-align: center">Project Details</th>
                            <th style="vertical-align: top; text-align: center">Project Abstract</th>
                            <th style="vertical-align: top; text-align: center">Gateway Request Status</th>
                            <th style="vertical-align: top; text-align: center">Actions</th>
                            <th style="vertical-align: top; text-align: center">SciGaP Admin Comments</th>
                        </tr>
                    </thead>
                    <tbody>
                    @foreach( $requestedGateways as $internalGatewayId => $gateway)
                        <tr>
                            <td>{{ $gateway["gatewayInfo"]->gatewayName }}</td>
                            <?php 
                                $timeDifference = Session::get("user_timezone");
                                $addOrSubtract = "-";
                                if( $timeDifference < 0)
                                    $addOrSubtract = "+";

                                $creationTime = date('m/d/Y h:i:s A', 
                                                    intval( strtotime( $addOrSubtract . " " . Session::get("user_timezone") . " hours", $gateway["gatewayInfo"]->requestCreationTime/1000) ) );
                            ?>
                            <td>{{ $creationTime}}</td>
                            <td>{{ $gateway["gatewayInfo"]->gatewayURL }}</td>
                            <td style="max-width: 400px; word-wrap: break-word;">{{ $gateway["gatewayInfo"]->reviewProposalDescription }}</td>
                            <td style="max-width: 400px; word-wrap: break-word;">{{ $gateway["gatewayInfo"]->gatewayPublicAbstract }}</td>
                            <td>{{ $gateway["approvalStatus"] }}</td>
                            <td>
                                @if( $gateway["approvalStatus"] == "CREATED" || $gateway["approvalStatus"] == "DEPLOYED" )
                                    <div class="btn-group" role="group" aria-label="...">
                                        <button type="button" class="btn btn-default view-credentials" data-gatewayobject="{{ htmlentities( json_encode( $gateway['gatewayInfo'])) }}">View Credentials</button>
                                        <!--
                                        <button type="button" class="btn btn-default"><a href="{{URL::to('/')}}/admin/dashboard?gatewayId={{$internalGatewayId}}">Manage Gateway</a></button>
                                        
                                        <button type="button" class="btn btn-danger deactivateGateway-button" data-toggle="modal" data-target="#deactivateGateway" data-gateway_id="{{$gateway['gatewayInfo']->gatewayId}}" data-internal_gateway_id="{{$internalGatewayId}}">Deactivate Gateway</button>
                                        -->
                                    </div>
                                @elseif( $gateway["approvalStatus"] == "REQUESTED" || $gateway["approvalStatus"] == "APPROVED")
                                    <a href="{{URL::to('/')}}/admin/update-gateway-request?internal_gateway_id={{$internalGatewayId}}&gateway_id={{$gateway['gatewayInfo']->gatewayId}}&cancelRequest=true">
                                        <button type="button" class="btn btn-danger">Cancel Request</button>
                                    </a>
                                    @if( $gateway["approvalStatus"] == "APPROVED")
                                        <a href="{{URL::to('/')}}/account/update-gateway?gateway-id={{$internalGatewayId}}&updateRequest=true">
                                            <button type="button" class="gateway-update-button btn btn-default">Update Request</button>
                                        </a>
                                    @endif
                                @endif
                            </td>
                            <td>
                                {{$gateway["gatewayInfo"]->declinedReason}}
                            </td>
                        </tr>
                    @endforeach
                    </tbody>
                </table>
            @endif
            </div>
                <div class="col-md-12">
                    <div>
                        <button class="gateway-request-button btn btn-default">Request a New Gateway</button>
                        <br/>
                    </div>
            @if(empty($requestedGateways))
                <br>
                <div class="well">
                    <h4>You are in this page to create request for a new Gateway. Please click 'Request a New Gateway' button and proceed. Once
                        your request is submitted, a SciGaP admin will process the requirement and if needed you will be contacted.
                    </h4>
                </div>
            @endif
            <br/>
            <div class="well">
                <h6 class="text-center">Need faster or more customised solutions for your Gateway? Contact us at: <a href="mailto:help@scigap.org">help@scigap.org</a></h6>
            </div>

            @if ($errors->has())
                @foreach ($errors->all() as $error)
                {{ CommonUtilities::print_error_message($error) }}
                @endforeach
            @endif

            <div class="row @if(! $errors->has())hide @endif gateway-request-form">
                <div class="col-md-offset-2 col-md-8">
                    <form id="request-tenant-form" action="{{ URL::to('/') }}/provider/request-gateway">
                        <div class="col-md-12 text-center" style="margin-top:20px;">
                            <h3>Request your gateway now!</h3>
                        </div>
                        <div class="form-group required">
                            <label class="control-label">Gateway Name</label>
                            <input type="text" maxlength="50" name="gateway-name" class="form-control" required="required" value="{{Input::old('gateway-name') }}" />
                        </div>
                        <div class="form-group required">
                            <label class="control-label">Gateway Contact Email</label>
                            <input type="text" name="email-address" class="form-control" required="required" value="{{Input::old('email-address') }}"/>
                        </div>
                        <div class="form-group required">
                            <label class="control-label">Public Project Description</label>
                            <textarea type="text" name="public-project-description" maxlength="250" id="public-project-description" class="form-control" required="required"  data-container="body" data-toggle="popover" data-placement="left" data-content="This description will be used to describe the gateway in the Science Gateways List. It help a user decide whether or not this gateway will be useful to them.">{{Input::old('public-project-description') }}</textarea>
                        </div>
                        <input type="submit" value="Send Request" class="btn btn-primary"/>
                        <input type="reset" value="Reset" class="btn">
                    </form>
                </div>
            </div>
            <hr/>
            </div>

            <hr/>
    </div>


        <!-- View Credentials -->
        <div class="modal fade" id="viewCredentials" tabindex="-1" role="dialog" aria-labelledby="vc">
          <div class="modal-dialog" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">Gateway Credentials</h4>
              </div>
              <div class="modal-body">
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th>Property</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Gateway Name</td>
                            <td class="gateway-id"></td>
                        </tr>
                        <tr>
                            <td>Gateway URL</td>
                            <td class="gateway-url"></td>
                        </tr>
                        <tr>
                            <td>Admin Username</td>
                            <td class="admin-username"></td>
                        </tr>
                        <tr>
                            <td>Oauth Client Key</td>
                            <td class="oauth-client-key"></td>
                        </tr>
                        <tr>
                            <td>Oauth Client Secret</td>
                            <td class="oauth-client-secret"></td>
                        </tr>
                    </tbody>
                </table>
              </div>
              <!--
              <div class="modal-footer">
              </div>
              -->
            </div>
          </div>
        </div>

        <!-- Deactivate Modal -->
        <div class="modal fade" id="deactivateGateway" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
          <div class="modal-dialog" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Deactivate Confirmation</h4>
              </div>
              <div class="modal-body">
                Are you sure, you want to deactivate this Gateway? This action cannot be undone.
              </div>
              <div class="modal-footer">
                <form action="{{URL::to('/')}}/admin/update-gateway-request?status=3" method="GET">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <input type="hidden" id="deactivateGatewayId" name="gateway_id" value=""/>
                    <input type="hidden" id="deactivateInternalGatewayId" name="internal_gateway_id" value=""/>
                    <button type="submit" class="btn btn-danger">Deactivate</button>
                </form>
              </div>
            </div>
          </div>
        </div>
        @elseif( Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only') )
        <div class="row text-center breathing-space">
            <h1>Gateway: {{Session::get("gateway_id")}}</h1>
            <h3>Let's get started!</h3>
        </div>
        @if (Session::has('auto_provisioned_accounts'))
            @include('partials/auto-provisioned-accounts', array("auto_provisioned_accounts" => Session::get('auto_provisioned_accounts')))
        @endif
        <div class="row text-center admin-options">

            <div class="row well">

                <h3>See what's happening in your projects</h3>

                <a href="{{URL::to('/')}}/project/browse">
                    <div class="@if( Session::has('admin') || Session::has('admin-read-only')) col-md-4 @else col-md-6 @endif well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-off console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Projects</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/experiment/browse">
                    <div class="@if( Session::has('admin') || Session::has('admin-read-only')) col-md-4 @else col-md-6 @endif well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-tasks console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Experiments</h4>
                        </div>
                    </div>
                </a>

                @if( Session::has('admin') || Session::has('admin-read-only'))
                <a href="{{URL::to('/')}}/admin/dashboard/experiments">
                    <div class="col-md-4  well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-stats console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Experiment Statistics</h4>
                        </div>
                    </div>
                </a>
                @endif
            </div>

            @if( Session::has('admin') || Session::has('admin-read-only') )

            <div class="row well">

                <h3>Manage Users Access</h3>
                <a href="{{URL::to('/')}}/admin/dashboard/users">
                    <div class="col-md-4 col-md-offset-4 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-user  console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Users</h4>
                        </div>
                    </div>
                </a>

            </div>

            <div class="row well">

                <h3>Manage Computing and Storage Resources and Preferences for your Gateway</h3>

                <a href="{{URL::to('/')}}/cr/browse">
                    <div class=" col-md-3 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-briefcase  console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Compute Resources</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/admin/dashboard/gateway">
                    <div class=" col-md-3 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-sort console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Gateway Management</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/sr/browse">
                    <div class=" col-md-3 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-folder-open console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Storage Resources</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/admin/dashboard/credential-store">
                    <div class=" col-md-3 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-lock console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Credential Store</h4>
                        </div>
                    </div>
                </a>

            </div>

            <div class="row well">

                <h3>Manage Application Modules, Interfaces and Deployments</h3>
                <a href="{{URL::to('/')}}/app/module">
                    <div class="col-md-4 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-th-large console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Application Modules</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/app/interface">
                    <div class="col-md-4 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-phone console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Application Interfaces</h4>
                        </div>
                    </div>
                </a>

                <a href="{{URL::to('/')}}/app/deployment">
                    <div class="col-md-4 well">
                        <div class="col-md-12">
                            <span class="glyphicon glyphicon-random console-icon"></span>
                        </div>
                        <div class="col-md-12">
                            <h4>Browse Application Deployments</h4>
                        </div>
                    </div>
                </a>
                @endif

                
                <!--
                <div class=" col-md-4">
                    <div class="col-md-12">
                        <span class="glyphicon glyphicon-list-alt console-icon"></span>
                    </div>
                    <div class="col-md-12">
                        Reports
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-4">
                    <div class="col-md-12">
                        <span class="glyphicon glyphicon-question-sign console-icon"></span>
                    </div>
                    <div class="col-md-12">
                        Support
                    </div>
                </div>
            </div>
            -->

        </div>
    </div>
    @else
    <div>
        <div class="row text-center breathing-space">
            <h1>Hi! You look new here.</h1>
        </div>
        <div class="row well">
            <h4>Your {{ Config::get('pga_config.portal')['portal-title'] }} account is pending approval. You will be notified via email upon approval by {{ Config::get('pga_config.portal')['portal-title'] }} Admin.</h4>
        </div>
    </div>
    @endif

    <!--
    Hidden until completed.
    <div class="col-md-12 text-center">
        <a href="{{URL::to('/')}}/allocation-request">
            <button class="btn btn-default ">Request an allocation</button>
        </a>
    </div>
    -->

</div>

@stop

@section('scripts')
@parent
<script>

    $(".add-tenant").slideUp();

    $(".toggle-add-tenant").click(function () {
        $('html, body').animate({
            scrollTop: $(".toggle-add-tenant").offset().top
        }, 500);
        $(".add-tenant").slideDown();
    });

    $(".gateway-request-button").click( function(){
        $(".gateway-request-form").removeClass("hide");
    });

    $("#password").popover({
        'trigger':'focus'
    });

    $("#gateway-url").popover({
        'trigger':'focus'
    });

    $(".gateway-acronym").popover({
        'trigger':'focus'
    });

    $("#project-details").popover({
        'trigger':'focus'
    });

    $("#public-project-description").popover({
        'trigger':'focus'
    });

    /*
    $("#add-tenant-form").on("submit", function(e){
        e.preventDefault();
        console.log( !/[^a-z]/i.test( $(".gateway-acronym").val()));
    });
    */

    $(".deactivateGateway-button").click( function(){
        var gatewayId = $(this).data("gateway_id");
        var internalGatewayId = $(this).data("internal_gateway_id");
        $("#deactivateGatewayId").val( gatewayId );
        $("#deactivateInternalGatewayId").val( internalGatewayId );
    });

    $(".view-credentials").click( function(){
        var gatewayObject = $(this).data("gatewayobject");
        $(".admin-username").html( gatewayObject["identityServerUserName"]);
        $(".admin-password").html( gatewayObject["identityServerPasswordToken"]);
        $(".gateway-id").html( gatewayObject["gatewayId"]);
        $(".gateway-url").html( gatewayObject["gatewayURL"]);
        $(".gateway-domain").html( gatewayObject["domain"]);
        $(".oauth-client-key").html( gatewayObject["oauthClientId"]);
        $(".oauth-client-secret").html( gatewayObject["oauthClientSecret"]);
        $("#viewCredentials").modal("show");
    });
</script>
@stop