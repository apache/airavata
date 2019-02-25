@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
@stop

@section('content')

<div id="wrapper">
    <!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
    @include( 'partials/dashboard-block')
    <div id="page-wrapper">

        <div class="container-fluid">
            <div class="col-md-12">
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

                <h1 class="text-center">SSH Keys</h1>
                <div class="error-alert"></div>

                @if(Session::has("admin"))
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Generate New SSH Key</h3>
                    </div>
                    <div class="panel-body">
                        <form id="new-ssh-form-submit" class="form-inline" action="{{URL::to('/')}}/admin/create-ssh-token" method="post">
                            <div id="credential-description-form-group" class="form-group">
                                <label for="credential-description" class="sr-only">Description for new SSH key</label>
                                <input type="text" id="credential-description" name="description" class="form-control" placeholder="Description" required/>
                            </div>
                            <button type="submit" class="btn btn-default">Generate</button>
                        </form>
                    </div>
                </div>
                @endif

                <ul class="list-group">
                    @foreach ($tokens as $val)
                    <li class="list-group-item credential-item">
                        <div class="row row_desc">
                            <div class="col-md-12 ssh_description">
                                @if($val->description!=null)
                                <p><strong>{{{ $val->description }}}</strong></p>
                                @else
                                <p style="color:red"><strong>NO DESCRIPTION! ({{{ $val->token }}})</strong></p>
                                @endif
                            </div>
                        </div><!-- .row -->
                        <div class="row row_details">
                            <div class="col-md-6">
                                <div class="input-group">
                                    <input type="text" class="form-control public-key" readonly
                                        id="credential-publickey-{{$val->token}}"
                                        value="{{ $val->publicKey }}">
                                    <span class="input-group-btn">
                                        <button type="button" class="btn btn-default copy-credential"
                                            data-clipboard-target="#credential-publickey-{{$val->token}}"
                                            data-toggle="tooltip" data-placement="bottom"
                                            data-title="Copied!" data-trigger="manual">
                                            Copy
                                        </button>
                                    </span>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <button data-token="{{$val->token}}"class="btn btn-danger delete-credential"
                                    @if(!Session::has("admin")) disabled @endif>Delete</button>
                            </div><br/>
                        </div><!-- .row -->
                    </li>
                    @endforeach
                </ul>                

                <!--
                @if(Session::has("admin"))
                <div class="row">
                    <h1 class="text-center">My Proxy Credentials</h1>

                    <div class="col-md-offset-3 col-md-6">
                        <table class="table table-striped table-condensed">
                            <tr>
                                <td>My Proxy Server</td>
                                <td><input type="text" class="form-control" placeholder="" value=""/></td>
                            </tr>
                            <tr>
                                <td>Username</td>
                                <td><input type="text" class="form-control" placeholder="" value=""/></td>
                            </tr>
                            <tr>
                                <td>Passphrase</td>
                                <td><input type="text" class="form-control" placeholder="" value=""/></td>
                            </tr>
                        </table>
                        <table class="table">
                            <tr class="text-center table-condensed">
                                <td>
                                    <button class="btn btn-default">Submit</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                @endif
                -->
                    <br/>
                    <h1 class="text-center">Password Credentials</h1>
                    @if(Session::has("admin"))
                        <table class="table">
                            <tr class="text-center table-condensed">
                                <td>
                                    <button class="btn btn-default" data-toggle="modal" data-target="#pwd-cred-form">Register a new password credential</button>
                                </td>
                            </tr>
                        </table>
                        <div class="loading-img-pwd text-center hide">
                            <img src="../../assets/ajax-loader.gif"/>
                        </div>
                    @endif
                    <ul class="list-group">
                        @foreach ($pwdTokens as $token => $desc)
                        <li class="list-group-item credential-item">
                            <div class="row row_desc">
                                <div class="col-md-12 pwd_description">
                                    <p><strong>{{{ $desc }}}</strong></p>
                                </div>
                            </div><!-- .row -->
                            <div class="row row_details">
                                <div class="col-md-6">
                                    <div class="input-group">
                                        <input type="text" class="form-control public-key" readonly
                                            id="credential-publickey-{{$token}}"
                                            value="{{ $token }}">
                                        <span class="input-group-btn">
                                            <button type="button" class="btn btn-default copy-credential"
                                                data-clipboard-target="#credential-publickey-{{$token}}"
                                                data-toggle="tooltip" data-placement="bottom"
                                                data-title="Copied!" data-trigger="manual">
                                                Copy
                                            </button>
                                        </span>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <button data-token="{{$token}}"class="btn btn-danger remove-pwd-token"
                                        @if(!Session::has("admin")) disabled @endif>Delete</button>
                                </div><br/>
                            </div><!-- .row -->
                        </li>
                        @endforeach
                    </ul>           

                {{--<h1 class="text-center">Amazon Credentials</h1>--}}

                {{--<table class="table table-striped table-condensed">--}}
                    {{--<tr class="text-center">--}}
                        {{--<td>Under Development</td>--}}
                    {{--</tr>--}}
                {{--</table>--}}

                {{--<h1 class="text-center">OAuth MyProxy</h1>--}}

                {{--<table class="table table-striped table-condensed">--}}
                    {{--<tr class="text-center">--}}
                        {{--<td>Under Development</td>--}}
                    {{--</tr>--}}
                {{--</table>--}}
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="delete-credential-modal" tabindex="-1" role="dialog" aria-labelledby="delete-credential-modal-title"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/admin/remove-ssh-token" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center" id="delete-credential-modal-title">Delete SSH Public Key</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control" name="token"/>

                    Do you really want to delete the "<span class="credential-description"></span>" SSH public key?
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

