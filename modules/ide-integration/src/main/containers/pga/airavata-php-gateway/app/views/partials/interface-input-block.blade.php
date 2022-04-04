<div class="well app-input-data-block">
    @if(Session::has("admin"))
    <span class="glyphicon glyphicon-trash pull-right remove-input-space"></span>
    @endif
    <h4>App Input Fields</h4>

    <div class="form-group required">
        <label class="control-label col-md-3">Name</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="inputName[]" required
                   value="@if( isset( $appInputs) ){{$appInputs->name}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Value</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="inputValue[]"
                   value="@if( isset( $appInputs)){{$appInputs->value}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Type</label>

        <div class="col-md-9">
            <select class="form-control" name="inputType[]" readonly>
                @foreach( $dataTypes as $index => $dataType)
                    @if( $dataType != 'URI_COLLECTION' && $dataType != 'STDOUT' && $dataType != 'STDERR')
                        <option value="{{ $index }}"
                        @if( isset( $appInputs) ) @if( $index == $appInputs->type) selected @endif @endif>{{ $dataType
                        }}</option>
                    @endif
                @endforeach
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Application Argument</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="applicationArgumentInput[]"
                   value="@if( isset( $appInputs) ){{$appInputs->applicationArgument }}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Standard Input</label>

        <div class="col-md-9">
            <select class="form-control" name="standardInput[]" readonly>
                <option value="0"
                @if( isset( $appInputs) ) @if( 0 == $appInputs->standardInput) selected @endif @endif>False</option>
                <option value="1"
                @if( isset( $appInputs) ) @if( 1 == $appInputs->standardInput) selected @endif @endif>True</option>
            </select>
        </div>
    </div>
        <div class="form-group">
            <label class="control-label col-md-3">Is Read Only</label>

            <div class="col-md-9">
                <select class="form-control" name="isReadOnly[]" readonly>
                    <option value="0"
                            @if( isset( $appInputs) ) @if( 0 == $appInputs->isReadOnly) selected @endif @endif>False</option>
                    <option value="1"
                            @if( isset( $appInputs) ) @if( 1 == $appInputs->isReadOnly) selected @endif @endif>True</option>
                </select>
            </div>
        </div>
    <div class="form-group">
        <label class="control-label col-md-3">User Friendly Description</label>

        <div class="col-md-9">
            <textarea readonly class="form-control" name="userFriendlyDescription[]">@if( isset( $appInputs)
                ){{$appInputs->userFriendlyDescription}}@endif</textarea>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Input Order</label>

        <div class="col-md-9">
            <input type="number" readonly class="form-control" name="inputOrder[]"
                   value="@if( isset( $appInputs) ){{$appInputs->inputOrder}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Data is Staged?</label>

        <div class="col-md-9">
            <select name="dataStaged[]" readonly class="form-control">
                <option value="0"
                        @if( isset( $appInputs) ) @if( $appInputs->dataStaged == 0) selected @endif @endif>False</option>
                <option value="1"
                @if( isset( $appInputs) ) @if( $appInputs->dataStaged == 1) selected @endif @endif>True</option>
            </select>
        </div>
        <!-- Removed Radio button because it creates problems with multiple inputs
        <label class="radio-inline">
            <input type="radio" name="dataStaged[]"  @if( isset( $appInputs) ) @if( $appInputs->dataStaged == 1) checked @endif @endif>True
        </label>
        <label class="radio-inline">
            <input type="radio" name="dataStaged[]"  @if( isset( $appInputs) ) @if( $appInputs->dataStaged == 0) checked @endif @endif>False
        </label>
        -->
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Is the Input required?</label>

        <div class="col-md-9">
            <select class="form-control" name="isRequiredInput[]" readonly>
                <option value="0"
                @if( isset( $appInputs) ) @if( 0 == $appInputs->isRequired) selected @endif @endif>False</option>
                <option value="1"
                @if( isset( $appInputs) ) @if( 1 == $appInputs->isRequired) selected @endif @endif>True</option>
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Required on command line?</label>

        <div class="col-md-9">
            <select class="form-control" name="requiredToAddedToCommandLineInput[]" readonly>
                <option value="0"
                @if( isset( $appInputs) ) @if( 0 == $appInputs->requiredToAddedToCommandLine) selected @endif
                @endif>False</option>
                <option value="1"
                @if( isset( $appInputs) ) @if( 1 == $appInputs->requiredToAddedToCommandLine) selected @endif
                @endif>True</option>
            </select>
        </div>
    </div>
    {{-- <div class="form-group">
        <label class="control-label col-md-3">Meta Data</label>

        <div class="col-md-9">
            <textarea readonly class="form-control" name="metaData[]">@if( isset( $appInputs)
                ){{$appInputs->metaData}}@endif</textarea>
        </div>
    </div> --}}
</div>