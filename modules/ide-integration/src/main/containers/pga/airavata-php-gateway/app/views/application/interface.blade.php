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
        @if(Session::has("admin"))
        <div class="row">
            <button class="btn btn-default create-app-interface">Create a new Application Interface</button>
        </div>
        @endif
        @if( count( $appInterfaces) )
        <div class="row">

            <div class="col-md-6">
                <h3>Existing Application Interfaces :</h3>
            </div>
            <div class="col-md-6" style="margin-top:3.5%">
                <input type="text" class="col-md-12 filterinput" placeholder="Search by Interface Name"/>
            </div>
        </div>
        <div class="panel-group" id="accordion">
            @foreach( $appInterfaces as $index => $interface )
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a class="accordion-toggle collapsed interface-name" data-toggle="collapse"
                           data-parent="#accordion" href="#collapse-{{$index}}">
                            {{ $interface->applicationName }}
                        </a>
                        <small>{{ $interface->applicationInterfaceId }}</small>
                        @if(Session::has("admin"))
                        <div class="pull-right col-md-2 interface-options fade">

                            <span class="glyphicon glyphicon-pencil edit-app-interface" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#edit-app-interface-block"
                                  data-interface-id="{{ $interface->applicationInterfaceId }}"></span>

                            <span class="glyphicon glyphicon-file clone-app-interface" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#clone-app-interface-block"
                                  data-interface-id="{{ $interface->applicationInterfaceId }}"></span>

                            <span class="glyphicon glyphicon-file clone-app-interface" style="cursor:pointer; position:relative; top:-2px; left:-10%;"
                                  data-toggle="modal" data-target="#clone-app-interface-block"
                                  data-interface-id="{{ $interface->applicationInterfaceId }}"
                                  data-interface-name="{{ $interface->applicationName }}"></span>

                            <span class="glyphicon glyphicon-trash delete-app-interface" style="cursor:pointer;"
                                  data-toggle="modal" data-target="#delete-app-interface-block"
                                  data-interface-id="{{ $interface->applicationInterfaceId }}"
                                  data-interface-name="{{ $interface->applicationName }}"></span>

                        </div>
                        @endif
                    </h4>
                </div>
                <div id="collapse-{{$index}}" class="panel-collapse collapse">
                    <div class="panel-body">
                        <div class="app-interface-block">
                            @include('partials/interface-block', array( 'interfaceObject' => $interface, 'dataTypes' =>
                            $dataTypes, 'modules' => $modules) )
                        </div>
                    </div>
                </div>
            </div>
            @endforeach
        </div>
        @endif

    </div>

    <div class="app-module-block hide">
        <div class="input-group">
            <select name="applicationModules[]" class="app-module-select form-control">
                @foreach( $modules as $index=> $module)
                <option value="{{ $module->appModuleId}}">{{ $module->appModuleName }}</option>
                @endforeach
            </select>
            <span class="input-group-addon remove-app-module" style="cursor:pointer;">x</span>
        </div>
    </div>

    <div class="app-input-block hide">
        @include('partials/interface-input-block', array( 'dataTypes' => $dataTypes) )
    </div>

    <div class="app-output-block hide">
        @include('partials/interface-output-block', array( 'dataTypes' => $dataTypes) )
    </div>
</div>

<div class="modal fade" id="edit-app-interface-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true" data-backdrop="static">
    <div class="modal-dialog">
        <form action="{{URL::to('/')}}/app/interface-edit" method="POST" id="edit-app-interface-form">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h3 class="text-center">Edit Application Interface</h3>
                </div>
                <div class="modal-body row">
                    <div class="app-interface-form-content col-md-12">
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="button" class="submit-edit-app-interface-form btn btn-primary" value="Update"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                        <input type="submit" class="btn btn-primary hide really-submit-edit-app-interface-form"
                               value=""/>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="modal fade" id="create-app-interface-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true" data-backdrop="static">
    <div class="modal-dialog">
        <form action="{{URL::to('/')}}/app/interface-create" method="POST" id="create-app-interface-form">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h3 class="text-center">Create Application Interface</h3>
                </div>
                <div class="modal-body row">
                    <div class="col-md-12">
                        <div class="create-app-interface-block">
                            @include('partials/interface-block', array( 'dataTypes' => $dataTypes, 'modules' =>
                            $modules) )
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="button" class="btn btn-primary submit-create-app-interface-form" value="Create"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                        <input type="submit" class="btn btn-primary hide really-submit-create-app-interface-form"
                               value=""/>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>


<div class="modal fade" id="clone-app-interface-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true" data-backdrop="static">
    <div class="modal-dialog">
        <form action="{{URL::to('/')}}/app/interface-clone" method="POST" id="clone-app-interface-form">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h3 class="text-center">Clone Application Interface</h3>
                </div>
                <div class="modal-body row">
                    <div class="col-md-12">
                        <div class="clone-app-interface-block">
                            <div class="cloneAppInterfaceInputs">
                                <div class="form-group required">
                                    <label class="control-label interface-name">Application Name</label>
                                    <input type="text" class="form-control" name="applicationName" required placeholder="Cloned Application Interface Name" value=""/>
                                </div>
                                <div class="form-group">
                                    <label class="control-label">Application chosen to be cloned</label>
                                    <input type="text" disabled class="form-control" id="cloneAppName" value=""/>
                                    <input type="hidden" class="form-control" id="cloneAppId" name="applicationInterfaceId" value=""/>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-primary submit-clone-app-interface-form" value="Clone"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="modal fade" id="delete-app-interface-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/app/interface-delete" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Delete Confirmation Application Interface</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control delete-interfaceid" name="appInterfaceId"/>
                    Do you really want to delete the Application Interface - <span class="delete-interface-name"></span>
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
{{ HTML::script('js/interface.js') }}
@stop