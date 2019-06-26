@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
@stop

@section('content')

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
        </div>
        <div class="container-fluid">

            <div class="row">

                <div class="col-md-6">
                    <h3>Existing Gateways :</h3>
                </div>
                <div class="col-md-6" style="margin-top:3.5%">
                    <input type="text" class="col-md-12 filterinput" placeholder="Search by Gateway Name"/>
                </div>
            </div>
            <table class="table table-bordered">
                <tr>
                    <th>No.</th>
                    <th>Gateway</th>
                    <th>Admin ( Not implemented yet.)</th>
                    <th>Actions</th>
                </tr>
                @foreach( $gateways as $indexGP => $gp)
                <tr>
                    <td>{{ $indexGP }}</td>
                    <td>{{ $gp->gatewayName }}</td>
                    <td>--</td>
                    <td>
                        <div class="gateway-options">
                            <span class="glyphicon glyphicon-pencil edit-gateway" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#edit-gateway-block"
                                  data-gp-id="{{ $gp->gatewayId }}" data-gp-name="{{ $gp->gatewayName }}"></span>
                            <span class="glyphicon glyphicon-trash delete-gateway" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#delete-gateway-block"
                                  data-gp-name="{{$gp->gatewayName}}" data-gp-id="{{ $gp->gatewayId }}"></span>
                        </div>
                    </td>
                </tr>
                @endforeach
            </table>
            <form id="add-tenant-form" action="{{ URL::to("/") }}/admin/add-gateway">
                <div class="col-md-12">
                    <button type="button" class="btn btn-default toggle-add-tenant"><span
                            class="glyphicon glyphicon-plus"></span>Add a new gateway
                    </button>
                </div>
                <div class="add-tenant col-md-6">
                    <div class="form-group required">
                        <label class="control-label">Enter Domain Name</label>
                        <input type="text" name="domain" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Desired Gateway Name</label>
                        <input type="text" name="gatewayName" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Admin Email Address</label>
                        <input type="text" name="admin-email" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Admin First Name</label>
                        <input type="text" name="admin-firstname" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Admin Last Name</label>
                        <input type="text" name="admin-lastname" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Admin Username</label>
                        <input type="text" name="admin-username" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Enter Admin Password</label>
                        <input type="password" name="admin-password" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <label class="control-label">Re-enter Admin Password</label>
                        <input type="password" name="admin-password-confirm" class="form-control" required="required"/>
                    </div>
                    <div class="form-group required">
                        <input type="submit" class="col-md-2 form-control btn btn-primary" value="Register"/>
                    </div>
                </div>
                <div class="col-md-6 alert alert-danger gateway-error hide">
                </div>
            </form>
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
                    @foreach( (array)$computeResources as $index => $cr)
                    <option value="{{ $cr->computeResourceId}}">{{ $cr->hostName }}</option>
                    @endforeach
                </select>
                <span class="input-group-addon remove-cr" style="cursor:pointer;">x</span>
            </div>
            <div class="pref-space form-horizontal"></div>
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
<script>

    $(".add-tenant").slideUp();

    $(".toggle-add-tenant").click(function () {
        $('html, body').animate({
            scrollTop: $(".toggle-add-tenant").offset().top
        }, 500);
        $(".add-tenant").slideDown();
    });

    /*$("#add-tenant-form").submit(function (event) {
        event.preventDefault();
        event.stopPropagation();
        var formData = $("#add-tenant-form").serialize();
        $("#add-gateway-loading").modal("show");
        $.ajax({
            type: "POST",
            data: formData,
            url: '{{ URL::to("/") }}/admin/add-gateway',
            success: function (data) {
                
            },
            error: function( data){
                var error = $.parseJSON( data.responseText);
                $(".gateway-error").html(error.message).removeClass("hide");
            }
        }).complete(function () {
            $("#add-gateway-loading").modal("hide");
        });
    });*/
</script>
@stop