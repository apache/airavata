@extends('layout.basic')

@section('page-header')
    @parent
    {{ HTML::style('css/admin.css')}}
@stop

@section('content')
    <div class="container">
        <div class="col-md-12">
            <div class="row gateway-update-form">
                <div class="col-md-offset-2 col-md-8">

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

                    @if ($errors->has())
                        @foreach ($errors->all() as $error)
                            {{ CommonUtilities::print_error_message($error) }}
                        @endforeach
                    @endif

                    <form id="add-tenant-form" action="{{ URL::to('/') }}/admin/add-new-gateway">
                        <div class="col-md-12 text-center" style="margin-top:20px;">
                            <h3>Add your Gateway now!</h3>
                        </div>
                        <div class="form-group required">
                            <label class="control-label">Gateway Name</label>
                            <input type="text" maxlength="50" name="gateway-name" class="form-control" required="required" value="{{Input::old('gateway-name') }}" />
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway URL</label>
                            <input type="text" name="gateway-url" id="gateway-url" class="form-control" value="{{Input::old('gateway-url') }}" data-container="body" data-toggle="popover" data-placement="left" data-content="URL to Portal home page or Download URL (for desktop applications) where gateway has been deployed."/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway Contact Email</label>
                            <input type="text" name="email-address" class="form-control" required="required" value="{{Input::old('email-address') }}"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway Admin Username</label>
                            <input type="text" name="admin-username" value="{{Input::old('admin-username')}}" class="form-control" required="required" />
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Gateway Admin Password</label>
                            <input type="password" id="password" name="admin-password" class="form-control" required="required" title="" type="password" data-container="body" data-toggle="popover" data-placement="left" data-content="Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$*"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Admin Password Confirmation</label>
                            <input type="password" name="admin-password-confirm" class="form-control" required="required"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Admin First Name</label>
                            <input type="text" name="admin-firstname" class="form-control" required="required" value="{{Input::old('admin-firstname') }}"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Admin Last Name</label>
                            <input type="text" name="admin-lastname" class="form-control" required="required" value="{{Input::old('admin-lastname') }}"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Admin Email ID</label>
                            <input type="text" name="admin-email" class="form-control" required="required" value="{{Input::old('admin-email') }}"/>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Project Details</label>
                            <textarea type="text" name="project-details" maxlength="250" id="project-details" class="form-control" required="required"  data-container="body" data-toggle="popover" data-placement="left" data-content="This information will help us to understand and identify your gateway requirements, such as local or remote resources, user management, field of science and communities supported, applications and interfaces, license handling, allocation management, data management, etc... It will help us in serving you and providing you with the best option for you and your research community.">{{Input::old('project-details') }}</textarea>
                        </div>

                        <div class="form-group required">
                            <label class="control-label">Public Project Description</label>
                            <textarea type="text" name="public-project-description" maxlength="250" id="public-project-description" class="form-control" required="required"  data-container="body" data-toggle="popover" data-placement="left" data-content="This description will be used to describe the gateway in the Science Gateways List. It help a user decide whether or not this gateway will be useful to them.">{{Input::old('public-project-description') }}</textarea>
                        </div>

                        <input type="submit" value="Submit" class="btn btn-primary"/>
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