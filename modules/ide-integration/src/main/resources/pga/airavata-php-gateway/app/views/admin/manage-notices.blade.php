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
            <div class="col-md-12">
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

                <h1 class="text-center">Notices</h1>
                <hr/>
                @if(Session::has("admin"))
                <div class="col-md-12">
                    <button class="btn btn-default create-notice-button">Create a new Notice</button>
                </div>
                <div class="loading-img text-center hide">
                   <img src="../../assets/ajax-loader.gif"/>
                </div>
                @endif
                <h4 class="text-center">Existing Notices</h4>
                <div class="table-responsive">
                <table class="table table-bordered table-condensed" style="word-wrap: break-word;">
                    <tr>
                        <th>Notice</th>
                        <th>Message</th>
                        <th>Publish Date</th>
                        <th>Expiry Date</th>
                        <th>Priority</th>
                        <th>Edit</th>
                        <th>Delete</th>
                    </tr>
                    <tbody class="notices-list">
                    @foreach( $notices as $index => $notice)
                    <tr id="notice-{{$notice->notificationId}}" data-notice-info="{{ htmlspecialchars(json_encode( $notice ), ENT_QUOTES, 'UTF-8') }}">
                        <td class="">
                            {{ $notice->title }}
                        </td>
                        <td>
                            {{ $notice->notificationMessage}}
                        </td>
                        <td @if( $notice->publishedTime != null) class="time" unix-time="{{ $notice->publishedTime/1000 }}" @endif>
                            Not Set
                        </td>
                        <td @if( $notice->expirationTime != null) class="time" unix-time="{{ $notice->expirationTime/1000 }}" @endif>
                            Not Set
                        </td>
                        <td class="priority">
                            {{ $priorities[$notice->priority] }}
                        </td>
                        @if( Session::has("admin"))
                        <td class="update-notice-icon">
                            <span class="glyphicon glyphicon-pencil"></span>
                        </td>
                        @endif
                        @if( Session::has("admin"))
                        <td class="delete-notice-icon">
                            <span class="glyphicon glyphicon-trash"></span>
                        </td>
                        @endif
                    </tr>
                    @endforeach
                    </tbody>
                </table>
                </div>
            </div>
        </div>
    </div>
</div>

<input type="hidden" id="priorities-list" data-priorities="{{ htmlspecialchars(json_encode( $priorities ), ENT_QUOTES, 'UTF-8')  }}"/>
<!-- Create a Notice -->
<div class="modal fade" id="create-notice" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form class="form-horizontal notice-form-values" action="javascript:0" method="POST">
                <div class="modal-header">
                    <h3 class="text-center">Create a new Notice</h3>
                </div>
                <div class="modal-body">
                    
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary submit-add-notice-form" class="btn btn-primary form-control">Create</button>
                </div>
            </form>

        </div>
    </div>
</div>
<div class="modal fade" id="update-notice" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form class="form-horizontal notice-form-values"  action="#" method="POST">
                <input type="hidden" class="notice-notificationId" name="notificationId"/>
                <div class="modal-header">
                    <h3 class="text-center">Update Notice</h3>
                </div>
                <div class="modal-body">
                    
                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary submit-update-notice-form" class="btn btn-primary form-control">Update</button>
                </div>
            </form>
        </div>
    </div>
</div>


<div class="modal fade" id="delete-notice" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/delete-notice" class="delete-notice-values" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Delete Notice Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control notice-notificationId" name="notificationId"/>
                    Do you really want to delete the Notice - <span class="notice-title"></span>
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <button type="submit" class="btn btn-danger delete-notice-submit">Delete</button>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>


    </div>
</div>

