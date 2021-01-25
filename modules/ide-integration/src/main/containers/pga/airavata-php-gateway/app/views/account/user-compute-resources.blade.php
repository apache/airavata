@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/datetimepicker.css')}}
{{ HTML::style('css/user-settings.css')}}
<style>
button.add-user-cr {
    margin-top: 10px;
    margin-bottom: 10px;
}
#user-cr-select-input-group {
    margin-bottom: 10px;
}
</style>
@stop

@section('content')
@foreach( (array)$computeResources as $index => $cr)
@include('partials/user-compute-resource-preferences',
    array('computeResource' => $cr, 'credentialSummaries' => $credentialSummaries,
        'defaultCredentialSummary' => $defaultCredentialSummary))
@endforeach
<div class="container">
    <ol class="breadcrumb">
        <li><a href="{{ URL::to('/') }}/account/settings">User Settings</a></li>
        <li class="active">Compute Resources</li>
    </ol>
    @if( Session::has("message"))
        <div class="alert alert-success alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert"><span
                    aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            {{ Session::get("message") }}
        </div>
    {{ Session::forget("message") }}
    @endif
    <button class="btn btn-default add-user-cr">
        <span class="glyphicon glyphicon-plus"></span> Add a Compute Resource Account
    </button>
    <div id="add-user-compute-resource-block-container">
    </div>
    <div class="panel-group" id="accordion">
        @foreach( (array)$userResourceProfile->userComputeResourcePreferences as $indexUserCRP => $user_crp )
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a class="accordion-toggle collapsed"
                       data-toggle="collapse" data-parent="#accordion"
                       href="#collapse-user-crp-{{$indexUserCRP}}">
                        {{$user_crp->crDetails->hostName}}
                    </a>
                </h4>
            </div>
            <div id="collapse-user-crp-{{$indexUserCRP}}"
                 class="panel-collapse collapse">
                <div class="panel-body">
                    <form class="set-cr-preference" action="{{URL::to('/')}}/account/update-user-crp"
                          method="POST">
                        <input type="hidden" name="gatewayId" id="gatewayId"
                               value="{{$userResourceProfile->gatewayID}}">
                        <input type="hidden" name="computeResourceId"
                               id="computeResourceId"
                               value="{{$user_crp->computeResourceId}}">

                        <div class="form-horizontal">
                            @include('partials/user-compute-resource-preferences',
                            array('computeResource' => $user_crp->crDetails,
                            'preferences'=>$user_crp, 'show'=>true,
                            'allowDelete'=>true, 'credentialSummaries' => $credentialSummaries,
                            'defaultCredentialSummary' => $defaultCredentialSummary))
                        </div>
                    </form>
                </div>
            </div>
        </div>
        @endforeach
    </div>
</div>
<div class="add-user-compute-resource-block hide">
    <div class="well">
        <form action="{{URL::to('/')}}/account/add-user-crp" method="POST">
            <input type="hidden" name="gatewayId" id="gatewayId" value="{{$userResourceProfile->gatewayID}}">

            <div id="user-cr-select-input-group" class="input-group">
                <select id="user-cr-select" name="computeResourceId" class="form-control">
                    <option value="">Select a Compute Resource and configure your account</option>
                    @foreach( (array)$unselectedCRs as $index => $cr)
                    <option value="{{ $cr->computeResourceId}}">{{ $cr->hostName }}</option>
                    @endforeach
                </select>
                <span class="input-group-addon remove-user-cr" style="cursor:pointer;">x</span>
            </div>
            <div class="user-cr-pref-space form-horizontal"></div>
        </form>
    </div>
</div>

<div class="modal fade" id="remove-user-compute-resource-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/account/delete-user-crp" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Remove Compute Resource Account Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control remove-user-crId" name="rem-user-crId"/>

                    Do you really want to remove your Compute Resource Account settings for <span class="remove-user-cr-name"> </span>?
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-danger" value="Remove"/>
                        <input type="button" class="btn btn-default" data-dismiss="modal" value="Cancel"/>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>
@stop

@section('scripts')
@parent
{{ HTML::script('js/moment.js')}}
{{ HTML::script('js/datetimepicker-3.1.3.js')}}
<script>

$('.add-user-cr').on('click', function(){

    $('#add-user-compute-resource-block-container').append( $(".add-user-compute-resource-block").html() );
});
$(".remove-user-compute-resource").click( function(){
	$(".remove-user-cr-name").html( $(this).data("cr-name") );
	$(".remove-user-crId").val( $(this).data("cr-id") );
});
$("#add-user-compute-resource-block-container").on("change", "#user-cr-select", function(){
    crId = $(this).val();
    //This is done as Jquery creates problems when using period(.) in id or class.
    crId = crId.replace(/\./g,"_");
    $("#add-user-compute-resource-block-container .user-cr-pref-space").html($("#cr-" + crId).html());
});
$("#add-user-compute-resource-block-container").on("click", ".remove-user-cr", function(){
    $("#add-user-compute-resource-block-container").empty();
});

/* making datetimepicker work for reservation start and end date kept in user-compute-resource-preferences blade*/
$('.datetimepicker1').datetimepicker({
    pick12HourFormat: false
    //pickTime: false
});
$('.datetimepicker2').datetimepicker({
    pick12HourFormat: false
    //pickTime: false
});

$(".datetimepicker1 input").focus( function(){
    $(this).parent().find(".glyphicon-calendar").click();
});
$(".datetimepicker2 input").focus( function(){
    $(this).parent().find(".glyphicon-calendar").click();
});
</script>
@stop