@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
{{ HTML::style('css/datetimepicker.css')}}
@stop

@section('content')

<div id="wrapper">
    <!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
    @include( 'partials/dashboard-block')
    <div id="page-wrapper">

<div class="container-fluid">
    @if( Session::has("message"))
    <div class="col-md-12">
        <span class="alert alert-success">{{ Session::get("message") }}</span>
    </div>
    {{ Session::forget("message") }}
    @endif

    @if ( isset( $allCRs) )
    @if (sizeof($allCRs) == 0)
    {{ CommonUtilities::print_warning_message('No Compute Resources are registered. Please use "Register Compute
    Resource" to
    register a new resources.') }}
    @else
    <br/>
    <div class="col-md-12">
        <div class="panel panel-default form-inline">
            <div class="panel-heading">
                <h3 style="margin:0;">Search Compute Resources</h3>
            </div>
            <div class="panel-body">
                <div class="form-group search-text-block">
                    <label>Compute Resource Name </label>
                    <input type="search" class="form-control filterinput"/>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="table-responsive">
                <table class="table">

                    <tr>

                        <th>Name</th>
                        <th>Id</th>
                        <th>
                            Enabled
                        </th>
                        <th>View</th>
                        @if(Session::has("super-admin"))
                            <th>Delete</th>
                        @endif
                    </tr>

                    @foreach($allCRs as $resource)
                    <?php
                        $crId = $resource->computeResourceId;
                        $crName = $resource->hostName;
                        $enabled = $resource->enabled;
                    ?>
                    <tr id="crDetails">
                        <td><a href="{{URL::to('/')}}/cr/edit?crId={{ $crId }}" title="Edit">{{ $crName }}</a></td>
                        <td>{{ $crId }}</td>
                        <td>
                            <div class="checkbox">
                                <input class="resource-status" type="checkbox" resourceId="{{$crId}}" @if($enabled) checked @endif
                                   @if(!Session::has("super-admin"))
                                       disabled="disabled"
                                   @endif
                                   >
                            </div>
                        </td>
                        <td><a href="{{URL::to('/')}}/cr/edit?crId={{ $crId }}" title="Edit">
                                <span class="glyphicon glyphicon-list"></span>
                            </a>
                        </td>
                        @if(Session::has("super-admin"))
                            <td>
                            <a href="#" title="Delete">
                                <span class="glyphicon glyphicon-trash del-cr" data-toggle="modal"
                                      data-target="#delete-cr-block" data-delete-cr-name="{{$crName}}"
                                      data-deployment-count="{{$connectedDeployments[$crId]}}"
                                      data-crid="{{$crId}}"></span>
                            </a>
                        </td>
                        @endif
                    </tr>
                    @endforeach

                </table>
            </div>
        </div>
        @endif
        @endif

        <div class="modal fade" id="delete-cr-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
             aria-hidden="true">
            <div class="modal-dialog">

                <form action="{{URL::to('/')}}/cr/delete-cr" method="POST">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3 class="text-center">Delete Compute Resource Confirmation</h3>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" class="form-control delete-crId" name="del-crId"/>
                            The Compute Resource, <span class="delete-cr-name"></span> is connected to <span
                                class="deploymentCount">0</span> deployments.
                            Do you really want to delete it? This action cannot be undone.
                        </div>
                        <div class="modal-footer">
                            <div class="form-group">
                                <input type="submit" class="btn btn-danger" value="Delete"/>
                                <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                            </div>
                        </div>
                    </div>

                </form>
            </div>
        </div>

    </div>
</div>
</div>

    @stop
    @section('scripts')
    @parent
    <script type="text/javascript">
        $('.filterinput').keyup(function () {
            var value = $(this).val();
            if (value.length > 0) {
                $("table tr").each(function (index) {
                    if (index != 0) {

                        $row = $(this);

                        var id = $row.find("td:first").text();
                        id = $.trim(id);
                        id = id.substr(0, value.length);
                        if (id == value) {
                            $(this).slideDown();
                        }
                        else {
                            $(this).slideUp();
                        }
                    }
                });
            } else {
                $("table tr").slideDown();
            }
            return false;
        });

        $(".del-cr").click(function () {
            $(".delete-cr-name").html("'" + $(this).data("delete-cr-name") + "'");
            $(".delete-crId").val($(this).data("crid"));
            $(".deploymentCount").html($(this).data("deployment-count"));
        });

        $('.resource-status').click(function() {
            var $this = $(this);
            if ($this.is(':checked')) {
                //enable compute resource
                $resourceId = $this.attr("resourceId");
                $.ajax({
                    type: 'POST',
                    url: "{{URL::to('/')}}/admin/enable-cr",
                    data: {
                        'resourceId': $resourceId
                    },
                    async: true,
                    success: function (data) {
                        console.log("enabled cr " + $resourceId);
                        $(".success-message").html("<span class='alert alert-success col-md-12'>Successfully enabled compute resource</span>");
                    }
                });
            } else {
                //disabled compute resource
                $resourceId = $this.attr("resourceId");
                $.ajax({
                    type: 'POST',
                    url: "{{URL::to('/')}}/admin/disable-cr",
                    data: {
                        'resourceId': $resourceId
                    },
                    async: true,
                    success: function (data) {
                        console.log("disabled cr " + $resourceId);
                        $(".success-message").html("<span class='alert alert-success col-md-12'>Successfully disabled compute resource</span>");
                    }
                });
            }
        });
        </script>
    @stop