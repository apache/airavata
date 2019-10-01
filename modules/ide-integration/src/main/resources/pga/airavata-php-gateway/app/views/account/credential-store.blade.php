@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/user-settings.css')}}
@stop

@section('content')
<div class="container">
    <ol class="breadcrumb">
        <li><a href="{{ URL::to('/') }}/account/settings">User Settings</a></li>
        <li class="active">Credential Store</li>
    </ol>
    @if( Session::has("message"))
    <div class="alert alert-success alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                class="sr-only">Close</span></button>
        {{{ Session::get("message") }}}
    </div>
    {{ Session::forget("message") }}
    @endif

    @if( Session::has("error-message"))
    <div class="alert alert-danger alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                class="sr-only">Close</span></button>
        {{{ Session::get("error-message") }}}
    </div>
    {{ Session::forget("error-message") }}
    @endif

    <ul class="list-group">
        @foreach ($credentialSummaries as $credentialSummary)
        <li class="list-group-item credential-item">
            <div class="row">
                <div class="col-md-12">
                    <p><strong>{{{ $credentialSummary->description }}}</strong></p>
                </div>
            </div><!-- .row -->
            <div class="row">
                <div class="col-md-6">
                    <div class="input-group">
                        <input type="text" class="form-control" readonly
                            id="credential-publickey-{{$credentialSummary->token}}"
                            value="{{$credentialSummary->publicKey}}">
                        <span class="input-group-btn">
                            <button type="button" class="btn btn-default copy-credential"
                                data-clipboard-target="#credential-publickey-{{$credentialSummary->token}}"
                                data-toggle="tooltip" data-placement="bottom"
                                data-title="Copied!" data-trigger="manual">
                                Copy
                            </button>
                        </span>
                    </div>
                </div>
                <div class="col-md-6">
                    @if ($credentialSummary->token != $defaultCredentialToken)
                    <form style="display: inline" action="{{ URL::to('/') }}/account/set-default-credential" method="post">
                        <input type="hidden" name="defaultToken" value="{{$credentialSummary->token}}"/>
                        <button type="submit" class="btn btn-default" title="Test">Make Default</button>
                    </form>
                    <button data-token="{{$credentialSummary->token}}"
                        data-description="{{$credentialSummary->description}}"
                        class="btn btn-danger delete-credential"
                        @if(!$credentialSummary->canDelete) disabled @endif>Delete</button>
                    @else
                    <small>This is the default SSH public key that the gateway will use to authenticate to your compute and storage accounts.</small>
                    @endif
                </div>
            </div><!-- .row -->
        </li>
        @endforeach
    </ul>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Add SSH Key</h3>
        </div>
        <div class="panel-body">
            @if ($errors->has())
            @foreach ($errors->all() as $error)
            {{ CommonUtilities::print_error_message($error) }}
            @endforeach
            @endif
            <form id="add-credential" class="form-inline" action="{{ URL::to('/') }}/account/add-credential" method="post">
                <div id="credential-description-form-group" class="form-group">
                    <label for="credential-description" class="sr-only">Description for new SSH key</label>
                    <input type="text" id="credential-description" name="credential-description"
                    class="form-control" placeholder="Description" required/>
                </div>
                <button type="submit" class="btn btn-default">Add</button>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="delete-credential-modal" tabindex="-1" role="dialog" aria-labelledby="delete-credential-modal-title"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/account/delete-credential" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center" id="delete-credential-modal-title">Delete SSH Public Key</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control" name="credentialStoreToken"/>

                    Do you really want to delete the <span class="credential-description"></span> SSH public key?
                </div>
                <div class="modal-footer">
                    <div class="form-group">
                        <input type="submit" class="btn btn-danger" value="Delete"/>
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
{{ HTML::script('js/clipboard.min.js') }}
<script>
$('.delete-credential').on('click', function(e){

    var credentialStoreToken = $(this).data('token');
    var credentialDescription = $(this).data('description');

    $("#delete-credential-modal input[name=credentialStoreToken]").val(credentialStoreToken);
    $("#delete-credential-modal .credential-description").text(credentialDescription);
    $("#delete-credential-modal").modal("show");
});

$('#credential-description').on('invalid', function(event){
    this.setCustomValidity("Please provide a description");
    $('#credential-description-form-group').addClass('has-error');
});
$('#credential-description').on('keyup input change', function(event){
    if (this.checkValidity) {
        // Reset custom error message. If it isn't empty string it is considered invalid.
        this.setCustomValidity("");
        // checkValidity will cause invalid event to be dispatched. See invalid
        // event handler above which will set the custom error message.
        var valid = this.checkValidity();
        $('#credential-description-form-group').toggleClass('has-error', !valid);
    }
});

var clipboard = new Clipboard('.copy-credential');
clipboard.on('success', function(e){
    // Show 'Copied!' tooltip for 2 seconds on successful copy
    $(e.trigger).tooltip('show');
    setTimeout(function(){
        $(e.trigger).tooltip('hide');
    }, 2000);
});
</script>
@stop