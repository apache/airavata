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

    @if ( isset( $allSRs) )
        @if (sizeof( $allSRs) == 0)
        {{ CommonUtilities::print_warning_message('No Storage Resources are registered.') }}
        <br/> 
        <a href="{{ URL::to('/')}}/sr/create" class="btn btn-primary">Create a new Storage Resource</a>
        @else
        <br/>
        <div class="col-md-12">
            <div class="panel panel-default form-inline">
                <div class="panel-heading">
                    <h3 style="margin:0;">Search Storage Resources</h3>
                </div>
                <div class="panel-body">
                    <div class="form-group search-text-block">
                        <label>Data Storage Resource Name </label>
                        <input type="search" class="form-control filterinput"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="table-responsive">
                    <table class="table">

                        <tr>
                            <th>Id</th>
                            <th>Hostname</th>
                            <th>Enabled</th>
                            <th>View</th>
                            @if(Session::has("super-admin"))
                            <th>Delete</th>
                            @endif
                        </tr>

                        @foreach($allSRs as $resource)
                        <?php
                            $srId = $resource->storageResourceId;
                            $hostName = $resource->hostName;
                            $enabled = $resource->enabled;
                        ?>
                        <tr id="srDetails">
                            <td><a href="{{URL::to('/')}}/sr/edit?srId={{ $srId }}" title="Edit">{{ $srId }}</a></td>
                            <td>{{ $hostName }}</td>
                            <td>
                            <div class="checkbox">
                                <input class="storage-resource-status" type="checkbox" resourceId="{{$srId}}" @if($enabled) checked @endif
                                   @if(!Session::has("super-admin"))
                                       disabled="disabled"
                                   @endif
                                   >
                            </div>
                        </td>
                            <td>
                                <a href="{{URL::to('/')}}/sr/edit?srId={{ $srId }}" title="Edit">
                                <span class="glyphicon glyphicon-list"></span>
                                </a>
                            </td>
                            @if(Session::has("super-admin"))
                            <td>
                                <a href="#" title="Delete">
                                    <span class="glyphicon glyphicon-trash del-sr" data-toggle="modal"
                                          data-target="#delete-sr-block" data-srid="{{$srId}}"></span>
                                </a>
                            </td>
                            @endif
                        </tr>
                        @endforeach

                    </table>
                </div>
            </div>
        </div>
        @endif
    @endif

    <div class="modal fade" id="delete-sr-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
         aria-hidden="true">
        <div class="modal-dialog">
            <form action="{{URL::to('/')}}/sr/delete-sr" method="POST">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="text-center">Delete Data Storage Resource Confirmation</h3>
                    </div>
                    <div class="modal-body">
                        <input type="hidden" class="form-control delete-srId" name="del-srId"/>
                        Do you really want to delete Data Storage Resource, <span class="delete-sr-id"></span>? This action cannot be undone.
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

    $(".del-sr").click(function () {
        $(".delete-sr-id").html("'" + $(this).data("srid") + "'");
        $(".delete-srId").val($(this).data("srid"));
    });

    $('.storage-resource-status').click(function() {
        var $this = $(this);
        if ($this.is(':checked')) {
            //enable compute resource
            $resourceId = $this.attr("resourceId");
            $.ajax({
                type: 'POST',
                url: "{{URL::to('/')}}/admin/enable-sr",
                data: {
                    'resourceId': $resourceId
                },
                async: true,
                success: function (data) {
                    $(".success-message").html("<span class='alert alert-success col-md-12'>Successfully enabled Storage Resource</span>");
                }
            });
        } else {
            //disabled compute resource
            $resourceId = $this.attr("resourceId");
            $.ajax({
                type: 'POST',
                url: "{{URL::to('/')}}/admin/disable-sr",
                data: {
                    'resourceId': $resourceId
                },
                async: true,
                success: function (data) {
                    $(".success-message").html("<span class='alert alert-success col-md-12'>Successfully disabled Storage Resource</span>");
                }
            });
        }
    });
</script>
@stop