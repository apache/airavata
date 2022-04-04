@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/user-settings.css')}}
<style>
button.add-user-sr {
    margin-top: 10px;
    margin-bottom: 10px;
}
#user-sr-select-input-group {
    margin-bottom: 10px;
}
</style>
@stop

@section('content')
@foreach( (array)$storageResources as $index => $sr)
@include('partials/user-storage-resource-preferences',
    array('storageResource' => $sr, 'credentialSummaries' => $credentialSummaries,
        'defaultCredentialSummary' => $defaultCredentialSummary))
@endforeach
<div class="container">
    <ol class="breadcrumb">
        <li><a href="{{ URL::to('/') }}/account/settings">User Settings</a></li>
        <li class="active">Storage Resources</li>
    </ol>
    @if( Session::has("message"))
        <div class="alert alert-success alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert"><span
                    aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            {{ Session::get("message") }}
        </div>
    {{ Session::forget("message") }}
    @endif
    <button class="btn btn-default add-user-sr">
        <span class="glyphicon glyphicon-plus"></span> Add a Storage Resource Account
    </button>
    <div id="add-user-storage-resource-block-container">
    </div>
    <div class="panel-group" id="accordion">
        @foreach( (array)$userResourceProfile->userStoragePreferences as $indexUserSRP => $user_srp )
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a class="accordion-toggle collapsed"
                       data-toggle="collapse" data-parent="#accordion"
                       href="#collapse-user-srp-{{$indexUserSRP}}">
                        {{$user_srp->srDetails->hostName}}
                    </a>
                </h4>
            </div>
            <div id="collapse-user-srp-{{$indexUserSRP}}"
                 class="panel-collapse collapse">
                <div class="panel-body">
                    <form class="set-sr-preference" action="{{URL::to('/')}}/account/update-user-srp"
                          method="POST">
                        <input type="hidden" name="gatewayId" id="gatewayId"
                               value="{{$userResourceProfile->gatewayID}}">
                        <input type="hidden" name="storageResourceId"
                               id="storageResourceId"
                               value="{{$user_srp->storageResourceId}}">

                        <div class="form-horizontal">
                            @include('partials/user-storage-resource-preferences',
                            array('storageResource' => $user_srp->srDetails,
                            'preferences'=>$user_srp, 'show'=>true,
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
<div class="add-user-storage-resource-block hide">
    <div class="well">
        <form action="{{URL::to('/')}}/account/add-user-srp" method="POST">
            <input type="hidden" name="gatewayId" id="gatewayId" value="{{$userResourceProfile->gatewayID}}">

            <div id="user-sr-select-input-group" class="input-group">
                <select id="user-sr-select" name="storageResourceId" class="form-control">
                    <option value="">Select a Storage Resource and configure your account</option>
                    @foreach( (array)$unselectedSRs as $index => $sr)
                    <option value="{{ $sr->storageResourceId}}">{{ $sr->hostName }}</option>
                    @endforeach
                </select>
                <span class="input-group-addon remove-user-sr" style="cursor:pointer;">x</span>
            </div>
            <div class="user-sr-pref-space form-horizontal"></div>
        </form>
    </div>
</div>

<div class="modal fade" id="remove-user-storage-resource-block" tabindex="-1" role="dialog" aria-labelledby="add-modal"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/account/delete-user-srp" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center">Remove Storage Resource Account Confirmation</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control remove-user-srId" name="rem-user-srId"/>

                    Do you really want to remove your Storage Resource Account settings for <span class="remove-user-sr-name"> </span>?
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
<script>

$('.add-user-sr').on('click', function(){

    $('#add-user-storage-resource-block-container').append( $(".add-user-storage-resource-block").html() );
});
$(".remove-user-storage-resource").click( function(){
	$(".remove-user-sr-name").html( $(this).data("sr-name") );
	$(".remove-user-srId").val( $(this).data("sr-id") );
});
$("#add-user-storage-resource-block-container").on("change", "#user-sr-select", function(){
    srId = $(this).val();
    //This is done as Jquery creates problems when using period(.) in id or class.
    srId = srId.replace(/\./g,"_");
    $("#add-user-storage-resource-block-container .user-sr-pref-space").html($("#sr-" + srId).html());
});
$("#add-user-storage-resource-block-container").on("click", ".remove-user-sr", function(){
    $("#add-user-storage-resource-block-container").empty();
});
</script>
@stop