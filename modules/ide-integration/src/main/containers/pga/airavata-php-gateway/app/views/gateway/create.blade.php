@extends('layout.basic')

@section('page-header')
@parent
@stop

@section('content')

<div class="container">
    <div class="col-md-offset-2 col-md-8">
        <h3>Create a Gateway</h3>

        <form role="form" method="POST" action="{{ URL::to('/') }}/gp/create">
            <div class="form-group required">
                <label class="control-label">Enter Name</label>
                <input class="form-control hostName" maxlength="100" name="gatewayName" required="required"
                       placeholder="Gateway Name"/>
            </div>
            <div class="form-group">
                <label class="control-label">Enter Description</label>
                <textarea class="form-control" maxlength="255" name="gatewayDescription"
                          placeholder="Gateway Description"></textarea>
            </div>
            <div class="form-group">
                <input type="submit" class="btn btn-lg btn-primary" value="Create"/>
                <input type="reset" class="btn btn-lg btn-success" value="Reset"/>
            </div>
        </form>
    </div>
</div>

@stop

@section('scripts')
@parent
{{ HTML::script('js/script.js') }}
@stop