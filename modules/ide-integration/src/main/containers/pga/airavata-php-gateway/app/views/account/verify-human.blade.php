@extends('layout.basic')

@section('page-header')
@parent
@stop

@section('content')

<div class="col-md-offset-3 col-md-6">

    <h3>Verify you are human</h3>
    @if ($errors->has())
    @foreach ($errors->all() as $error)
    {{ CommonUtilities::print_error_message($error) }}
    @endforeach
    @endif
    <form role="form" method="POST">
        <div class="form-group form-horizontal">
            <img src="{{$imageUrl}}"/>
            <hr>
            <div>
                <input name="confirmationCode" type="hidden" value="{{$code}}" class="form-control"/>
                <input name="username" type="hidden" value="{{$username}}" class="form-control"/>
                <input name="imagePath" type="hidden" value="{{$imagePath}}" class="form-control"/>
                <input name="secretKey" type="hidden" value="{{$secretKey}}" class="form-control"/>
                <input class="form-control" id="userAnswer" name="userAnswer" required="required"/>
            </div>
        </div>
        <div class="form-group btn-toolbar">
            <div class="btn-group">
                <input type="submit" class="form-control btn btn-primary" value="Submit"/>
            </div>
        </div>
    </form>
</div>
@stop