
<div class="select-resource-manager-type">
    <div class="form-group required">
        <label class="control-label">Select resource manager type</label>
        <select name="resourceJobManagerType" class="form-control selected-resource-manager"
                required="required">
            @foreach( $resourceJobManagerTypes as $index => $rJmT)
            <option value="{{ $index }}"
                @if( isset( $JSInterface) )
                    @if( $JSInterface->resourceJobManager->resourceJobManagerType == $index ) selected @endif
                @endif >
                {{ $rJmT }}</option>
            @endforeach
        </select>
    </div>
    <div class="form-group">
        <label class="control-label">Push Monitoring End Point</label>
        <input type="text" class="form-control" name="pushMonitoringEndpoint"
            @if( isset( $JSInterface) )
               value="{{ $JSInterface->resourceJobManager->pushMonitoringEndpoint }}"
            @endif />
    </div>
    <div class="form-group">
        <label class="control-label">Job Manager Bin Path</label>
        <input type="text" class="form-control" name="jobManagerBinPath"
            @if( isset( $JSInterface) )
               value="{{ $JSInterface->resourceJobManager->jobManagerBinPath }}"
            @endif />
    </div>
    <div class="form-group">
        <h3>Job Manager Commands</h3>
        @foreach( $jobManagerCommands as $index => $jmc)
        <label class="control-label">{{ $jmc }}</label>
        <input class="form-control" name="jobManagerCommands[{{ $index }}]" placeholder="{{ $jmc }}"
               value="@if( isset( $JSInterface->resourceJobManager->jobManagerCommands[$index] ) ){{$JSInterface->resourceJobManager->jobManagerCommands[$index]}}@endif"/>
        @endforeach
    </div>
    <div class="form-group">
        <h3>Parallelism Prefixes</h3>
        @foreach( $parallelismTypes as $index => $pt)
        <label class="control-label">{{ $pt }}</label>
        <input class="form-control" name="parallelismPrefix[{{ $index }}]" placeholder="{{ $pt }}"
               value="@if( isset( $JSInterface->resourceJobManager->parallelismPrefix[$index] ) ){{$JSInterface->resourceJobManager->parallelismPrefix[$index]}}@endif"/>
        @endforeach
    </div>
</div>