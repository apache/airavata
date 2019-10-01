@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/filemanager.css')}}

@stop

@section('content')

    <div class="container">
        <div class="center-content">
            <br><br><br>
            <div class="well-sm" id="tools">
                <a class="btn btn-default" id="refresh-button"><i class="icon-refresh"></i> Refresh</a>
            </div>

            <!-- breadcrumb -->
            <ol class="breadcrumb" id="breadcrumb"></ol>
            <!-- file manager view -->
            <div class="input-group"> <span class="input-group-addon">Filter</span>
                <input id="filter-text" type="text" class="form-control" placeholder="Search Here...">
            </div>
            <br/>
            <table class="table table-hover table-condensed" id="filemanager"></table>

            <!-- message box -->
            <div id="msgbox"></div>

        </div>
    </div>

@stop

@section('scripts')
@parent
{{ HTML::script('js/filemanager.js')}}

<script type="text/javascript">

    $(document).ready(function () {
        var PATH = "{{$path}}";
        browse(PATH);
    });

</script>
@stop