<div class="notice-form hide">
    <div class="form-group required">
        <label class="control-label col-md-3">Notice title</label>

        <div class="col-md-6">
            <input type="text" class="form-control notice-title" required name="title"/>
        </div>
    </div>
    <div class="form-group required">
        <label class="control-label col-md-3">Notice Message</label>

        <div class="col-md-6">
            <textarea type="text" class="form-control notice-notificationMessage" required name="notificationMessage" maxlength="4096"></textarea>
        </div>
    </div>
    <div class='form-group required'>
        <label class="col-md-3 control-label">Publish Date</label>
        <div class="col-md-6">
            <div class='input-group date datetimepicker9'>
                <input type='text' class="form-control notice-publishedTime" required placeholder="From Date"/>
                <span class="input-group-addon">
                    <span class="glyphicon glyphicon-calendar"></span>
                </span>
            </div>
        </div>
    </div>
     <div class='form-group'>
        <label class="col-md-3 control-label">Expiration Date</label>
        <div class="col-md-6">
            <div class='input-group date datetimepicker10'>
                <input type='text' class="form-control notice-expirationTime" placeholder="To Date"/>
                <span class="input-group-addon">
                    <span class="glyphicon glyphicon-calendar"></span>
                </span>
            </div>
        </div>
    </div>

    <div class="form-group required">
        <label class="col-md-3 control-label">Priority</label>
        <div class="col-md-6">
            <select class="form-control notice-priority" name="priority">
                @foreach( $priorities as $index => $priority)
                    <option value="{{ $index }}">{{ $priority}}</option>
                @endforeach
            </select>
        </div>
    </div>
</div>

@stop

@section('scripts')
@parent

{{ HTML::script('js/moment.js')}}
{{ HTML::script('js/datetimepicker-3.1.3.js')}}

