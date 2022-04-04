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
<div class="col-md-offset-2 col-md-8 storage-resource-properties">

<input type="hidden" class="base-url" value="{{URL::to('/')}}"/>

<ol class="breadcrumb">
    <li><a href="{{URL::to('/')}}/sr/browse">Storage Resources</a></li>
    <li class="active">{{ $storageResource->hostName }}</li>
</ol>
@if( Session::has("message"))
<span class="alert alert-success col-md-12">{{Session::get("message")}}</span>
{{Session::forget("message") }}
@endif

<div class="col-md-12">
    <ul class="nav nav-tabs nav-justified" id="tabs" role="tablist">
        <li class="active"><a href="#tab-desc" data-toggle="tab">Description</a></li>
    <!--
        <li><a href="#tab-queues" data-toggle="tab">Queues</a></a></li>
        <li><a href="#tab-filesystem" data-toggle="tab">FileSystem</a></li>
        <li><a href="#tab-jobSubmission" data-toggle="tab">Job Submission Interfaces</a></li>
    -->
        <li><a href="#tab-dataMovement" data-toggle="tab">Data Movement Interfaces</a></li>
    </ul>
</div>

<div class="tab-content">

    <div class="tab-pane active" id="tab-desc">

        <form role="form" method="POST" action="{{ URL::to('/') }}/sr/edit">
            <input type="hidden" name="srId" value="{{Input::get('srId') }}"/>
            <input type="hidden" name="sr-edit" value="resDesc"/>

            <div class="form-group required">
                <label class="control-label">Host Name</label>
                <input class="form-control hostName" value="{{ $storageResource->hostName }}" maxlength="100"
                       name="hostname" required="required"/>
            </div>
            <div class="form-group">
                <label class="control-label">Resource Description</label>
                <textarea class="form-control" maxlength="255" name="description">{{ $storageResource->storageResourceDescription
                    }}</textarea>
            </div>
            <div class="form-group">
                <input type="submit" class="btn btn-primary" name="step1" value="Save changes"/>
            </div>

        </form>

    </div>

    <div class="tab-pane" id="tab-dataMovement">

        <div class="form-group">
            <div class="data-movement-info row hide"></div>
            <button type="button" class="btn btn-sm btn-default add-data-movement">Add a new Data Movement Interface
            </button>
            @if( count( $dataMovementInterfaces ) > 1)
            <button type="button" class="btn btn-sm btn-default update-priority" data-type="dmi" data-toggle="modal"
                    data-target="#update-dmi-priority">Update Priority
            </button>
            @endif
        </div>

        @if( count( $dataMovementInterfaces ) )
        <div class="job-edit-info">
            @foreach( $dataMovementInterfaces as $index => $DMI )
            <div class="data-movement-block">
                <form role="form" method="POST" action="{{ URL::to('/') }}/sr/edit">
                    <input type="hidden" name="srId" class="srId" value="{{Input::get('srId') }}"/>
                    <input type="hidden" name="sr-edit" value="edit-dmi"/>
                    <input type="hidden" name="dmiId" value="{{ $DMI->dataMovementInterfaceId }}"/>

                    <?php $selectedDMIIndex = $storageResource->dataMovementInterfaces[$index]->dataMovementProtocol; ?>

                    <h4>Data Movement Protocol : {{ $dataMovementProtocols[ $selectedDMIIndex] }}
                        <button type='button' class='close delete-dmi' data-toggle="modal" data-target="#confirm-delete-dmi"
                                data-dmi-id="{{ $DMI->dataMovementInterfaceId }}">
                            <span class="glyphicon glyphicon-trash delete-dmi" data-toggle="modal"
                                  data-target="#confirm-delete-dmi"
                                  data-dmi-id="{{ $DMI->dataMovementInterfaceId }}"></span>
                        </button>
                    </h4>
                    <input type="hidden" name="dataMovementProtocol" value="{{ $selectedDMIIndex }}"/>
                    @if( $selectedDMIIndex == $dataMovementProtocolsObject::LOCAL)
                    <!-- Nothing here on local UI -->
                    @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::SCP)
                    <div class="form-group">
                        <label class="control-label">Select Security Protocol</label>
                        <select name="securityProtocol">
                            @foreach( $securityProtocols as $index => $sp)
                            <option value="{{ $index }}"
                            @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                            @endforeach
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Alternate SSH Host Name</label>
                        <input class='form-control' name='alternativeSSHHostName'
                               value="{{ $DMI->alternativeSCPHostName }}"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">SSH Port</label>
                        <input class='form-control' name='sshPort' value="{{ $DMI->sshPort }}"/>
                    </div>
                    <div class="form-group">
                        <button type="submit" class="btn">Update</button>
                    </div>
                    @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::GridFTP)
                    <div class="form-group">
                        <label class="control-label">Select Security Protocol</label>
                        <select name="securityProtocol">
                            @foreach( $securityProtocols as $index => $sp)
                            <option value="{{ $index }}"
                            @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                            @endforeach
                        </select>
                    </div>

                    <div>
                        <div class="form-group required">
                            <label class="control-label">Grid FTP End Points</label>
                            @foreach( $DMI->gridFTPEndPoints as $endPoint)
                            <input class="form-control" maxlength="30" name="gridFTPEndPoints[]" required="required"
                                   value="{{$endPoint}}"/>
                            @endforeach
                            <button type="button" class="btn btn-sm btn-default add-gridFTPEndPoint">Add More Grid FTP
                                End Points
                            </button>
                        </div>
                    </div>
                    <div class="form-group">
                        <button type="submit" class="btn">Update</button>
                    </div>
                    @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::UNICORE_STORAGE_SERVICE)
                    <div class="form-group">
                        <label class="control-label">Select Security Protocol</label>
                        <select name="securityProtocol">
                            @foreach( $securityProtocols as $index => $sp)
                            <option value="{{ $index }}"
                            @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                            @endforeach
                        </select>
                    </div>

                    <div>
                        <div class="form-group required">
                            <label class="control-label">Unicore End Point URL</label>
                            <input class="form-control" maxlength="30" name="unicoreEndPointURL" required="required"
                                   value="{{ $DMI->unicoreEndPointURL }}"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <button type="submit" class="btn">Update</button>
                    </div>
                    @endif
                </form>
            </div>
            @endforeach
        </div>
        @endif
        <div class="select-data-movement hide">

            <form role="form" method="POST" action="{{ URL::to('/') }}/sr/edit">
                <input type="hidden" name="srId" class="srId" value="{{Input::get('srId') }}"/>
                <input type="hidden" name="sr-edit" value="dmp"/>
                <h4>
                    Select the Data Movement Protocol
                </h4>

                <select name="dataMovementProtocol" class="form-control selected-data-movement-protocol">
                    <option></option>
                    @foreach( $dataMovementProtocols as $index => $dmp)
                    //GridFTP and SFTP not supported in Airavata backend. Therefore commenting out from UI
                    @if( ! in_array( $index, $addedDMI) && $dmp!="GridFTP" && $dmp!="SFTP")
                    <option value="{{ $index }}">{{ $dmp }}</option>
                    @endif
                    @endforeach
                </select>

                <div class="form-group">
                    <button type="submit" class="btn btn-primary dmpSubmit hide">Add Data Movement Protocol</button>
                </div>

            </form>

        </div>

    </div>


