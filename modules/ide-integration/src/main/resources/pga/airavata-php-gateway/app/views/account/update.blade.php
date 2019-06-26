@extends('layout.basic')

@section('page-header')
    @parent
    {{ HTML::style('css/admin.css')}}
@stop

@section('content')
    <div class="container">
        <div class="col-md-12">

            @if (Session::has("messages"))
                <div class="row">
                    <div class="alert alert-success alert-dismissible" role="alert">
                        <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                                    class="sr-only">Close</span></button>
                        {{ Session::get("messages") }}
                    </div>
                </div>
                {{ Session::forget("messages") }}
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

            <div class="row gateway-update-form">
                <div class="col-md-offset-2 col-md-8">

                    @if ($errors->has())
                        @foreach ($errors->all() as $error)
                            {{ CommonUtilities::print_error_message($error) }}
                        @endforeach
                    @endif

                    <form id="add-tenant-form" action="{{ URL::to('/') }}/provider/update-details?updateRequest=true">
                        <div class="col-md-12 text-center" style="margin-top:20px;">
                            <h3>Update your Gateway details now!</h3>
                        </div>
                        <input type="hidden" name="internal-gateway-id" value="{{ $gatewayData["airavataInternalGatewayId"] }}" />
                        <div class="form-group required">
                            <label class="control-label">Gateway ID</label>
                            <input type="text" maxlength="50" name="gateway-id" class="form-control" readonly="readonly" required="required" value="{{ $gatewayData["gatewayId"] }}" />
                        </div>
                        <div class="form-group required">
                            <label class="control-label">Gateway Name</label>
                            <input type="text" maxlength="50" name="gateway-name" class="form-control" readonly="readonly" required="required" value="{{ $gatewayData["gatewayName"] }}" />
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway URL</label>
                            <input type="text" name="gateway-url" id="gateway-url" class="form-control" value="{{ $gatewayData["gatewayURL"] }}" data-container="body" data-toggle="popover" data-placement="left" data-content="URL to Portal home page or Download URL (for desktop applications) where gateway has been deployed."/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway Contact Email</label>
                            <input type="text" name="email-address" class="form-control" required="required" value="{{ $gatewayData["emailAddress"] }}"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Project Details</label>
                            <textarea type="text" name="project-details" maxlength="250" id="project-details" class="form-control" required="required"  data-container="body" data-toggle="popover" data-placement="left" data-content="This information will help us to understand and identify your gateway requirements, such as local or remote resources, user management, field of science and communities supported, applications and interfaces, license handling, allocation management, data management, etc... It will help us in serving you and providing you with the best option for you and your research community.">{{ $gatewayData["projectDetails"] }}</textarea>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Public Project Description</label>
                            <textarea type="text" name="public-project-description" maxlength="250" id="public-project-description" class="form-control" required="required"  data-container="body" data-toggle="popover" data-placement="left" data-content="This description will be used to describe the gateway in the Science Gateways List. It help a user decide whether or not this gateway will be useful to them.">{{ $gatewayData["publicProjectDescription"] }}</textarea>
                        </div>

                        <input type="submit" value="Update Details" class="btn btn-primary"/>
                        <input type="reset" value="Reset" class="btn">
                    </form>
                </div>
            </div>
        </div>
    </div>
    </div>
@stop

@section('scripts')
@parent
<script>

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