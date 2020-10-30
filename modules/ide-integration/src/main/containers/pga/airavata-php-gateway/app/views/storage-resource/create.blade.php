@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/admin.css')}}
{{ HTML::style('css/datetimepicker.css')}}
@stop

@section('content')

<div id="wrapper">
    <!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
    @include( 'partials/dashboard-block')
    <div id="page-wrapper">

<div class="container-fluid">
    <div class="col-md-offset-2 col-md-8">
        <h3>Create a Resource</h3>

        <form role="form" method="POST" action="{{ URL::to('/') }}/sr/create">
            <div class="form-group required">
                <label class="control-label">Host Name</label>
                <input class="form-control hostName" maxlength="100" name="hostname" required="required"/>
            </div>
            <div class="form-group">
                <label class="control-label">Stoage Resource Description</label>
                <textarea class="form-control" maxlength="255" name="description"></textarea>
            </div>
            <div class="form-group">
                <input type="submit" class="btn btn-lg btn-primary" name="step1" value="Create"/>
                <input type="reset" class="btn btn-lg btn-success" value="Reset"/>
            </div>
        </form>
    </div>
</div>
</div>
</div>
@stop

@section('scripts')
@parent
{{ HTML::script('js/script.js') }}
@stop