</div>

</div>
</div>
</div>
</div>





<div class="resource-manager-block hide">
    <div class="select-resource-manager-type">
        <div class="form-group required">
            <label class="control-label">Select resource manager type</label>
            <select name="resourceJobManagerType" class="form-control selected-resource-manager" required="required">
                @foreach( $resourceJobManagerTypes as $index => $rJmT)
                <option value="{{ $index }}">{{ $rJmT }}</option>
                @endforeach
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label">Push Monitoring End Point</label>
        <input type="text" class="form-control" name="pushMonitoringEndpoint"/>
    </div>
    <div class="form-group">
        <label class="control-label">Job Manager Bin Path</label>
        <input type="text" class="form-control" name="jobManagerBinPath"/>
    </div>
    <div class="form-group">
        <h3>Job Manager Commands</h3>
        @foreach( $jobManagerCommands as $index => $jmc)
        <label class="control-label">{{ $jmc }}</label>
        <input class="form-control" name="jobManagerCommands[{{ $index }}]" placeholder="{{ $jmc }}"/>
        @endforeach
        </select>
    </div>
</div>

<div class="ssh-block hide">
    <div class="form-group required">
        <label class="control-label">Select Security Protocol </label>
        <select name="securityProtocol" required>
            @foreach( $securityProtocols as $index => $sp)
            <option value="{{ $index }}">{{ $sp }}</option>
            @endforeach
        </select>
    </div>

