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
                <div class="row">
                    <div class="alert alert-success alert-dismissible" role="alert">
                        <button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span
                                class="sr-only">Close</span></button>
                        {{ Session::get("message") }}
                    </div>
                </div>
                {{ Session::forget("message") }}
                @endif

                <h1 class="text-center">SSH Keys</h1>
                @if(Session::has("admin"))
                <table class="table">
                    <tr class="text-center table-condensed">
                        <td>
                            <button class="btn btn-default generate-ssh">Generate a new token</button>
                        </td>
                    </tr>
                </table>
                <div class="loading-img text-center hide">
                   <img src="../../assets/ajax-loader.gif"/>
                </div>
                @endif
                <table class="table table-bordered table-condensed" style="word-wrap: break-word;">
                    <tr>
                        <th class="text-center">
                            Token
                        </th>
                        <th class="text-center">Public Key</th>
                        @if( Session::has("admin"))
                        <th>Delete</th>
                        @endif
                    </tr>
                    <tbody class="token-values-ssh">
                    @foreach( $tokens as $token => $publicKey)
                    <tr>
                        <td class="">
                            {{ $token }}
                        </td>
                        <td class="public-key">
                            {{ $publicKey }}
                        </td>
                        @if( Session::has("admin"))
                        <td>
                            <span data-token="{{$token}}" class="glyphicon glyphicon-trash remove-ssh-token"></span>
                        </td>
                        @endif
                    </tr>
                    @endforeach
                    </tbody>
                </table>
                

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
                    <table class="table table-bordered table-condensed" style="word-wrap: break-word;">
                        <tr>
                            <th class="text-center">
                                Token
                            </th>
                            <th class="text-center">Description</th>
                            @if( Session::has("admin"))
                                <th>Delete</th>
                            @endif
                        </tr>
                        <tbody class="token-values">
                        @foreach( $pwdTokens as $token => $publicKey)
                            <tr>
                                <td class="">
                                    {{ $token }}
                                </td>
                                <td class="description">
                                    {{ $publicKey }}
                                </td>
                                @if( Session::has("admin"))
                                    <td>
                                        <span data-token="{{$token}}" class="glyphicon glyphicon-trash remove-pwd-token"></span>
                                    </td>
                                @endif
                            </tr>
                        @endforeach
                        </tbody>
                    </table>

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
<script>
   $(".generate-ssh").click( function(){
        $(".loading-img").removeClass("hide");
        $.ajax({
          type: "POST",
          url: "{{URL::to('/')}}/admin/create-ssh-token"
        }).success( function( data){

            var tokenJson = data;

            //$(".token-values").html("");
            $(".generate-ssh").after("<div class='alert alert-success new-token-msg'>New Token has been generated.</div>");

            $(".token-values-ssh").prepend("<tr class='alert alert-success'><td>" + tokenJson.token + "</td><td class='public-key'>" + tokenJson.pubkey + "</td>" + "<td><a href=''><span data-token='"+tokenJson.token+"' class='glyphicon glyphicon-trash remove-token'></span></a></td></<tr>");
            $(".loading-img").addClass("hide");
            
            setInterval( function(){
                $(".new-token-msg").fadeOut();
            }, 3000);
        }).fail( function( data){
        $(".loading-img").addClass("hide");

            failureObject = $.parseJSON( data.responseText);
            $(".generate-ssh").after("<div class='alert alert-danger'>" + failureObject.error.message + "</div>");
        });
   });

   $(".remove-ssh-token").click( function(){
        var removeSpan = $(this);
        var tr = removeSpan.parent().parent();
        var tokenToRemove = removeSpan.data("token");
        var publicKey = tr.children(".public-key").html();
        tr.children(".public-key").html("<div class='alert alert-danger'>Do you really want to remove the token? This action cannot be undone.<br/>" +
                                                                    "<span class='btn-group'>"+
                                                                    "<input type='button' class='btn btn-default remove-token-confirmation' value='Yes'/>" +
                                                                    "<input type='button' class='btn btn-default remove-token-cancel' value='Cancel'/>"+
                                                                    "</span></div>");

        
        tr.find( ".remove-token-confirmation").click( function(){
            $(".loading-img").removeClass("hide");
            $.ajax({
              type: "POST",
              data:{ "token" : tokenToRemove},
              url: "{{URL::to('/')}}/admin/remove-ssh-token"
              }).success( function( data){
                if( data.responseText == 1)
                    tr.addClass("alert").addClass("alert-danger");
                        tr.fadeOut(1000);
            }).fail( function( data){
                tr.after("<tr class='alert alert-danger'><td></td><td>Error occurred : " + $.parseJSON( data.responseText).error.message + "</td><td></td></tr>");
            }).complete( function(){
                $(".loading-img").addClass("hide");

            });
        });
        tr.find( ".remove-token-cancel").click( function(){
            tr.children(".public-key").html( publicKey);
        });
        
   });

   $(".remove-pwd-token").click( function(){
       var removeSpan = $(this);
       var tr = removeSpan.parent().parent();
       var tokenToRemove = removeSpan.data("token");
       var description = tr.children(".description").html();
       tr.children(".description").html("<div class='alert alert-danger'>Do you really want to remove the token? This action cannot be undone.<br/>" +
               "<span class='btn-group'>"+
               "<input type='button' class='btn btn-default remove-token-confirmation' value='Yes'/>" +
               "<input type='button' class='btn btn-default remove-token-cancel' value='Cancel'/>"+
               "</span></div>");


       tr.find( ".remove-token-confirmation").click( function(){
           $(".loading-img-pwd").removeClass("hide");
           $.ajax({
               type: "POST",
               data:{ "token" : tokenToRemove},
               url: "{{URL::to('/')}}/admin/remove-pwd-token"
           }).success( function( data){
               if( data.responseText == 1)
                   tr.addClass("alert").addClass("alert-danger");
               tr.fadeOut(1000);
           }).fail( function( data){
               tr.after("<tr class='alert alert-danger'><td></td><td>Error occurred : " + $.parseJSON( data.responseText).error.message + "</td><td></td></tr>");
           }).complete( function(){
               $(".loading-img-pwd").addClass("hide");

           });
       });
       tr.find( ".remove-token-cancel").click( function(){
           tr.children(".description").html( description);
       });

   });
</script>
@stop