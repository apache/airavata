@extends('layout.basic')

@section('content')
<?php
    $theme = Theme::uses( Session::get("theme"));
    echo $theme->partial( $page);
?>
@stop