<!--    <div class="form-group required">-->
<!--        <label class="control-label">Select Monitoring Mode </label>-->
<!--        <select name="monitorMode" required>-->
<!--            @foreach( $monitorModes as $index => $mode)-->
<!--            <option value="{{ $index }}">{{ $mode}}</option>-->
<!--            @endforeach-->
<!--        </select>-->
<!--    </div>-->

    <div class="form-group addedScpValue hide">
        <label class="control-label">Alternate SSH Host Name</label>
        <input class='form-control' name='alternativeSSHHostName'/>
    </div>
    <div class="form-group addedScpValue hide">
        <label class="control-label">SSH Port</label>
        <input class='form-control' name='sshPort'/>
    </div>
</div>

<div class="cloud-block hide">
    <div class="form-group">
        <label class="control-label">Node Id</label>
        <input class="form-control" name="nodeId" placeholder="nodId"/>
    </div>
    <div class="form-group">
        <label class="control-label">Node Id</label>
        <input class="form-control" name="nodeId" placeholder="nodId"/>
    </div>
    <div class="form-group">
        <label class="control-label">Executable Type</label>
        <input class="form-control" name="nodeId" placeholder="executableType"/>
    </div>
    <div class="form-group">
        <label class="control-label">Select Provider Name</label>
        <select class="form-control">
            <option name="EC2">EC2</option>
            <option name="AWSEC2">AWEC2</option>
            <option name="RACKSPACE">RACKSPACE</option>
        </select>
    </div>
</div>

<div class="dm-gridftp hide">
    <div class="form-group required">
        <label class="control-label">Grid FTP End Points</label>
        <input class="form-control" maxlength="30" name="gridFTPEndPoints[]" required/>
        <button type="button" class="btn btn-sm btn-default add-gridFTPEndPoint">Add More Grid FTP End Points</button>
    </div>
</div>

<!-- modals -->
<div class="modal fade" id="confirm-delete-dmi" tabindex="-1" role="dialog" aria-labelledby="delete-modal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="{{ URL::to('sr/delete-jsi') }}" method="POST">
                <input type="hidden" name="srId" value="{{Input::get('srId') }}"/>
                <input type="hidden" name="dmiId" value="" class="delete-dmi-confirm"/>

                <div class="modal-header">
                    Confirmation
                </div>
                <div class="modal-body">
                    Do you really want to delete this Data Movement Interface ?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-danger danger">Delete</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="add-dmi" tabindex="-1" role="dialog" aria-labelledby="add-modal" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                Add a Data Model Interface
            </div>
            <div class="modal-body add-dmi-body row">

            </div>
        </div>
    </div>
</div>

@if( count( $dataMovementInterfaces ) > 1)
<div class="modal fade" id="update-dmi-priority" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                Update the Priority Order
            </div>
            <div class="modal-body">
                <form action="{{URL::to('/')}}/sr/edit" method="POST" id="dmi-priority-form">
                    <input type="hidden" name="srId" value="{{Input::get('srId') }}"/>
                    <input type="hidden" name="sr-edit" value="dmi-priority"/>
                    @foreach( $storageResource->dataMovementInterfaces as $index => $DMI )
                    <div class="row">
                        <div class="col-md-offset-1 col-md-5">
                            <label>
                                {{ $dataMovementProtocols[ $DMI->dataMovementProtocol] }}
                            </label>
                        </div>
                        <input type="hidden" name="dmi-id[]" maxlength="2" value="{{ $DMI->dataMovementInterfaceId }}"/>

                        <div class="col-md-4">
                            <input type="number" min="0" name="dmi-priority[]" value="{{ $DMI->priorityOrder }}"
                                   required/>
                        </div>
                    </div>
                    @endforeach
                    <button type="submit" class="btn btn-update">Update</button>
                    <div class='priority-updated alert alert-success hide'>
                        The Data Movement Interface Priority has been updated.
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
@endif

@stop

@section('scripts')
@parent
{{ HTML::script('js/script.js') }}
@if(! Session::has('super-admin'))
    <script>
    disableInputs( $(".storage-resource-properties"));
    </script>
@endif

@stop