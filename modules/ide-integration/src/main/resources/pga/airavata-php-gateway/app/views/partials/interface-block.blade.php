@if( isset( $interfaceObject) )
<input type="hidden" name="app-interface-id" value="{{$interfaceObject->applicationInterfaceId}}"/>
@endif
<div class="appInterfaceInputs">
    <div class="form-group required">
        <label class="control-label interface-name">Application Name</label>
        <input type="text" readonly class="form-control" name="applicationName" required
               value="@if( isset( $interfaceObject) ){{$interfaceObject->applicationName}}@endif"/>
    </div>
    <div class="form-group">
        <label class="control-label">Application Description</label>
        <input type="text" readonly class="form-control" name="applicationDescription"
               value="@if( isset( $interfaceObject) ){{ $interfaceObject->applicationDescription}}@endif"/>
    </div>
    <div class="form-group">
        <label class="control-label">Application Modules</label>
        <div class="app-modules">
            @if( isset( $interfaceObject))
            <div class="input-group">
                <select name="applicationModules[]" class="app-module-select form-control" style="min-width: 200px" readonly>
            @for( $i=0; $i< count( $interfaceObject->applicationModules); $i++ )
                    @foreach( $modules as $index => $module)
                    <option value="{{ $module->appModuleId }}"
                    @if( $interfaceObject->applicationModules[$i] == $module->appModuleId)
                        selected
                    @endif
                    >{{$module->appModuleName}}</option>
                    @endforeach
            @endfor
                </select>
                <span class="input-group-addon hide remove-app-module" style="cursor:pointer;">x</span>
            </div>
            @endif
        </div>
        <button type="button" class="hide btn btn-default add-app-module">Add Application Module</button>
    </div>
    
    <div class="form-group">
        <label class="control-label">Enable Archiving Working Directory</label>
        <select name="archiveWorkingDirectory" class="form-control" style="max-width: 200px" readonly>
            <option value="0"
             @if( isset( $interfaceObject) ) @if( false == $interfaceObject->archiveWorkingDirectory ) selected @endif @endif>False</option>
            <option value="1"
             @if( isset( $interfaceObject) ) @if( true == $interfaceObject->archiveWorkingDirectory ) selected @endif @endif>True</option>
        </select>
    </div>

    <div class="form-group">
        <label class="control-label">Enable Optional File Inputs</label>
        <select name="hasOptionalFileInputs" class="form-control" style="max-width: 200px" readonly>
            <option value="0"
                    @if( isset( $interfaceObject) ) @if( false == $interfaceObject->hasOptionalFileInputs ) selected @endif @endif>False</option>
            <option value="1"
                    @if( isset( $interfaceObject) ) @if( true == $interfaceObject->hasOptionalFileInputs ) selected @endif @endif>True</option>
        </select>
    </div>

    <div class="form-group form-horizontal">
        @if( isset( $interfaceObject))
        @foreach( (array)$interfaceObject->applicationInputs as $index => $appInputs)
        @include( 'partials/interface-input-block', array('dataTypes' => $dataTypes, 'appInputs' => $appInputs) )
        @endforeach
        @endif
        <div class="app-inputs"></div>
        <button type="button" class=" hide btn btn-default add-input">Add Application Input</button>
    </div>
    <div class="form-group form-horizontal">
        @if( isset( $interfaceObject) )
        @foreach( (array)$interfaceObject->applicationOutputs as $index => $appOutputs)
        @include( 'partials/interface-output-block', array('dataTypes' => $dataTypes, 'appOutputs' => $appOutputs) )
        @endforeach
        @endif
        <div class="app-outputs"></div>
        <button type="button" class="hide btn btn-default add-output">Add Application Output</button>
    </div>
</div>