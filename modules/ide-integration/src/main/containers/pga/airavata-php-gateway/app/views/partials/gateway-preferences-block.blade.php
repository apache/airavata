<div class="panel panel-default">
    <div class="panel-heading">
        <h4 class="panel-title">
            <a class="accordion-toggle collapsed gateway-name" data-toggle="collapse"
               data-parent="#accordion2" href="#{{$accName}}-collapse-gateway-{{$indexGP}}">
                {{ $gp->gatewayName }}
            </a>
            @if(Session::has("admin"))
            <!-- Backend needs to be added for this
            
            <div class="pull-right col-md-2 gateway-options fade">
                <span class="glyphicon glyphicon-pencil edit-gateway" style="cursor:pointer;"
                      data-toggle="modal" data-target="#edit-gateway-block"
                      data-gp-id="{{ $gp->gatewayId }}" data-gp-name="{{ $gp->gatewayName }}"></span>
            </div>
            -->
            @endif
        </h4>
    </div>
    <div id="{{$accName}}-collapse-gateway-{{$indexGP}}" class="panel-collapse collapse">
        <div class="panel-body">
            <div class="app-interface-block">
                <div class="row">
                    <div class="col-md-10 credential-store-token-change">
                        <form>
                            <div class="form-group">
                                <label class="control-label col-md-12">{{ Session::get('theme') }} Credential Store Token</label>
                                <div class="col-md-9">
                                    <select class="form-control gateway-credential-store-token" id="gateway-credential-store-token" name="resourceSpecificCredentialStoreToken"  data-gpid="{{$gp->gatewayId}}" >
                                        <option value="">Select a Credential Token from Store</option>
                                        @if( isset( $gp->profile->credentialStoreToken) )
                                        @foreach( $tokens as $val)
                                            <option value="{{$val->token}}" @if( $val->token == $gp->profile->credentialStoreToken) selected @endif>
                                                @if($val->description)
                                                    {{{$val->description}}}
                                                @else
                                                    NO DESCRIPTION: ({{{$val->token}}})
                                                @endif
                                            </option>
                                        @endforeach
                                        @else
                                        @foreach( $tokens as $val)
                                            <option value="{{$val->token}}">
                                                @if($val->description)
                                                    {{{$val->description}}}
                                                @else
                                                    NO DESCRIPTION: ({{{$val->token}}})
                                                @endif
                                            </option>
                                        @endforeach
                                        @endif
                                        <option value="">DO-NO-SET</option>
                                    </select>
                                    <!--
                                    <input type="text" name="resourceSpecificCredentialStoreToken"  data-gpid="{{$gp->gatewayId}}" class="form-control credential-store-token"
                                           value="@if( isset( $gp->profile->credentialStoreToken) ){{$gp->profile->credentialStoreToken}}@endif"/>
                                    -->
                                </div>
                                <div class="col-md-3">
                                        <input type="submit" class="form-control btn btn-primary" value="Set"/>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                    
                <div class="col-md-10">


                    <div class="row">
                        <button class="btn btn-default add-cr" data-gpid="{{$gp->gatewayId}}"><span
                                class="glyphicon glyphicon-plus"></span> Add a Compute Resource Preference
                        </button>
                    </div>

                    @if( isset( $gp->profile->computeResourcePreferences) )
                    @if( count( (array)$gp->profile->computeResourcePreferences) > 0)
                    <div>
                        <h3>Compute Resource Preferences :</h3>
                    </div>
                    @endif
                    <div class="accordion-inner">
                        <div class="panel-group" id="cr-{{$accName}}-{{$indexGP}}">
                            @foreach( (array)$gp->profile->computeResourcePreferences as $indexCRP
                            => $crp )
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="accordion-toggle collapsed gateway-name"
                                           data-toggle="collapse" data-parent="#accordion"
                                           href="#{{$accName}}-collapse-crp-{{$indexGP}}-{{$indexCRP}}">
                                            {{ $crp->crDetails->hostName }}
                                        </a>
                                        @if(Session::has("admin"))
                                        <div class="pull-right col-md-2 gateway-options fade">
                                            <span class="glyphicon glyphicon-remove remove-compute-resource"
                                                  style="cursor:pointer;" data-toggle="modal"
                                                  data-target="#remove-compute-resource-block"
                                                  data-cr-name="{{$crp->crDetails->hostName}}"
                                                  data-cr-id="{{$crp->computeResourceId}}"
                                                  data-gp-id="{{ $gp->gatewayId }}"></span>
                                        </div>
                                        @endif
                                    </h4>
                                </div>
                                <div id="{{$accName}}-collapse-crp-{{$indexGP}}-{{$indexCRP}}"
                                     class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="app-compute-resource-preferences-block">
                                            <form class="set-cr-preference" action="{{URL::to('/')}}/gp/update-crp"
                                                  method="POST">
                                                <input type="hidden" name="gatewayId" id="gatewayId"
                                                       value="{{$gp->gatewayId}}">
                                                <input type="hidden" name="computeResourceId"
                                                       id="gatewayId"
                                                       value="{{$crp->computeResourceId}}">

                                                <div class="form-horizontal">
                                                    @include('partials/compute-resource-preferences',
                                                    array('computeResource' => $crp->crDetails,
                                                    'crData' => $crData, 'preferences'=>$crp,
                                                    'show'=>true))
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            @endforeach
                        </div>
                    </div>
                    @endif


                    <div class="row">
                        <button class="btn btn-default add-dsp" data-gpid="{{$gp->gatewayId}}"><span
                                class="glyphicon glyphicon-plus"></span> Add a Storage Resource Preference
                        </button>
                    </div>

                    @if( isset( $gp->profile->storagePreferences) )
                    @if( count( (array)$gp->profile->storagePreferences) > 0)
                    <div>
                        <h3>Storage Resource Preferences :</h3>
                    </div>
                    @endif
                    <div class="accordion-inner">
                        <div class="panel-group" id="cr-accordion-{{$indexGP}}">
                            @foreach( (array)$gp->profile->storagePreferences as $indexSRP
                            => $srp )
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="accordion-toggle collapsed gateway-name"
                                           data-toggle="collapse" data-parent="#accordion"
                                           href="#{{$accName}}-collapse-srp-{{$indexGP}}-{{$indexSRP}}">
                                            {{ $srp->srDetails->hostName }}
                                        </a>
                                        @if(Session::has("admin"))
                                        <div class="pull-right col-md-2 gateway-options fade">
                                            <span class="glyphicon glyphicon-remove remove-storage-resource"
                                                  style="cursor:pointer;" data-toggle="modal"
                                                  data-target="#remove-storage-resource-block"
                                                  data-sr-name="{{$srp->srDetails->hostName}}"
                                                  data-sr-id="{{$srp->storageResourceId}}"
                                                  data-gp-id="{{ $gp->gatewayId }}"></span>
                                        </div>
                                        @endif
                                    </h4>
                                </div>
                                <div id="{{$accName}}-collapse-srp-{{$indexGP}}-{{$indexSRP}}"
                                     class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="app-compute-resource-preferences-block">
                                            <form class="set-sr-preference" action="{{URL::to('/')}}/gp/update-srp"
                                                  method="POST">
                                                <input type="hidden" name="gatewayId" id="gatewayId"
                                                       value="{{$gp->gatewayId}}">
                                                <input type="hidden" name="storageResourceId"
                                                       id="gatewayId"
                                                       value="{{$srp->storageResourceId}}">

                                                <div class="form-horizontal">
                                                    @include('partials/storage-resource-preferences',
                                                    array('storageResource' => $srp->srDetails,
                                                    'srData' => $srData, 'preferences'=>$srp,
                                                    'show'=>true))
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            @endforeach
                        </div>
                    </div>
                    @endif

                    {{-- <br/>
                    <div>
                        <h3>Identity Server Configuration :</h3>
                    </div>
                    <div class="panel panel-default panel-body">
                        <form action="{{URL::to('/')}}/gp/update-idp" method="POST">
                            <input type="hidden" name="gatewayId" id="gatewayId"
                                   value="{{$gp->gatewayId}}">
                            <br/>
                            <div class="form-horizontal">
                                <div>
                                    <div class="form-group">
                                        <label class="control-label col-md-3">Tenant ID</label>
                                        <div class="col-md-9">
                                            <input type="text" name="identityServerTenant" class="form-control"
                                                   value="@if( isset( $gp->profile->identityServerTenant) ){{$gp->profile->identityServerTenant}}@endif"/>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="control-label col-md-3">Identity Server Password Credential</label>
                                        <div class="col-md-9">
                                            <select class="form-control gateway-credential-store-token" name="identityServerPwdCredToken">
                                                @foreach( $pwdTokens as $token => $description)
                                                    <option value="{{$token}}" @if( isset( $gp->profile->identityServerPwdCredToken) ) @if( $token == $gp->profile->identityServerPwdCredToken) selected @endif @endif>{{$token}}</option>
                                                @endforeach
                                                <option value="">DO-NO-SET</option>
                                            </select>
                                        </div>
                                    </div>
                                    @if(Session::has("admin"))
                                        <div class="form-group text-center">
                                            <input type="submit" class="btn btn-primary submit-srp-form" value="Set preferences"/>
                                        </div>
                                    @endif
                                </div>
                            </div>
                        </form>
                    </div> --}}
                </div>

                <div class="col-md-10">
                    @if( isset( $gp->profile->dataStoragePreferences) )
                    <div>
                        <h3>Data Storage Preferences :</h3>
                    </div>

                    <div class="accordion-inner">
                        <div class="panel-group" id="accordion-{{$indexGP}}">
                            @foreach( (array)$gp->profile->dataStoragePreferences as $indexDSP
                            => $dsp )
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="accordion-toggle collapsed gateway-name"
                                           data-toggle="collapse" data-parent="#accordion"
                                           href="#collapse-dsp-{{$indexGP}}-{{$indexDSP}}">
                                            {{ $dsp->dataMovememtResourceId }}
                                        </a>
                                        @if(Session::has("admin"))
                                        <div class="pull-right col-md-2 gateway-options fade">
                                            <span class="glyphicon glyphicon-remove remove-storage-resource"
                                                  style="cursor:pointer;" data-toggle="modal"
                                                  data-target="#remove-storage-resource-block"
                                                  data-dsp-id="{{$ds->computeResourceId}}"
                                                  data-gp-id="{{ $gp->gatewayId }}"></span>
                                        </div>
                                        @endif
                                    </h4>
                                </div>
                                <div id="collapse-dsp-{{$indexGP}}-{{$indexDSP}}"
                                     class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="app-data-storage-preferences-block">
                                            <form action="{{URL::to('/')}}/gp/update-dsp"
                                                  method="POST">
                                                <input type="hidden" name="gatewayId" id="gatewayId"
                                                       value="{{$gp->gatewayId}}">
                                                <input type="hidden" name="dataStorageId"
                                                       id="gatewayId"
                                                       value="{{$crp->dataMovememtResourceId}}">

                                                <div class="form-horizontal">
                                                    @include('partials/compute-resource-preferences',
                                                    array('computeResource' => $crp->crDetails,
                                                    'crData' => $crData, 'preferences'=>$crp,
                                                    'show'=>true))
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            @endforeach
                        </div>
                    </div>
                    @endif

                </div>
            </div>
        </div>
    </div>
</div>