@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/sharing.css') }}
@stop

@section('content')

<?php
//$echoResources = array('localhost', 'trestles.sdsc.edu', 'lonestar.tacc.utexas.edu');
//$wrfResources = array('trestles.sdsc.edu');

//$appResources = array('Echo' => $echoResources, 'WRF' => $wrfResources);
?>

<div class="container">

    @if (Session::has("error-message"))
        <div class="alert alert-danger">
            {{{ Session::get("error-message") }}}
        </div>
    @endif

    <div class="col-md-offset-3 col-md-6">
        <h1>Edit Experiment</h1>

        <form action="{{URL::to('/')}}/experiment/edit" method="POST" role="form" enctype="multipart/form-data">
            <input type="hidden" name="expId" value="{{{ Input::get('expId') }}}"/>

            @include('partials/experiment-inputs', array( "expInputs" => $expInputs, "cpusPerNode"=>$cpusPerNode))

            @if( count( $expInputs['computeResources']) > 0)
            <div class="btn-toolbar">
                <div class="btn-group">
                    <input name="save" type="submit" class="btn btn-primary"
                           value="Save" <?php if (!$expInputs['expVal']['editable']) echo 'disabled' ?>>
                    <input name="launch" type="submit" class="btn btn-success"
                           value="Save and launch" <?php if (!$expInputs['expVal']['editable']) echo 'disabled' ?>>
                </div>
            </div>
            @else
            <p class="well alert alert-danger">
                This experiment is connected with an Application which is currently not deployed on any Resource. The experiment cannot be launched at the moment.
            </p>
            @endif
            <input type="hidden" id="allowedFileSize" value="{{ $expInputs['allowedFileSize'] }}"/>
        </form>
    </div>

</div>

{{ HTML::image("assets/Profile_avatar_placeholder_large.png", 'placeholder image', array('class' => 'baseimage')) }}

@include('partials/sharing-form-modal', array("entityName" => "experiment"))
@stop


@section('scripts')
@parent
<script>
    var users = {{ $users }};
    var owner = {{ $owner }};
    var projectOwner = {{ $projectOwner }};
    $('#entity-share').data({url: "{{URL::to('/')}}/experiment/unshared-users", resourceId: {{json_encode(Input::get('expId'))}} })
</script>
{{ HTML::script('js/sharing/sharing_utils.js') }}
{{ HTML::script('js/sharing/share.js') }}
{{ HTML::script('js/util.js') }}
<script>
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
        $(".queue-view ").addClass("hide");
        $.ajax({
            url: '../experiment/getQueueView',
            type: 'get',
            data: {crId: crId},
            success: function (data) {
                $(".queue-view").html(data);
                $(".loading-img ").addClass("hide");
                $(".queue-view ").removeClass("hide");
            }
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
