@if ($userHasComputeResourcePreference)
<div class="form-group">
    <label class="control-label" for="use-user-cr-pref">
        Use My Compute Resource Account
            <input name="use-user-cr-pref" id="use-user-cr-pref" type="checkbox"
            @if($useUserCRPref) checked @endif>
    </label>
</div>
@endif
<input type="hidden" id="queue-array" value="{{ htmlentities( json_encode( $queues ) ) }}"/>
<input type="hidden" id="app-deployment-defaults-array" value="{{ htmlentities( json_encode( $appDeploymentDefaults ) ) }}"/>
<input type="hidden" id="queue-defaults-array" value="{{ htmlentities( json_encode( $queueDefaults ) ) }}"/>

<!-- Setting the node count we got from previous page to a hidden field for jquery -->
@if(isset($expVal['scheduling']->nodeCount))
<input type="hidden" id="passed-nodeCount" value="{{ $expVal['scheduling']->nodeCount }}"/>
@else
<input type="hidden" id="passed-nodeCount" value="0"/>
@endif

<!-- Setting the cpu count we got from previous page to a hidden field for jquery -->
@if(isset($expVal['scheduling']->totalCPUCount))
<input type="hidden" id="passed-cpuCount" value="{{ $expVal['scheduling']->totalCPUCount }}"/>
@else
<input type="hidden" id="passed-cpuCount" value="0"/>
@endif

<!-- Setting the wall time limit we got from previous page to a hidden field for jquery -->
@if(isset($expVal['scheduling']->wallTimeLimit))
<input type="hidden" id="passed-wallTime" value="{{ $expVal['scheduling']->wallTimeLimit }}"/>
@else
<input type="hidden" id="passed-wallTime" value="0"/>
@endif

<!-- Setting the physical memory we got from previous page to a hidden field for jquery -->
@if(isset($expVal['scheduling']->totalPhysicalMemory))
<input type="hidden" id="passed-physicalmem" value="{{ $expVal['scheduling']->totalPhysicalMemory }}"/>
@else
<input type="hidden" id="passed-physicalmem" value="0"/>
@endif

<div class="form-group required">
    @if( count( $queues) > 0 )
    <label class="control-label" for="node-count">Select a Queue</label>
    <select name="queue-name" class="form-control" id="select-queue" @if(isset($expVal) ) @if(!$expVal['editable']){{
    "disabled" }} @endif @endif required>
    @foreach( $queues as $queue)
    <option value="{{$queue->queueName}}"
    @if(isset($expVal) )
        @if( $expVal['scheduling']->queueName == $queue->queueName )
        selected
        @endif
    @else
        @if(isset($appDeploymentDefaults['queueName']) && $appDeploymentDefaults['queueName'] != null)
            @if($appDeploymentDefaults['queueName'] == $queue->queueName)
                selected
            @endif
        @else
            @if( $queueDefaults['queueName'] == $queue->queueName )
                selected
            @endif
        @endif
    @endif
    >
    {{$queue->queueName}}
    </option>
    @endforeach
    </select>
    @else
    <div class="alert alert-warning">
        This resources has no queues available at the moment. Please contact the administrator.
    </div>
    @endif
</div>

<div class="queue-data">
    <div class="form-group">
        <label for="node-count">Node Count <span>( Max Allowed Nodes - <span
                    class="node-count alert-warning"></span>)</span></label>
        <input type="number" class="form-control" name="node-count" id="node-count" min="1"
               value="@if(isset($expVal) ){{ $expVal['scheduling']->nodeCount }}@else{{$queueDefaults['nodeCount']}}@endif"
               required
        @if(isset($expVal) ) @if(!$expVal['editable']) disabled @endif @endif>
    </div>
    <div class="form-group">
        <label for="cpu-count">Total Core Count <span>( Max Allowed Cores - <span
                    class="cpu-count alert-warning"></span>)</span></label>
        <input type="number" class="form-control" name="cpu-count" id="cpu-count" min="1"
               value="@if(isset($expVal) ){{ $expVal['scheduling']->totalCPUCount }}@else{{$queueDefaults['cpuCount']}}@endif"
               required
        @if(isset($expVal)) @if(!$expVal['editable']) disabled @endif @endif>
    </div>
    <div class="form-group">
        <label for="wall-time">Wall Time Limit <span>( Max Allowed Wall Time - <span
                    class="walltime-count alert-warning"></span>)</span></label>

        <div class="input-group">
            <input type="number" class="form-control" name="wall-time" id="wall-time" min="1"
                   value="@if(isset($expVal)){{$expVal['scheduling']->wallTimeLimit}}@else{{$queueDefaults['wallTimeLimit']}}@endif"
                   required
            @if(isset($expVal)) @if(!$expVal['editable']) disabled @endif @endif>
            <span class="input-group-addon">minutes</span>
        </div>
    </div>
    <div class="form-group">
        <label for="physical-memory">Total Physical Memory <span>( Max Allowed Memory - <span
                    class="memory-count alert-warning"></span>)</span></label>

        <div class="input-group">
            <input type="number" class="form-control" name="total-physical-memory" id="memory-count" min="0"
                   value="@if(isset($expVal) ){{ $expVal['scheduling']->totalPhysicalMemory }}@endif"
            @if(isset($expVal)) @if(!$expVal['editable']) disabled @endif @endif>
            <span class="input-group-addon">MB</span>
        </div>
    </div>
    {{--<div class="form-group">--}}
        {{--<label for="static-working-dir">Static Working Directory<span--}}
                        {{--class="static-working-dir alert-warning"></span></label>--}}
        {{--<input type="text" class="form-control" name="static-working-dir" id="static-working-dir"--}}
               {{--value="@if(isset($expVal) ){{ $expVal['scheduling']->staticWorkingDir }}@endif"--}}
        {{--@if(isset($expVal)) @if(!$expVal['editable']) disabled @endif @endif>--}}
    {{--</div>--}}
