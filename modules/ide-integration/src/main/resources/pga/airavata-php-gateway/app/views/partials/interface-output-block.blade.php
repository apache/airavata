<div class="well">
    @if(Session::has("admin"))
    <span class="glyphicon glyphicon-trash pull-right remove-output-space"></span>
    @endif
    <h4>App Output Fields</h4>

    <div class="form-group required">
        <label class="control-label col-md-3">Name</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="outputName[]" required
                   value="@if( isset( $appOutputs) ){{$appOutputs->name}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Value</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="outputValue[]"
                   value="@if( isset( $appOutputs) ){{$appOutputs->value}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Type</label>

        <div class="col-md-9">
            <select class="form-control" name="outputType[]" readonly>
                @foreach( $dataTypes as $index => $dataType)
                    @if( $dataType != 'URI_COLLECTION')
                        <option value="{{ $index }}"
                        @if( isset( $appOutputs) ) @if( $index == $appOutputs->type ) selected @endif @endif>{{ $dataType
                        }}</option>
                    @endif
                @endforeach
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Application Argument</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="applicationArgumentOutput[]"
                   value="@if( isset( $appOutputs) ){{$appOutputs->applicationArgument }}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Search Query</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="searchQuery[]"
                   value="@if( isset( $appOutputs) ){{$appOutputs->searchQuery }}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Data Movement</label>

        <div class="col-md-9">
            <select name="dataMovement[]" readonly class="form-control">
                <option value="0"
                @if( isset( $appOutputs) ) @if( $appOutputs->dataMovement == 0) selected @endif @endif>False</option>
                <option value="1"
                @if( isset( $appOutputs) ) @if( $appOutputs->dataMovement == 1) selected @endif @endif>True</option>
            </select>
        </div>
        <!--
        <label class="radio-inline">
            <input type="radio" name="dataMovement[]"  @if( isset( $appOutputs) ) @if( $appOutputs->dataMovement == 1) checked @endif @endif>True
        </label>
        <label class="radio-inline">
            <input type="radio" name="dataMovement[]"  @if( isset( $appOutputs) ) @if( $appOutputs->dataMovement == 0) checked @endif @endif>False
        </label>
        -->
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Is the Output required?</label>

        <div class="col-md-9">
            <select class="form-control" name="isRequiredOutput[]" readonly>
                <option value="0"
                @if( isset( $appOutputs) ) @if( 0 == $appOutputs->isRequired) selected @endif @endif>False</option>
                <option value="1"
                @if( isset( $appOutputs) ) @if( 1 == $appOutputs->isRequired) selected @endif @endif>True</option>
            </select>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Required on command line?</label>

        <div class="col-md-9">
            <select class="form-control" name="requiredToAddedToCommandLineOutput[]" readonly>
                <option value="0"
                @if( isset( $appOutputs) ) @if( 0 == $appOutputs->requiredToAddedToCommandLine) selected @endif
                @endif>False</option>
                <option value="1"
                @if( isset( $appOutputs) ) @if( 1 == $appOutputs->requiredToAddedToCommandLine) selected @endif
                @endif>True</option>
            </select>
        </div>
    </div>
    {{-- <div class="form-group">
        <label class="control-label col-md-3">Location</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="location[]"
                   value="@if( isset( $appOutputs) ){{$appOutputs->location}}@endif"/>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label col-md-3">Search Query</label>

        <div class="col-md-9">
            <input type="text" readonly class="form-control" name="searchQuery[]"
                   value="@if( isset( $appOutputs) ){{$appOutputs->searchQuery}}@endif"/>
        </div>
    </div> --}}
</div>