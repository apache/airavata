<!-- partial template variables:
    storageResource - (required, StorageResourceDescription) the storage resource object
    credentialSummaries - (required, list of CredentialSummary) user's credentials
    defaultCredentialSummary - (required, CredentialSummary) user's default credential
    preferences - (optional, UserStoragePreference) the saved preference data
    show - (optional, boolean)
    allowDelete - (optional, boolean)
-->
<!-- String replace is done as Jquery creates problems when using period(.) in id or class. -->
<div id="sr-{{ str_replace( '.', "_", $storageResource->storageResourceId) }}" class="@if(isset( $show) ) @if( !$show) hide @endif @else hide @endif">
<div class="form-group">
    <label class="control-label col-md-3">Login Username</label>

    <div class="col-md-9">
        <input type="text" name="loginUserName" class="form-control"
               value="@if( isset( $preferences) ){{$preferences->loginUserName}}@endif"/>
    </div>
</div>

<div class="form-group">
    <label class="control-label col-md-3">File System Root Location</label>

    <div class="col-md-9">
        <input type="text" name="fileSystemRootLocation" class="form-control"
               value="@if( isset( $preferences) ){{$preferences->fileSystemRootLocation}}@endif"/>
    </div>
</div>

<div class="form-group">
    <label class="control-label col-md-3">Resource Specific SSH Key</label>

    <div class="col-md-9">
        <select class="form-control" name="resourceSpecificCredentialStoreToken" >
            <option value="" @if( isset( $preferences) && $preferences->resourceSpecificCredentialStoreToken == null) selected @endif>
                No resource specific SSH key, just use the default one ({{{$defaultCredentialSummary->description}}})
            </option>
            @foreach( $credentialSummaries as $token => $credentialSummary )
            @if( $token != $defaultCredentialSummary->token)
            <option value="{{$token}}" @if( isset( $preferences) && $token == $preferences->resourceSpecificCredentialStoreToken) selected @endif>
                Use {{{$credentialSummary->description}}}
            </option>
            @endif
            @endforeach
        </select>
    </div>
</div>

<div class="row">
    <div class="form-group col-md-12 text-center">
        <input type="submit" class="btn btn-primary" value="Save"/>
        <button type="button" class="btn btn-danger remove-user-storage-resource @if(isset( $allowDelete ) ) @if( !$allowDelete) hide @endif @else hide @endif"
            data-toggle="modal"
            data-target="#remove-user-storage-resource-block"
            data-sr-name="{{$storageResource->hostName}}"
            data-sr-id="{{$storageResource->storageResourceId}}">
            Remove
        </button>
    </div>
</div>

</div>
