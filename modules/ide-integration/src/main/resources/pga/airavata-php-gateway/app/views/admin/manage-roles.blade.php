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

        <div class="container-fluid">
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

                <h1 class="text-center">Roles</h1>

                <table class="table table-striped table-condensed">
                    <tr>
                        <th>
                            Role
                        </th>
                        @if(Session::has("admin"))
                        <th>Actions</th>
                        @endif
                    </tr>
                    @foreach( $roles as $role)
                        @if((strpos($role, 'Internal') === false) && (strpos($role, 'Application') === false) )
                        <tr>
                            <td class="role-name">{{ $role }}</td>
                            @if(Session::has("admin"))
                            <td>
                                <!-- 
                                // unable to find functions to edit a role name so commenting for now
                                <span class="glyphicon glyphicon-pencil edit-role-name"></span>&nbsp;&nbsp;
                                -->
                                <a href="{{URL::to('/')}}/admin/dashboard/users?role={{$role}}">
                                    <span class="glyphicon glyphicon-user role-users"></span>&nbsp;&nbsp;
                                </a>
                                <span class="glyphicon glyphicon-trash delete-role"></span>&nbsp;&nbsp;
                            </td>
                            @endif
                        </tr>
                        @endif
                    @endforeach
                </table>
                @if(Session::has("admin"))
                {{--<div class="col-md-12">--}}
                    {{--<button type="button" class="btn btn-default toggle-add-role"><span--}}
                            {{--class="glyphicon glyphicon-plus"></span>Add a new Role--}}
                    {{--</button>--}}
                {{--</div>--}}
                <div class="add-role col-md-6">
                    <form role="form" action="{{URL::to('/')}}/admin/add-role" method="POST" class="add-role-form">
                        <div class="form-group">
                            <label>Enter Role Name</label>
                            <input type="text" name="role" class="form-control"/>
                        </div>
                        <div class="form-group">
                            <input type="submit" class="form-control btn btn-primary" value="Add"/>
                        </div>
                    </form>
                </div>

                <div class="edit-role hide">
                    <form class="edit-role-form">
                        <div class="form-group col-md-4">
                            <input type="text" name="new-role-name" class="new-role-name form-control"/>
                            <input type="hidden" name="original-role-name" class="original-role-name" value=""/>
                        </div>
                        <div class="form-group col-md-4">
                            <input type="submit" class="form-control btn btn-primary edit-role-submit" value="Edit"/>
                        </div>
                    </form>
                </div>
                @endif
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="delete-role-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/admin/delete-role" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Delete Role Confirmation</h3>
                </div>
                <div class="modal-body">
                    <div class="loading-count">
                        <img src="{{URL::to('/')}}/assets/ajax-loader.gif"/>
                    </div>
                    <div class="delete-warning-text hide">
                        <input type="hidden" class="form-control delete-roleName" name="role"/>
                        <h4 class="alert alert-warning">
                        <span class="role-user-count"> 0 </span> users currently have the role - <span class="delete-role-name"></span>. Do you really want to delete this role?</h4>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-danger" value="Delete"/>
                        <input type="button" class="btn btn-default cancel-delete-role" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>


    </div>
</div>


@stop

@section('scripts')
@parent
<script>
    $(".toggle-add-role").click(function () {
        $(".add-role").slideDown();
    });

    $(".edit-role-name").click(function () {
        var roleNameSpace = $(this).parent().parent().find(".role-name");
        if (roleNameSpace.find(".edit-role-form").length) {
            roleNameSpace.html(roleNameSpace.find(".original-role-name").val());
        }
        else {
            var role = roleNameSpace.html();
            roleNameSpace.html($(".edit-role").html());
            roleNameSpace.find(".original-role-name").val(role);
            roleNameSpace.find(".new-role-name").val(role);
        }
    });
    $(".delete-role").click(function () {
        var roleName = $(this).parent().parent().find(".role-name").html();
        $(".loading-count").removeClass("hide");
        $.ajax({
            type: "GET",
            url: "{{URL::to('/')}}/admin/getusercountinrole",
            data: {
                role: roleName
            }
        }).success( function( data){
            data = parseInt(data);
            if( data === parseInt(data, 10)){
                $(".role-user-count").html( data);
                $(".loading-count").addClass("hide");
                $(".delete-warning-text").removeClass("hide");
            }
            else{
                $(".loading-count").after("<h4 class='problem-retrieving-count alert alert-warning'>There was a problem retrieving number of users connected with this role. Do you still want to delete the role - " + roleName + "?</h4>");
                $(".loading-count").addClass("hide");
            }
        }).error( function(){
            $(".loading-count").after("<h4 class='problem-retrieving-count alert alert-warning'>There was a problem retrieving number of users connected with this role. Do you still want to delete the role - " + roleName + "?</h4>");
            $(".loading-count").addClass("hide");
        });
        $("#delete-role-block").modal("show");
        $(".delete-role-name").html(roleName);
        $(".delete-roleName").val(roleName);
    });

    $(".cancel-delete-role").click( function(){
            $(".loading-count").removeClass("hide");
            $(".delete-warning-text").addClass("hide");
            $(".problem-retrieving-count").remove();
    });
</script>
@stop