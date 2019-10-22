@extends('layout.basic')

@section('page-header')
@parent
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
        <div class="panel-group" id="accordion">

            <!-- Super Admin Users have access to Scigap Administration -->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
                            Scigap Administration
                        </a>
                    </h4>
                </div>
                <div id="collapseOne" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <h1>Add a User to Admin</h1>

                        <form action="{{URL::to('/')}}/admin/adduser" method="POST" role="form"
                              enctype="multipart/form-data">

                            <div class="form-group required">
                                <label for="experiment-name" class="control-label">Enter Username</label>
                                <input type="text" class="form-control" name="username" id="experiment-name"
                                       placeholder="username" autofocus required="required">
                            </div>
                            <div class="btn-toolbar">
                                <input name="add" type="submit" class="btn btn-primary" value="Add User">
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <!-- Scigap Administration Ends -->

            <!-- Gateway Administration can be accessed by Super Admins and Gateway Admins can access their particular gateway -->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">
                            Gateways Administration
                        </a>
                    </h4>
                </div>
                <div id="collapseThree" class="panel-collapse collapse">
                    <div class="panel-body">
                        <div class="row">

                            <div class="col-md-6">
                                <h3>Existing Gateway Resource Profiles :</h3>
                            </div>
                            <div class="col-md-6" style="margin-top:3.5%">
                                <input type="text" class="col-md-12 filterinput" placeholder="Search by Gateway Name"/>
                            </div>
                        </div>
                        <div class="panel-group" id="accordion2">
                            @foreach( $gatewayProfiles as $indexGP => $gp )
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="accordion-toggle collapsed gateway-name" data-toggle="collapse"
                                           data-parent="#accordion2" href="#collapse-gateway-{{$indexGP}}">
                                            {{ $gp->gatewayName }}
                                        </a>

                                        <div class="pull-right col-md-2 gateway-options fade">
                                            <span class="glyphicon glyphicon-pencil edit-gateway"
                                                  style="cursor:pointer;" data-toggle="modal"
                                                  data-target="#edit-gateway-block" data-gp-id="{{ $gp->gatewayID }}"
                                                  data-gp-name="{{ $gp->gatewayName }}"
                                                  data-gp-desc="{{ $gp->gatewayDescription }}"></span>
                                            <span class="glyphicon glyphicon-trash delete-gateway"
                                                  style="cursor:pointer;" data-toggle="modal"
                                                  data-target="#delete-gateway-block"
                                                  data-gp-name="{{$gp->gatewayName}}"
                                                  data-gp-id="{{ $gp->gatewayID }}"></span>
                                        </div>
                                    </h4>
                                </div>
                                <div id="collapse-gateway-{{$indexGP}}" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="app-interface-block">
                                            <h5>{{ $gp->gatewayDescription}}</h5>
                                            <hr/>
                                            <div class="row">
                                                <div class="col-md-10">
                                                    <h4><span class="glyphicon glyphicon-plus"></span> Add a user as
                                                        Admin to this Gateway</h4>

                                                    <form action="{{URL::to('/')}}/admin/addgatewayadmin" method="POST"
                                                          role="form" enctype="multipart/form-data">
                                                        <div class="form-group required">
                                                            <label for="experiment-name" class="control-label">Enter
                                                                Username</label>
                                                            <input type="text" class="form-control" name="username"
                                                                   id="experiment-name" placeholder="username" autofocus
                                                                   required="required">
                                                            <input type="hidden" name="gateway_name"
                                                                   value="{{ $gp->gatewayName }}"/>
                                                        </div>
                                                        <div class="btn-toolbar">
                                                            <input name="add" type="submit" class="btn btn-primary"
                                                                   value="Add Admin"/>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            @endforeach
                        </div>
                    </div>
                </div>
            </div>

            <!-- Super Admin Users have access to Role Administration -->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo">
                            Role Adminstration
                        </a>
                    </h4>
                </div>
                <div id="collapseTwo" class="panel-collapse collapse">
                    <div class="panel-body">
                        <h1>Existing Roles</h1>
                        <table class="table table-condensed">
                            @foreach( $roles as $role)
                            <tr>
                                <td>{{ $role }}</td>
                                @if( $role != "admin")
                                <td><a href=""><span class="glyphicon glyphicon-pencil"></span></a></td>
                                <td><span class="glyphicon glyphicon-remove"></span></td>
                                @endif
                            </tr>
                            @endforeach
                        </table>
                        <h1>Add a new Role</h1>

                        <form id="role-form" action="{{URL::to('/')}}/admin/addrole" method="POST" role="form"
                              enctype="multipart/form-data">
                            <div class="form-group form-horizontal required col-md-2">
                                <label for="experiment-name" class="control-label">Enter Role</label>
                            </div>
                            <div class="form-group form-horizontal required col-md-4">
                                <input type="text" class="form-control input-small role-name" name="role"
                                       placeholder="username" autofocus required="required">
                            </div>
                            <div class="form-group form-horizontal col-md-4">
                                <input name="add" type="submit" class="btn btn-primary add-role" value="Add">
                            </div>
                        </form>
                        <input type="hidden" id="roles" value="{{ htmlentities( json_encode( $roles) ) }}"/>
                    </div>
                </div>
            </div>
            <!-- Role Administration complete -->

            <!-- Admins will have access to their settings -->
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-parent="#accordion" href="#collapseThree">
                            Settings
                        </a>
                    </h4>
                </div>
                <div id="collapseThree" class="panel-collapse collapse">
                    <div class="panel-body">
                        some settngs should come here.
                    </div>
                </div>
            </div>
            <!--Settings complete -->

        </div>
    </div>
</div>

@stop


@section('scripts')
@parent
<script>
    // Checking if role already exists.
    $(".add-role").click(function (e) {
        e.preventDefault();
        var roles = $.parseJSON($("#roles").val());
        if ($.inArray($.trim($(".role-name").val()), roles) != -1) {
            console.log($(this).parent().find(".alert").length);
            if (!$(this).parent().parent().find(".alert").length) {
                $(this).parent().after("<div class='col-md-12 alert alert-danger'>This role already exists. Please pick another role.</span>");
            }
        }
        else
            $("#role-form").submit();
    });
</script>
@stop