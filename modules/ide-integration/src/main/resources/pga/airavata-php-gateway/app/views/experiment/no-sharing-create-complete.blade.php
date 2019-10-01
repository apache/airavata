@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/sharing.css') }}
@stop

@section('content')
<div class="col-md-offset-3 col-md-6">

    <h1>Create a new experiment</h1>

    <form action="{{URL::to('/')}}/experiment/create" method="POST" role="form" enctype="multipart/form-data">

        <input type="hidden" name="experiment-name" value="{{{$expInputs['experimentName']}}}">
        <input type="hidden" name="experiment-description" value="{{{$expInputs['experimentDescription']}}}">
        <input type="hidden" name="project" value="{{{$expInputs['project']}}}">
        <input type="hidden" name="application" value="{{{$expInputs['application']}}}">

        @include('partials/experiment-inputs', array("expInputs" => $expInputs, "queueDefaults" =>
        $expInputs['queueDefaults']) )

        <div class="form-group btn-toolbar">
            <div class="btn-group">
                <button onclick="disableWarn()" name="save" type="submit" class="btn btn-primary" value="Save">Save</button>
                <button onclick="disableWarn()" name="launch" type="submit" class="btn btn-success" id="expLaunch" value="Save and launch">Save
                    and launch
                </button>
            </div>

            <a onclick="disableWarn()" href="{{URL::to('/')}}/experiment/create" class="btn btn-default" role="button">Start over</a>
        </div>

    </form>

<input type="hidden" id="allowedFileSize" value="{{ $expInputs['allowedFileSize'] }}"/>
</div>


@stop

@section('scripts')
@parent
{{ HTML::script('js/util.js') }}
<script>
    var warn = true;

    function disableWarn(){
        warn = false;
        return false;
    }

    $('.file-input').bind('change', function () {

        var allowedFileSize = $("#allowedFileSize").val();
        var tooLargeFilenames = util.validateMaxUploadFileSize(this.files, allowedFileSize);

        if (tooLargeFilenames.length > 0) {
            var singleOrMultiple = tooLargeFilenames.length === 1 ? " the file [" : " each of the files [";
            alert("The size of " + singleOrMultiple + tooLargeFilenames.join(", ") + "] is greater than the allowed file size (" + allowedFileSize + " MB) in a form. Please upload another file.");
            $(this).val("");
        }
    });

    $("#enableEmail").change(function () {
        if (this.checked) {
            $("#emailAddresses").attr("required", "required");
            $(this).parent().children(".emailSection").removeClass("hide");
        }
        else {
            $(this).parent().children(".emailSection").addClass("hide");
            $("#emailAddresses").removeAttr("required");
        }

    });

    $(".addEmail").click(function () {
        var emailInput = $(this).parent().find("#emailAddresses").clone();
        emailInput.removeAttr("id").removeAttr("required").val("").appendTo(".emailAddresses");
    });

    $("#compute-resource").change(function () {
        var crId = $(this).val();
        $(".loading-img ").removeClass("hide");
        $.ajax({
            url: '../experiment/getQueueView',
            type: 'get',
            data: {crId: crId},
            success: function (data) {
                $(".queue-view").html(data);
                $(".loading-img ").addClass("hide");
            }
        });
    });

    window.onbeforeunload = function() {
        if(warn){
            return "Are you sure you want to navigate to other page ? (you will loose all unsaved data)";
        }
        warn = true;
    }

    //Selecting the first option as the default
    $( document ).ready(function() {
        var $cr = $("#compute-resource");
        var crId = $cr.val();
        if ($cr.children("option").size() === 1 && crId !== "") {
            $(".loading-img ").removeClass("hide");
            $.ajax({
                url: '../experiment/getQueueView',
                type: 'get',
                data: {crId: crId},
                success: function (data) {
                    $(".queue-view").html(data);
                    $(".loading-img ").addClass("hide");
                },error : function(data){
                    $(".loading-img ").addClass("hide");
                }
            });
        }
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

    updateList = function() {
        var input = document.getElementById('optInputFiles');
        var output = document.getElementById('optFileList');

        output.innerHTML = '<ul>';
        for (var i = 0; i < input.files.length; ++i) {
            output.innerHTML += '<li>' + input.files.item(i).name + '</li>';
        }
        output.innerHTML += '</ul>';
    }
</script>
@stop
