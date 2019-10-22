<!-- Application Deployments do not have a name. :(
<div class="form-group" required>
	<label class="control-label">Application Deployment Name</label>
	<input type="text" class="form-control" name="applicationName" value="Class not saving it anywhere." readonly/>
</div>
-->
@if( isset( $deploymentObject) )
<input type="hidden" name="app-deployment-id" value="{{$deploymentObject->appDeploymentId}}"/>
<input type="hidden" name="app-deployment-object" id="app-deployment-object" value="{{ htmlentities( json_encode( $deploymentObject ) ) }}"/>
@endif
<div class="form-group required">
    <label class="control-label">Application Module</label>
    <select name="appModuleId" class="form-control app-module-filter" required readonly>
        @foreach( $modules as $index => $module)
        <option value="{{ $module->appModuleId }}"
        @if( isset( $deploymentObject) ) @if( $module->appModuleId == $deploymentObject->appModuleId) selected @endif
        @endif>{{ $module->appModuleName }}</option>
        @endforeach
    </select>
</div>
<div class="form-group required">
    <label class="control-label">Application Compute Host</label>
    <select name="computeHostId" class="form-control computeHostId" required readonly>
        @foreach( $computeResourcePreferences as $computeResourcePreference)
        <option value="{{{ $computeResourcePreference->computeResourceId }}}"
        @if( isset($deploymentObject) && $computeResourcePreference->computeResourceId == $deploymentObject->computeHostId) selected @endif
        >
        {{{ $computeResourcePreference->crName }}}
        </option>
        @endforeach
    </select>
</div>
<div class="form-group required">
    <label class="control-label">Application Executable Path</label>
    <input type="text" class="form-control" name="executablePath"
           value="@if( isset( $deploymentObject)){{$deploymentObject->executablePath}}@endif" required readonly/>
</div>
<div class="form-group required">
    <label class="control-label">Application Parallelism Type</label>
    <select name="parallelism" class="form-control" readonly>
        @foreach( $applicationParallelismTypes as $index=>$parallelismType)
        <option value="{{$parallelismType}}"
        @if( isset( $deploymentObject) ) @if( $index == $deploymentObject->parallelism) selected @endif @endif>{{
        $parallelismType }}</option>
        @endforeach
    </select>
</div>
<div class="form-group">
    <label class="control-label">Application Deployment Description</label>
    <textarea class="form-control" name="appDeploymentDescription" readonly>@if( isset(
        $deploymentObject)){{$deploymentObject->appDeploymentDescription}}@endif</textarea>
</div>
<hr/>
<div class="form-group">
    <label class="control-label">Module Load Commands</label>
    <div class="show-load-cmds">
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->moduleLoadCmds as $index => $cmd)
        <input name="moduleLoadCmds[]" type="text" class="form-control" placeholder="Module Load Command"
               value="{{$cmd->command}}" readonly/>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-load-cmd hide">Add Module Load Commands</button>
</div>
<hr/>
<div class="form-group">
    <div class="show-lib-prepend-paths">
        <h5>Library Prepend Paths</h5>
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->libPrependPaths as $path)
        <div class="col-md-12 well">
            <input name="libraryPrependPathName[]" type="text" class="col-md-4" placeholder="Name"
                   value="{{$path->name}}" readonly/>
            <input name="libraryPrependPathValue[]" type="text" class="col-md-8" placeholder="Value"
                   value="{{$path->value}}" readonly/>
        </div>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-lib-prepend-path hide">Add a Library Prepend Path
    </button>
</div>
<hr/>
<div class="form-group">
    <div class="show-lib-append-paths">
        <h5>Library Append Paths</h5>
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->libAppendPaths as $path)
        <div class="col-md-12 well">
            <input name="libraryAppendPathName[]" type="text" class="col-md-4" placeholder="Name"
                   value="{{$path->name}}" readonly/>
            <input name="libraryAppendPathValue[]" type="text" class="col-md-8" placeholder="Value"
                   value="{{$path->value}}" readonly/>
        </div>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-lib-append-path hide">Add a Library Append Path
    </button>
</div>
<hr/>
<div class="form-group">
    <div class="show-environments">
        <h5>Environments</h5>
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->setEnvironment as $path)
        <div class="col-md-12 well">
            <input name="environmentName[]" type="text" class="col-md-4" placeholder="Name" value="{{$path->name}}"
                   readonly/>
            <input name="environmentValue[]" type="text" class="col-md-8" placeholder="Value" value="{{$path->value}}"
                   readonly/>
        </div>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-environment hide">Add Environment</button>
</div>

<div class="form-group">
    <div class="show-preJobCommands">
        <h5>Pre Job Commands</h5>
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->preJobCommands as $preJobCommand)
        <div class="col-md-12 well">
            <input name="preJobCommand[]" type="text" class="col-md-12" placeholder="Pre Job Command"
                   value="{{ htmlentities( $preJobCommand->command)}}" readonly/>
        </div>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-preJobCommand hide">Add Pre Job Command</button>
</div>

<div class="form-group">
    <div class="show-postJobCommands">
        <h5>Post Job Commands</h5>
        @if( isset( $deploymentObject))
        @foreach( (array)$deploymentObject->postJobCommands as $postJobCommand)
        <div class="col-md-12 well">
            <input name="postJobCommand[]" type="text" class="col-md-12" placeholder="Post Job Command"
                   value="{{htmlentities( $postJobCommand->command )}}" readonly/>
        </div>
        @endforeach
        @endif
    </div>
    <button type="button" class="btn btn-default control-label add-postJobCommand hide">Add Post Job Command</button>
</div>
<div class="form-group">
    <label class="control-label">Default Node Count</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $deploymentObject)){{$deploymentObject->defaultNodeCount}}@endif"
    required readonly maxlength="30" name="defaultNodeCount"/>
</div>
<div class="form-group">
    <label class="control-label">Default CPU Count</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $deploymentObject)){{$deploymentObject->defaultCPUCount}}@endif"
    required readonly maxlength="30" name="defaultCPUCount"/>
</div>
{{--<div class="form-group">--}}
    {{--<label class="control-label">Default Walltime</label>--}}
    {{--<input type="number" min="0" class="form-control" value="@if( isset( $deploymentObject)){{$deploymentObject->defaultWalltime}}@endif"--}}
    {{--required readonly maxlength="30" name="defaultWalltime"/>--}}
{{--</div>--}}
<div class="form-group required">
    <label class="control-label">Default Queue Name</label>
    <select name="defaultQueueName" class="form-control default-queue-name-select" readonly>
        @if(isset($deploymentObject))
            @foreach($computeResourceFullObjects as $computeResourceFullObject)
                @if(0 === strpos($computeResourceFullObject->computeResourceId, $deploymentObject->computeHostId))
                    <?php $queues = $computeResourceFullObject->batchQueues;?>
                    @foreach( $queues as $queue)
                        <option value="{{ $queue->queueName }}"
                                @if( isset( $deploymentObject) ) @if( $queue->queueName == $deploymentObject->defaultQueueName) selected @endif
                                @endif>{{ $queue->queueName }}</option>
                    @endforeach
                @endif
            @endforeach
        @endif
    </select>
</div>