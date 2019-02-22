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

<input type="hidden" class="base-url" value="{{URL::to('/')}}"/>

<div class="well">
    <h4>Compute Resource : {{ $computeResource->hostName }}
        @if(Session::has("admin"))
        <div class="pull-right">
            <a href="{{URL::to('/')}}/cr/edit?crId={{Input::get('crId') }}" title="Edit">
                <span class="glyphicon glyphicon-pencil"></span>
            </a>
        </div>
        @endif
    </h4>
</div>
@if( Session::has("message"))
<span class="alert alert-success col-md-12">{{Session::get("message")}}</span>
{{Session::forget("message") }}
@endif

<div class="col-md-12">
    <ul class="nav nav-tabs nav-justified" id="tabs" role="tablist">
        <li class="active"><a href="#tab-desc" data-toggle="tab">Description</a></li>
        <li><a href="#tab-queues" data-toggle="tab">Queues</a></a></li>
        <li><a href="#tab-filesystem" data-toggle="tab">FileSystem</a></li>
        <li><a href="#tab-jobSubmission" data-toggle="tab">Job Submission Interfaces</a></li>
        <li><a href="#tab-dataMovement" data-toggle="tab">Data Movement Interfaces</a></li>
    </ul>
</div>

<div class="tab-content">

<div class="tab-pane active" id="tab-desc">

    <form>
        <input type="hidden" name="crId" value="{{Input::get('crId') }}"/>
        <input type="hidden" name="cr-edit" value="resDesc"/>

        <div class="form-group required">
            <label>Host Name</label>
            <input readonly class="form-control hostName" value="{{ $computeResource->hostName }}"/>
        </div>
        <div class="form-group">
            @if( count( $computeResource->hostAliases) )
            <label>Host Aliases</label>
            @foreach( $computeResource->hostAliases as $hostAlias )
            <input readonly class="form-control" value="{{$hostAlias}}" maxlength="30" name="hostaliases[]"/>
            @endforeach
            @endif
        </div>
        <div class="form-group">
            @if( count( $computeResource->ipAddresses))
            <label class="control-label">IP Addresses</label>
            @foreach( $computeResource->ipAddresses as $ip )
            <input readonly class="form-control" value="{{ $ip }}" maxlength="30" name="ips[]"/>
            @endforeach
            @endif
        </div>
        <div class="form-group">
            <label class="control-label">Resource Description</label>
            <textarea readonly class="form-control" maxlength="255" name="description">{{
                $computeResource->resourceDescription
                }}</textarea>
        </div>
        <div class="form-group">
            <label class="control-label">Maximum Memory Per Node ( In MB )</label>
            <input readonly type="number" min="0" class="form-control" value="{{ $computeResource->maxMemoryPerNode }}"
                   maxlength="30" name="maxMemoryPerNode"/>
        </div>
        {{--<div class="form-group">--}}
            {{--<label class="control-label">CPUs Per Node</label>--}}
            {{--<input type="number" min="0" class="form-control" value="{{ $computeResource->cpusPerNode }}"--}}
                   {{--maxlength="30" name="cpusPerNode"/>--}}
        {{--</div>--}}
        {{--<div class="form-group">--}}
            {{--<label class="control-label">Default Node Count</label>--}}
            {{--<input type="number" min="0" class="form-control" value="{{ $computeResource->defaultNodeCount }}"--}}
                   {{--maxlength="30" name="defaultNodeCount"/>--}}
        {{--</div>--}}
        {{--<div class="form-group">--}}
            {{--<label class="control-label">Default CPU Count</label>--}}
            {{--<input type="number" min="0" class="form-control" value="{{ $computeResource->defaultCPUCount }}"--}}
                   {{--maxlength="30" name="defaultCPUCount"/>--}}
        {{--</div>--}}
        {{--<div class="form-group">--}}
            {{--<label class="control-label">Default Walltime</label>--}}
            {{--<input type="number" min="0" class="form-control" value="{{ $computeResource->defaultWalltime }}"--}}
                   {{--maxlength="30" name="defaultWalltime"/>--}}
        {{--</div>--}}
    </form>

</div>

<div class="tab-pane" id="tab-queues">

    @if( is_array( $computeResource->batchQueues) )
    <h3>Existing Queues :</h3>

    <div class="panel-group" id="accordion">
        @foreach( $computeResource->batchQueues as $index => $queue)
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a class="accordion-toggle collapsed existing-queue-name" data-toggle="collapse"
                       data-parent="#accordion" href="#collapse-{{$index}}">{{ $queue->queueName }}</a>
                </h4>
            </div>
            <div id="collapse-{{$index}}" class="panel-collapse collapse">
                <div class="panel-body">
                    <form>
                        <div class="queue">
                            <div class="form-group required">
                                <label>Queue Name
                                    <small> ( cannot be changed.)</small>
                                </label>
                                <input class="form-control" value="{{ $queue->queueName }}" maxlength="30" name="qname"
                                       placeholder="Queue Name" readonly/>
                            </div>
                            @include('partials/queue-block', array('queueData'=>$queue, 'readOnly'=>true))
                        </div>
                    </form>
                </div>
            </div>
        </div>
        @endforeach
    </div>
    @endif

