@extends('layout.basic')

@section('page-header')
@parent
@stop

@section('content')

<div class="row">

    <div class="col-md-offset-3 col-md-6">
        <h1>Request an allocation</h1>

	    <form action="{{URL::to('/')}}/allocation-request" method="POST" role="form" enctype="multipart/form-data">

	        <div class="form-group required">
	            <label for="experiment-name" class="control-label">Total Allocation Request in Service Units</label>
	            <input type="text" class="form-control" name=""
	                   placeholder="" autofocus required="required" maxlength="50"/>
	        </div>
	        <div class="form-group">
	            <label for="experiment-description">Total SUs per job ( CPU hours x No. of CPUs )</label>
	            <textarea class="form-control" name="experiment-description" id=""
	                      placeholder="" maxlength="200"></textarea>
	        </div>
	        <div class="form-group required">
	            <label for="project">Maximum memory per CPU</label>
	            <input type="text" class="form-control" name=""
	                   placeholder="" autofocus required="required" maxlength="50"/>


	        </div>
	        <div class="form-group">
	            <label for="application">Disk Usage range ( GB ) per job</label>
	            <input type="text" class="form-control" name=""
	                   placeholder="" autofocus required="required" maxlength="50"/>

	        </div>
	        <div class="form-group">
	            <label for="application">Number of CPUs ( if parallel code is used ) per job</label>
	            <input type="text" class="form-control" name=""
	                   placeholder="" autofocus required="required" maxlength="50"/>
	        </div>
	        <div class="form-group">
	            <label for="experiment-description">Project Reviewed and Funded By (Eg., NSF, NIH, DOD, DOE, None etc...)</label>
	            <textarea class="form-control" name="experiment-description" id="experiment-description"
	                      placeholder="" maxlength="200"></textarea>
	        </div>
	        <div class="form-group">
	            <label for="experiment-description">Brief Project Description (Hypothesis, Model Systems, Methods, and Analysis)</label>
	            <textarea class="form-control" name="experiment-description" id="experiment-description"
	                      placeholder="" maxlength="200"></textarea>
	        </div>
	        <div class="btn-toolbar">
	            <input name="continue" type="submit" class="btn btn-primary" value="Submit Request">
	            <input name="clear" type="reset" class="btn btn-default" value="Reset values">
	        </div>
	    </form>

	</div>
</div>


@stop

@section('scripts')
@parent
<script>
</script>
@stop
