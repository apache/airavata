@extends('layout.basic')

@section('page-header')
@parent
@stop

@section('content')

<div class="container">
    <div class="col-md-offset-2 col-md-8">
        <div class="row">
            <a href="{{URL::to('/')}}/gp/create">
                <button class="btn btn-default create-gateway-profile">Create a new Gateway Resource Profile</button>
            </a>
        </div>
        @if( count( $gatewayProfiles) )
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
        <div class="row">

            <div class="col-md-6">
                <h3>Existing Gateway Resource Profiles :</h3>
            </div>
            <div class="col-md-6" style="margin-top:3.5%">
                <input type="text" class="col-md-12 filterinput" placeholder="Search by Gateway Name"/>
            </div>
        </div>
        <div class="panel-group" id="accordion1">
            @foreach( $gatewayProfiles as $indexGP => $gp )
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle collapsed gateway-name" data-toggle="collapse"
                           data-parent="#accordion" href="#collapse-gateway-{{$indexGP}}">
                            {{ $gp->gatewayName }}
                        </a>

                        <div class="pull-right col-md-2 gateway-options fade">
                            <span class="glyphicon glyphicon-pencil edit-gateway" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#edit-gateway-block"
                                  data-gp-id="{{ $gp->gatewayID }}" data-gp-name="{{ $gp->gatewayName }}"
                                  data-gp-desc="{{ $gp->gatewayDescription }}"></span>
                            <span class="glyphicon glyphicon-trash delete-gateway" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#delete-gateway-block"
                                  data-gp-name="{{$gp->gatewayName}}" data-gp-id="{{ $gp->gatewayID }}"></span>
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
                                    <button class="btn btn-default add-cr" data-gpid="{{$gp->gatewayID}}"><span
                                            class="glyphicon glyphicon-plus"></span> Add a Compute Resource
                                    </button>
                                </div>
                            </div>
                            @if( count( $gp->computeResourcePreferences) )
                            <div class="col-md-12">
                                <h3>Existing Compute Resources :</h3>
                            </div>
                            <div class="accordion-inner">
                                <div class="panel-group" id="accordion-{{$indexGP}}">
                                    @foreach( (array)$gp->computeResourcePreferences as $indexCRP => $crp )
                                    <div class="panel panel-default">
                                        <div class="panel-heading">
                                            <h4 class="panel-title">
                                                <a class="accordion-toggle collapsed gateway-name"
                                                   data-toggle="collapse" data-parent="#accordion"
                                                   href="#collapse-crp-{{$indexGP}}-{{$indexCRP}}">
                                                    {{ $crp->crDetails->hostName }}
                                                </a>

                                                <div class="pull-right col-md-2 gateway-options fade">
                                                    <span class="glyphicon glyphicon-remove remove-resource"
                                                          style="cursor:pointer;" data-toggle="modal"
                                                          data-target="#remove-resource-block"
                                                          data-cr-name="{{$crp->crDetails->hostName}}"
                                                          data-cr-id="{{$crp->computeResourceId}}"
                                                          data-gp-id="{{ $gp->gatewayID }}"></span>
                                                </div>
                                            </h4>
                                        </div>
                                        <div id="collapse-crp-{{$indexGP}}-{{$indexCRP}}"
                                             class="panel-collapse collapse">
                                            <div class="panel-body">
                                                <div class="app-compute-resource-preferences-block">
                                                    <form action="{{URL::to('/')}}/gp/update-crp" method="POST">
                                                        <input type="hidden" name="gatewayId" id="gatewayId"
                                                               value="{{$gp->gatewayID}}">
                                                        <input type="hidden" name="computeResourceId" id="gatewayId"
                                                               value="{{$crp->computeResourceId}}">

                                                        <div class="form-horizontal">
                                                            @include('partials/gateway-preferences',
                                                            array('computeResource' => $crp->crDetails, 'crData' =>
                                                            $crData, 'preferences'=>$crp, 'show'=>true))
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    @endforeach
                                </div>
                            </div>
                            @endif
                        </div>
                    </div>
                </div>
            </div>
            @endforeach
        </div>
        @endif
    </div>
</div>

<div class="add-compute-resource-block hide">
    <div class="well">
        <form action="{{URL::to('/')}}/gp/add-crp" method="POST">
            <input type="hidden" name="gatewayId" id="gatewayId" value="">

            <div class="input-group">
                <select name="computeResourceId" class="cr-select form-control" required>
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

<!-- Edit a Gateway Modal -->
<div class="modal fade" id="edit-gateway-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/gp/edit" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Edit Gateway</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control edit-gpId" name="edit-gpId"/>

                    <div class="form-group required">
                        <label class="control-label">Gateway Name</label>
                        <input class="form-control edit-gp-name" maxlength="100" name="gatewayName" required="required"
                               placeholder="Gateway Name"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Gateway Description</label>
                        <textarea class="form-control edit-gp-desc" maxlength="255" name="gatewayDescription"
                                  placeholder="Gateway Description"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-primary" value="Update"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>

<!-- delete a Gateway Modal -->
<div class="modal fade" id="delete-gateway-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/gp/delete-gp" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Delete Gateway Profile Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control delete-gpId" name="del-gpId"/>
                    Do you really want to delete the Gateway Profile, <span class="delete-gp-name"></span> ?
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

<!-- Remove a Compute Resource from a Gateway -->
<div class="modal fade" id="remove-resource-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
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

                    Do you really want to remove the Compute Resource, <span class="remove-cr-name"> </span>from the
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

<!-- contains all compute resource choices that might get selected on adding a new one to a gateway -->
@foreach( (array)$computeResources as $index => $cr)
@include('partials/gateway-preferences', array('computeResource' => $cr, 'crData' => $crData))
@endforeach


@stop

@section('scripts')
@parent
{{ HTML::script('js/gateway.js') }}
@stop