<script>
        $(".create-notice-button").click( function(){
            $("#create-notice .modal-body").html( $(".notice-form").html());
            setDateProperties("#create-notice");
            $("#create-notice").modal( "show");
        });

        $(".notices-list").on("click", ".update-notice-icon", function(){
            var noticeData = $(this).parent().data("notice-info");
            if( typeof noticeData != "object")
                noticeData = $.parseJSON( noticeData);
            $("#update-notice .modal-body").html( $(".notice-form").html());

            $("#update-notice").modal( "show");

            for( var key in noticeData){
                var formInput = $("#update-notice .notice-" + key);
                formInput.val( noticeData[key]);
            }
            var publishedTimeElem = $("#update-notice .notice-publishedTime");
            if( publishedTimeElem.val() != "")
                publishedTimeElem.val( moment( parseInt( publishedTimeElem.val())).format('MM/DD/YYYY hh:mm a') );

            var expirationTimeElem = $("#update-notice .notice-expirationTime");
            if( expirationTimeElem.val() != "")
                expirationTimeElem.val( moment( parseInt( expirationTimeElem.val())).format('MM/DD/YYYY hh:mm a') );

            setDateProperties("#update-notice");
        });

        //Add notice submit
        $("body").on("submit", "#create-notice .notice-form-values", function(ev){
            ev.preventDefault();

            if( $(this)[0].checkValidity() ){
                $(".submit-add-notice-form").html("<img src='{{URL::to('/')}}/assets/ajax-loader.gif'/>");
                var formData = $("#create-notice .notice-form-values").serialize();
                formData += "&publishedTime="+ moment( $("#create-notice .notice-publishedTime").val() ).utc().format('MM/DD/YYYY hh:mm a');
                formData += "&expirationTime="+ moment( $("#create-notice .notice-expirationTime").val() ).utc().format('MM/DD/YYYY hh:mm a');
                $.ajax({
                    url: '{{URL::to('/')}}/add-notice',
                    type: "post",
                    data: formData,
                    success: function( data){
                        var addedNotice = $.parseJSON( data);
                        $(".notices-list").prepend(
                            "<tr id='notice-'" + addedNotice.notificationId + "' class='alert alert-success' data-notice-info='" + data + "'>" + updateRow( addedNotice) + "</tr>"
                        );
                        $("#create-notice").modal("hide");  
                    },
                    error: function(){
                        $(".submit-add-notice-form").after("<span alert alert-danger'>An error has occurred. Please try again later.</span>");
                    }
                }).complete( function(){
                        $(".submit-add-notice-form").html("Create");
                });
            }
        });

        //Update Notice Submit
        $("body").on("submit", "#update-notice .notice-form-values", function(ev){
            ev.preventDefault();
            if( $(this)[0].checkValidity() ){
                $(".submit-update-notice-form").html("<img src='{{URL::to('/')}}/assets/ajax-loader.gif'/>");
                var formData = $("#update-notice .notice-form-values").serialize();
                var publishedTime = $("#update-notice .notice-publishedTime").val();
                if( publishedTime != "")
                    formData += "&publishedTime="+  moment( publishedTime ).utc().format('MM/DD/YYYY hh:mm a');
                else
                    formData += "&publishedTime=";
                var expirationTime = $("#update-notice .notice-expirationTime").val();
                if( expirationTime != "")
                    formData += "&expirationTime="+ moment( expirationTime ).utc().format('MM/DD/YYYY hh:mm a');
                else
                    formData += "&expirationTime=";

                $.ajax({
                    url: '{{URL::to('/')}}/update-notice',
                    type: "post",
                    data: formData,
                    success: function( data){
                        var addedNotice = $.parseJSON( data);
                        elemToUpdate = $("#notice-" + $("#update-notice .notice-notificationId").val() );
                        elemToUpdate.html(updateRow( addedNotice));
                        elemToUpdate.addClass("alert").addClass("alert-success").data("notice-info", data);
                        $("#update-notice").modal("hide");  

                    },
                    error: function(){
                        $(".submit-update-notice-form").after("<span alert alert-danger'>An error has occurred. Please try again later.</span>");
                    }
                }).complete( function(){
                        $(".submit-update-notice-form").html("Update");
                });
            }
            else
                $('#update-notice .notice-form-values').submit();
        });

        $(".notices-list").on("click", ".delete-notice-icon", function(){
            var notice = $(this).parent().data("notice-info");
            $("#delete-notice .notice-notificationId").val( notice.notificationId);
            $("#delete-notice .notice-title").html( notice.title);
            $("#delete-notice").modal("show");
        });

        //Update Notice Submit
        $(".delete-notice-submit").click( function(ev){
            ev.preventDefault();
            formData = $(".delete-notice-values").serialize();

            $(this).html("<img src='{{URL::to('/')}}/assets/ajax-loader.gif'/>");

            $.ajax({
                url: '{{URL::to('/')}}/delete-notice',
                type: "post",
                data: formData,
                success: function( data){
                    if( data == 1){
                        $("#notice-" +$("#delete-notice .notice-notificationId").val()).fadeOut().remove();
                        $("#delete-notice").modal("hide");  

                    }
                    else{
                        $(".delete-notice-submit").after("<span alert alert-danger'>An error has occurred. Please try again later.</span>");
                    }
                },
                error: function(){
                    $(".submit-update-notice-form").after("<span alert alert-danger'>An error has occurred. Please try again later.</span>");
                }
            }).complete( function(){
                    $(".delete-notice-submit").html("Delete");
            });
        });

        function setDateProperties( parent){
            $( parent + " .datetimepicker9").datetimepicker({
                pick12HourFormat: false
            });
            $( parent + " .datetimepicker10").datetimepicker({
                pick12HourFormat: false
            });
            $( parent + " .datetimepicker9").on("dp.change", function (e) {
                $( parent + " .datetimepicker10").data("DateTimePicker").setMinDate(e.date);

                //hack to close calendar on selecting date
                //$(this).find(".glyphicon-calendar").click();
            });
            $( parent + " .datetimepicker10").on("dp.change", function (e) {
                $( parent + " .datetimepicker9").data("DateTimePicker").setMaxDate(e.date);

                //hack to close calendar on selecting date
                //$(this).find(".glyphicon-calendar").click();
            });
        }

        function updateRow( noticeObject){
            var prioritiesList = $("#priorities-list").data("priorities");
            var row =   "<td>" + noticeObject.title + "</td>" +
                        "<td>" + noticeObject.notificationMessage + "</td>" +
                        "<td class='date'>" + convertTimestamp( noticeObject.publishedTime) + "</td>" +
                        "<td class='date'>" + convertTimestamp( noticeObject.expirationTime ) + "</td>" +
                        "<td>" +  prioritiesList[noticeObject.priority] + "</td>"+
                        "<td class='update-notice-icon'><span class='glyphicon glyphicon-pencil'></span></td>"+
                        "<td class='delete-notice-icon'><span class='glyphicon glyphicon-trash'></span></td>";
            return row;
        }
</script>
@stop