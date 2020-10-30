<div class="form-group">
    <label class="control-label">Queue Description</label>
    <textarea class="form-control" maxlength="255" name="qdesc" placeholder="Queue Description"
    @if( isset( $readOnly)){{ "readOnly" }}@endif>@if( isset( $queueData)
    ){{ $queueData->queueDescription }}@endif</textarea>
</div>
<div class="form-group">
    <label class="control-label">Queue Max Run Time
        <small> (In Minutes)</small>
    </label>
    <input type="number" min="0" class="form-control"
           value="@if( isset( $queueData) ){{ $queueData->maxRunTime }}@endif" maxlength="30" name="qmaxruntime"
           placeholder="Queue Max Run Time"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Queue Max Nodes</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->maxNodes }}@endif"
           maxlength="30" name="qmaxnodes" placeholder="Queue Max Nodes"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Queue Max Processors</label>
    <input type="number" min="0" class="form-control"
           value="@if( isset( $queueData) ){{ $queueData->maxProcessors }}@endif" maxlength="30" name="qmaxprocessors"
           placeholder="Queue Max Processors"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Max Jobs in Queue</label>
    <input type="number" min="0" class="form-control"
           value="@if( isset( $queueData) ){{ $queueData->maxJobsInQueue }}@endif" maxlength="30" name="qmaxjobsinqueue"
           placeholder="Max Jobs In Queue"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Max Memory For Queue( In MB )</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->maxMemory }}@endif"
           maxlength="30" name="qmaxmemoryinqueue" placeholder="Max Memory For Queue"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">CPUs Per Node</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->cpuPerNode }}@endif"
           maxlength="30" name="cpuPerNode" placeholder="CPUs Per Node"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Default Node Count</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->defaultNodeCount }}@endif"
           maxlength="30" name="defaultNodeCount" placeholder="Default Node Count"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Default CPU Count</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->defaultCPUCount }}@endif"
           maxlength="30" name="defaultCPUCount" placeholder="defaultCPUCount"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Default Wall Time</label>
    <input type="number" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->defaultWalltime }}@endif"
           maxlength="30" name="defaultWalltime" placeholder="Default Wall Time"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Queue Specific Macros</label>
    <input type="text" min="0" class="form-control" value="@if( isset( $queueData) ){{ $queueData->queueSpecificMacros }}@endif"
           maxlength="100" name="queueSpecificMacros" placeholder="Queue Specific Macros"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>
<div class="form-group">
    <label class="control-label">Set as Default Queue for the Resource</label>
    <input type="checkbox" @if( isset( $queueData) && $queueData->isDefaultQueue == true){{ "checked" }}@endif
           name="isDefaultQueue" placeholder="Set as Default Queue for the Resource"
    @if( isset( $readOnly)){{ "readOnly" }}@endif/>
</div>