</div>

<script>
var experimentQueueBlockInit = function() {
    //To work with experiment edit (Not Ajax)
    $( document ).ready(function() {
        var selectedQueue = $("#select-queue").val();
        getQueueData(selectedQueue);
        $("#select-queue").change(function () {
            var selectedQueue = $("#select-queue").val();
            getQueueData(selectedQueue);
        });
    });

    $("#enable-auto-scheduling").change(function () {
        var selectedQueue = $("#select-queue").val();
        getQueueData(selectedQueue);
    });

    //Setting the file input view JS code
    $( document ).ready(function() {
        function readBlob(opt_startByte, opt_stopByte, fileId) {

            var files = document.getElementById(fileId).files;
            if (!files.length) {
                alert('Please select a file!');
                return;
            }

            var file = files[0];
            var start = 0;
            var stop = Math.min(512*1024,file.size - 1);

            var reader = new FileReader();

            // If we use onloadend, we need to check the readyState.
            reader.onloadend = function(evt) {
                if (evt.target.readyState == FileReader.DONE) { // DONE == 2
                    $('#byte_content').html(evt.target.result.replace(/(?:\r\n|\r|\n)/g, '<br />'));
                    $('#byte_range').html(
                            ['Read bytes: ', start + 1, ' - ', stop + 1,
                                ' of ', file.size, ' byte file'].join(''));
                }
            };

            var blob = file.slice(start, stop + 1);
            reader.readAsBinaryString(blob);

            $('#input-file-view').modal('show');
        }

        $( ".readBytesButtons" ).click(function() {
            var startByte = $(this).data('startbyte');
            var endByte = $(this).data('endbyte');
            var fileId = $(this).data('file-id');
            readBlob(startByte, endByte, fileId);
        });
    });
    
    // To work work with experiment create (Ajax)
    var selectedQueue = $("#select-queue").val();
    getQueueData(selectedQueue);
    $("#select-queue").change(function () {
        var selectedQueue = $(this).val();
        getQueueData(selectedQueue);
    });

    $("#enable-auto-scheduling").change(function () {
        var selectedQueue = $("#select-queue").val();
        getQueueData(selectedQueue);
    });

    function getQueueData(selectedQueue) {
        var queues = $.parseJSON($("#queue-array").val());
        var queueDefaults = $.parseJSON($("#queue-defaults-array").val());
        var appDefaults = $.parseJSON($("#app-deployment-defaults-array").val());
        //getting the html values we set to hidden fields above!
        var passedNodeCount = parseInt($("#passed-nodeCount").val());
        var passedCpuCount = parseInt($('#passed-cpuCount').val());
        var passedWallTime = parseInt($('#passed-wallTime').val());
        var passedPhysicalMemory = parseInt($('#passed-physicalmem').val());
        var veryLargeValue = 9999999;

        console.log(queues);
        $(".queue-view").addClass("hide");
        for (var i = 0; i < queues.length; i++) {
            if (queues[i]['queueName'] == selectedQueue) {
                //node-count
                if (queues[i]['maxNodes'] != 0 && queues[i]['maxNodes'] != null) {
                    if($('#enable-auto-scheduling').prop('checked')){
                        $("#node-count").attr("max", veryLargeValue);
                    }else{
                        $("#node-count").attr("max", queues[i]['maxNodes']);
                    }
                    $(".node-count").html(queues[i]['maxNodes']);
                    $(".node-count").parent().removeClass("hide");
                }
                else
                    $(".node-count").parent().addClass("hide");

                if(appDefaults['nodeCount'] > 0){
                    $("#node-count").val(appDefaults['nodeCount']);
                }else if(queues[i]['defaultNodeCount'] > 0){
                    $("#node-count").val(queues[i]['defaultNodeCount']);
                }else{
                    $("#node-count").val(queueDefaults['nodeCount']);
                }
                // load previously set values on page load.
                if(passedNodeCount!=0){
                    $("#node-count").val(passedNodeCount);
                }

                //core-count
                if (queues[i]['maxProcessors'] != 0 && queues[i]['maxProcessors'] != null) {
                    if($('#enable-auto-scheduling').prop('checked')){
                        $("#cpu-count").attr("max", veryLargeValue);
                    }else {
                        $("#cpu-count").attr("max", queues[i]['maxProcessors']);
                    }
                    $(".cpu-count").html(queues[i]['maxProcessors']);
                    $(".cpu-count").parent().removeClass("hide");
                }
                else
                    $(".cpu-count").parent().addClass("hide");

                if(appDefaults['cpuCount'] > 0){
                    $("#cpu-count").val(appDefaults['cpuCount']);
                }else if(queues[i]['defaultCPUCount'] > 0){
                    $("#cpu-count").val(queues[i]['defaultCPUCount']);
                }else{
                    $("#cpu-count").val(queueDefaults['cpuCount']);
                }

                // load previously set values on page load.
                if(passedCpuCount!=0){
                    $("#cpu-count").val(passedCpuCount);
                }


                //walltime-count
                if (queues[i]['maxRunTime'] != null && queues[i]['maxRunTime'] != 0) {
                    if($('#enable-auto-scheduling').prop('checked')){
                        $("#wall-time").attr("max", veryLargeValue);
                    }else {
                        $("#wall-time").attr("max", queues[i]['maxRunTime']);
                    }
                    $(".walltime-count").html(queues[i]['maxRunTime']);
                    $(".walltime-count").parent().removeClass("hide");
                }
                else
                    $(".walltime-count").parent().addClass("hide");

                if(queues[i]['defaultWalltime'] > 0) {
                    $("#wall-time").val(queues[i]['defaultWalltime']);
                }else{
                    $("#wall-time").val(queueDefaults['wallTimeLimit']);
                }

                // load previously set values on page load.
                if(passedWallTime!=0){
                    $("#wall-time").val(passedWallTime);
                }

                //memory-count
                if (queues[i]['maxMemory'] != 0 && queues[i]['maxMemory'] != null) {
                    if($('#enable-auto-scheduling').prop('checked')){
                        $("#memory-count").attr("max", veryLargeValue);
                    }else {
                        $("#memory-count").attr("max", queues[i]['maxMemory']).val(0);
                    }
                    $(".memory-count").html(queues[i]['maxMemory']);
                    $(".memory-count").parent().removeClass("hide");
                }
                else
                    $(".memory-count").parent().addClass("hide");

                if(queues[i]['cpuPerNode'] > 0){
                    var cpusPerNode = queues[i]['cpuPerNode'];
                }else{
                    var cpusPerNode = queueDefaults['cpusPerNode'];
                }

                // load previously set values on page load.
                if(passedPhysicalMemory!=0){
                    $("#memory-count").val(passedPhysicalMemory);
                }


                var nodeCount=$("#node-count");
                var cpuCount=$("#cpu-count");

                cpuCount.keyup(function(){
                    var cpuCountVal = parseInt(cpuCount.val());
                    if(cpuCountVal > 0){
                        nodeCount.val(Math.ceil(cpuCountVal/cpusPerNode));
                    }
                });
                nodeCount.keyup(function(){
                    var nodeCountVal = parseInt(nodeCount.val());
                    if(nodeCountVal > 0){
                        cpuCount.val(nodeCountVal*cpusPerNode);
                    }
                });

                if(cpusPerNode > 0){
                    cpuCount.on('keyup');
                    nodeCount.on('keyup');
                }else{
                    cpuCount.off('keyup');
                    nodeCount.off('keyup');
                }
            }
        }
        $(".queue-view").removeClass("hide");
    }
}

// On initial load jQuery isn't loaded until later so wait until DOMContentLoaded
if (typeof $ === 'undefined') {
    document.addEventListener("DOMContentLoaded", experimentQueueBlockInit);
} else {
    experimentQueueBlockInit();
}
</script>