</div>

<div class="tab-pane" id="tab-filesystem">

    <form role="form">
        <div class="form-group">
            <h3>FileSystem</h3>
            @foreach( $fileSystems as $index => $fileSystem)
            <label>{{ $fileSystem }}</label>
            <input readonly class="form-control" name="fileSystems[{{ $index }}]" placeholder="{{ $fileSystem }}"
                   value="@if( isset( $computeResource->fileSystems[ $index]) ){{ $computeResource->fileSystems[ $index] }} @endif"/>
            @endforeach
            </select>
        </div>
    </form>

</div>

<div class="tab-pane" id="tab-jobSubmission">
    <br/><br/><br/><br/>
    @if( count( $jobSubmissionInterfaces ) )
    <div class="job-edit-info">
        @foreach( $jobSubmissionInterfaces as $index => $JSI )

        <div class="job-protocol-block">
            <form role="form">
                <?php $selectedJspIndex = $computeResource->jobSubmissionInterfaces[$index]->jobSubmissionProtocol; ?>

                <h4>Job Submission Protocol : {{ $jobSubmissionProtocols[ $selectedJspIndex] }}</h4>
                @if( $selectedJspIndex == $jobSubmissionProtocolsObject::LOCAL)
                <div class="select-resource-manager-type">
                    <div class="form-group required">
                        <label>Selected resource manager type</label>
                        <select disabled="true" name="resourceJobManagerType"
                                class="form-control selected-resource-manager"
                                required="required">
                            @foreach( $resourceJobManagerTypes as $index => $rJmT)
                            <option value="{{ $index }}"
                            @if( $JSI->resourceJobManager->resourceJobManagerType == $index ) selected @endif >{{ $rJmT
                            }}</option>
                            @endforeach
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Push Monitoring End Point</label>
                        <input disabled type="text" class="form-control" name="pushMonitoringEndpoint"
                               value="{{ $JSI->resourceJobManager->pushMonitoringEndpoint }}"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">Job Manager Bin Path</label>
                        <input disabled type="text" class="form-control" name="jobManagerBinPath"
                               value="{{ $JSI->resourceJobManager->jobManagerBinPath }}"/>
                    </div>
                    <div class="form-group">
                        <h3>Job Manager Commands</h3>
                        @foreach( $jobManagerCommands as $index => $jmc)
                        <label class="control-label">{{ $jmc }}</label>
                        <input disabled class="form-control" name="jobManagerCommands[{{ $index }}]"
                               placeholder="{{ $jmc }}"
                               value="@if( isset( $JSI->resourceJobManager->jobManagerCommands[$index] ) ) {{ $JSI->resourceJobManager->jobManagerCommands[$index] }} @endif"/>
                        @endforeach
                        </select>
                    </div>
                </div>
                @elseif( $selectedJspIndex == $jobSubmissionProtocolsObject::SSH || $jobSubmissionProtocolsObject::SSH_FORK)
                <div class="form-group required">
                    <label>Selected Security Protocol</label>
                    <select disabled="true" name="securityProtocol" required="required">
                        @foreach( $securityProtocols as $index => $sp)
                        <option value="{{ $index }}"
                        @if( $JSI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                        @endforeach
                    </select>
                </div>

                <div class="form-group">
                    <label class="control-label">Alternate SSH Host Name</label>
                    <input readonly class='form-control' name='alternativeSSHHostName'
                           value="{{ $JSI->alternativeSSHHostName}}"/>
                </div>
                <div class="form-group">
                    <label class="control-label">SSH Port</label>
                    <input readonly class='form-control' name='sshPort' value="{{ $JSI->sshPort }}"/>
                </div>

                <div class="form-group required">
                    <label>Selected Monitoring Mode</label>
                    <select disabled="true" name="monitorMode" required>
                        @foreach( $monitorModes as $index => $mode)
                        <option value="{{ $index }}"
                        @if( $JSI->monitorMode == $index ) selected @endif>{{ $mode}}</option>
                        @endforeach
                    </select>
                </div>

                <div class="form-group">
                    <div class="select-resource-manager-type">
                        <div class="form-group required">
                            <label>Selected resource manager type</label>
                            <select disabled="true" name="resourceJobManagerType"
                                    class="form-control selected-resource-manager"
                                    required="required">
                                @foreach( $resourceJobManagerTypes as $index => $rJmT)
                                <option value="{{ $index }}"
                                @if( $JSI->resourceJobManager->resourceJobManagerType == $index ) selected @endif >{{
                                $rJmT }}</option>
                                @endforeach
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="control-label">Push Monitoring End Point</label>
                            <input disabled type="text" class="form-control" name="pushMonitoringEndpoint"
                                   value="{{ $JSI->resourceJobManager->pushMonitoringEndpoint }}"/>
                        </div>
                        <div class="form-group">
                            <label class="control-label">Job Manager Bin Path</label>
                            <input disabled type="text" class="form-control" name="jobManagerBinPath"
                                   value="{{ $JSI->resourceJobManager->jobManagerBinPath }}"/>
                        </div>
                        <div class="form-group">
                            <h3>Job Manager Commands</h3>
                            @foreach( $jobManagerCommands as $index => $jmc)
                            <label class="control-label">{{ $jmc }}</label>
                            <input disabled class="form-control" name="jobManagerCommands[{{ $index }}]"
                                   placeholder="{{ $jmc }}"
                                   value="@if( isset( $JSI->resourceJobManager->jobManagerCommands[$index] ) ) {{ $JSI->resourceJobManager->jobManagerCommands[$index] }} @endif"/>
                            @endforeach
                        </div>
                    </div>
                </div>

                @elseif( $selectedJspIndex == $jobSubmissionProtocolsObject::UNICORE)
                <div class="form-group required">
                    <label>Selected Security Protocol</label>
                    <select disabled="true" name="securityProtocol" required="required">
                        @foreach( $securityProtocols as $index => $sp)
                        <option value="{{ $index }}"
                        @if( $JSI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                        @endforeach
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Unicore End Point URL</label>
                    <input readonly class='form-control' name='unicoreEndPointURL'
                           value="{{ $JSI->unicoreEndPointURL }}"/>
                </div>
                @endif
            </form>

        </div>
        @endforeach
    </div>
    @endif

    <div class="select-job-protocol hide">
        <form>
            <div class="form-group">
                <label class="control-label">Job Submission Protocol:</label>
                <select disabled="true" name="jobSubmissionProtocol" class="form-control selected-job-protocol"
                        required="required">
                    <option></option>
                    @foreach( $jobSubmissionProtocols as $index => $jobSubmissionProtocol)
                    @if( ! in_array( $index, $addedJSP))
                    <option value="{{ $index }}">{{ $jobSubmissionProtocol }}</option>
                    @endif
                    @endforeach
                </select>
            </div>
        </form>
    </div>

</div>

<div class="tab-pane" id="tab-dataMovement">
    @if( count( $dataMovementInterfaces ) )
    <div class="job-edit-info">
        <br/><br/><br/>
        @foreach( $dataMovementInterfaces as $index => $DMI )
        <div class="data-movement-block">
            <form>
                <?php $selectedDMIIndex = $computeResource->dataMovementInterfaces[$index]->dataMovementProtocol; ?>
                <h4>Data Movement Protocol : {{ $dataMovementProtocols[ $selectedDMIIndex] }}</h4>
                @if( $selectedDMIIndex == $dataMovementProtocolsObject::LOCAL)
                <!-- Nothing here on local UI -->
                @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::SCP)
                <div class="form-group">
                    <label class="control-label">Selected Security Protocol</label>
                    <select disabled="true" name="securityProtocol">
                        @foreach( $securityProtocols as $index => $sp)
                        <option value="{{ $index }}"
                        @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                        @endforeach
                    </select>
                </div>
                <div class="form-group">
                    <label class="control-label">Alternate SSH Host Name</label>
                    <input readonly class='form-control' name='alternativeSSHHostName'
                           value="{{ $DMI->alternativeSCPHostName }}"/>
                </div>
                <div class="form-group">
                    <label class="control-label">SSH Port</label>
                    <input readonly class='form-control' name='sshPort' value="{{ $DMI->sshPort }}"/>
                </div>
                @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::GridFTP)
                <div class="form-group">
                    <label class="control-label">Select Security Protocol</label>
                    <select disabled="true" name="securityProtocol">
                        @foreach( $securityProtocols as $index => $sp)
                        <option value="{{ $index }}"
                        @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                        @endforeach
                    </select>

                    <div>
                        <div class="form-group required">
                            <label class="control-label">Grid FTP End Points</label>
                            @foreach( $DMI->gridFTPEndPoints as $endPoint)
                            <input readonly class="form-control" maxlength="30" name="gridFTPEndPoints[]"
                                   required="required"
                                   value="{{$endPoint}}"/>
                            @endforeach
                        </div>
                    </div>
                </div>
                @elseif( $selectedDMIIndex == $dataMovementProtocolsObject::UNICORE_STORAGE_SERVICE)
                <div class="form-group">
                    <label class="control-label">Select Security Protocol</label>
                    <select disabled="true" name="securityProtocol">
                        @foreach( $securityProtocols as $index => $sp)
                        <option value="{{ $index }}"
                        @if( $DMI->securityProtocol == $index ) selected @endif>{{ $sp }}</option>
                        @endforeach
                    </select>

                    <div>
                        <div class="form-group required">
                            <label class="control-label">Unicore End Point URL</label>
                            <input readonly class="form-control" maxlength="30" name="unicoreEndPointURL"
                                   required="required"
                                   value="{{ $DMI->unicoreEndPointURL }}"/>
                        </div>
                    </div>
                </div>
                @endif
            </form>
        </div>
        @endforeach
    </div>
    @endif
</div>

</div>
</div>
</div>
</div>
@stop