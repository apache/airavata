@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/bootstrap-toggle.css')}}
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
            <div class="success-message"></div>
            <div class="col-md-12">
<!--                <h1 class="text-center well alert alert-danger">Proposed(Dummy) UI for maintaining availability of-->
<!--                    Resources. More fields can be added.</h1>-->
                <h1 class="text-center">Resources</h1>

                <table class="table table-striped table-condensed">
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>
                            Enabled
                        </th>
                    </tr>
                    @foreach( (array)$resources as $resource)
                    <?php
                        $resourceId = $resource->computeResourceId;
                        $resourceName = $resource->hostName;
                        $enabled = $resource->enabled;
                    ?>
                    <tr class="user-row">
                        <td>{{ $resourceId }}</td>
                        <td>{{ $resourceName }}</td>
                        <td>
                            @if(!$enabled)
                                <input unchecked class="resource-status" resourceId="{{$resourceId}}" type="checkbox" data-toggle="toggle" data-on="Enabled" data-off="Disabled" data-onstyle="success" data-offstyle="danger">
                            @else
                                <input checked class="resource-status" resourceId="{{$resourceId}}" type="checkbox" data-toggle="toggle" data-on="Enabled" data-off="Disabled" data-onstyle="success" data-offstyle="danger">
                            @endif
                        </td>
                    </tr>
                    @endforeach
                </table>

            </div>
        </div>
    </div>
</div>
@stop

@section('scripts')
@parent
{{ HTML::script('js/bootstrap-toggle.js')}}
<script>
    // instantiate bootstrap toggle button
    $(".resource-status").bootstrapToggle();

    // bootstrap toggle button wraps checkbox in its own div named toggle
    $('.toggle').click(function() {
        var $this = $(this).find(".resource-status");

        /*  
            * conditional needs to be flipped because of the way the toggle checkbox UI works.
            * the state of checkbox AT clicktime is reported, but the intention is
            * to get the state that the checkbox switches to.
         */
        if (!($this.is(':checked'))) {
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