<div class="modal fade" id="delete-pwd-modal" tabindex="-1" role="dialog" aria-labelledby="delete-pwd-modal-title"
     aria-hidden="true">
    <div class="modal-dialog">

        <form action="{{URL::to('/')}}/admin/remove-pwd-token" method="POST">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="text-center" id="delete-pwd-modal-title">Delete Password Credential</h3>
                </div>
                <div class="modal-body">
                    <input type="hidden" class="form-control" name="token"/>

                    Do you really want to delete the "<span class="pwd-description"></span>" Password Credential?
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

<div class="modal fade" id="pwd-cred-form" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
                <button type="button" class="close"
                        data-dismiss="modal">
                    <span aria-hidden="true">&times;</span>
                    <span class="sr-only">Close</span>
                </button>
                <h4 class="modal-title">
                    Password Credential
                </h4>
            </div>

            <!-- Modal Body -->
            <div class="modal-body">

                <form role="form" id="register-pwd-form" action="{{URL::to('/')}}/admin/create-pwd-token" method="POST">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" class="form-control" required="required"
                               id="username" name="username" placeholder="Username"/>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="text" class="form-control" required="required"
                               id="password" name="password" placeholder="Password"/>
                    </div>

                    <div class="form-group">
                        <label for="description">Description</label>
                        <input type="text" class="form-control" required="required"
                               id="description" name="description" placeholder="Description"/>
                    </div>

                    <!-- Modal Footer -->
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default"
                                data-dismiss="modal">
                            Close
                        </button>
                        <button type="submit" class="btn btn-primary">
                            Save changes
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
@stop

@section('scripts')
@parent
{{ HTML::script('js/clipboard.min.js') }}
<script>
    $('.delete-credential').on('click', function(e){

        var removeSpan = $(this);
        var par = removeSpan.closest("li");
        var credentialStoreToken = removeSpan.data("token");
        var credentialDescription = $.trim(par.find('.ssh_description').text());

        $("#delete-credential-modal input[name=token]").val(credentialStoreToken);
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

   $("#new-ssh-form-submit").submit( function(){
        var description = $("#credential-description").val();
        var items = $('.ssh_description').map(function () { return $.trim($(this).text()); }).get();
        for(var i=0;i<items.length;++i){
            if(description === $.trim(items[i])){
                $('.error-alert').html("<div class='alert alert-danger' role='alert'><button type='button' class='close' data-dismiss='alert'><span aria-hidden='true'>&times;</span><span class='sr-only'>Close</span></button>Description should be unique for each key.</div>");
                return false;
            }
        }
        return true;
   });

   $("#register-pwd-form").submit( function() {
        var pcred_description = $("#description").val();
        var items = $('.pwd_description').map(function() { return $.trim($(this).text()); }).get();
        for(var i=0;i<items.length;++i){
            if(pcred_description === $.trim(items[i])){
                $("#pwd-cred-form").modal("hide");
                $('.error-alert').html("<div class='alert alert-danger' role='alert'><button type='button' class='close' data-dismiss='alert'><span aria-hidden='true'>&times;</span><span class='sr-only'>Close</span></button>Description should be unique for each key.</div>");
                return false;
            }
        }
        return true;

   });


   $(".remove-pwd-token").click( function(){
        var removeSpan = $(this);
        var par = removeSpan.closest("li");
        var credentialStoreToken = removeSpan.data("token");
        var credentialDescription = $.trim(par.find('.pwd_description').text());
        
        $("#delete-pwd-modal input[name=token]").val(credentialStoreToken);
        $("#delete-pwd-modal .pwd-description").text(credentialDescription);
        $("#delete-pwd-modal").modal("show");
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