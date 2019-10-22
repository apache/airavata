<!-- high level statistics -->
<div class="high-level-values row tex-center">
    <div class="col-lg-2 col-md-4">
        <div class="panel panel-primary">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->allExperimentCount}}</div>
                        <div>Total Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getAllExperiments" href="#experiment-container">
            <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">All</span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>
    <div class="col-lg-2 col-md-4">
        <div class="panel panel-default">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->createdExperimentCount}}</div>
                        <div>Created Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getCreatedExperiments" href="#experiment-container">
                <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">CREATED VALIDATED &nbsp; &nbsp; &nbsp; &nbsp; </span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>
    <div class="col-lg-2 col-md-4">
        <div class="panel panel-success">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->runningExperimentCount}}</div>
                        <div>Running Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getRunningExperiments" href="#experiment-container">
                <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">SCHEDULED LAUNCHED EXECUTING</span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>
    <div class="col-lg-2 col-md-4">
        <div class="panel panel-green">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->completedExperimentCount}}</div>
                        <div>Successful Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getCompletedExperiments" href="#experiment-container">
            <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">COMPLETED</span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></i></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>

    <div class="col-lg-2 col-md-4">
        <div class="panel panel-yellow">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->cancelledExperimentCount}}</div>
                        <div>Canceled Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getCancelledExperiments" href="#experiment-container">
            <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">CANCELLING CANCELLED</span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></i></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>

    <div class="col-lg-2 col-md-4">
        <div class="panel panel-red">
            <div class="panel-heading">
                <div class="row">
                    <div class="col-xs-3">
                        <i class="fa fa-comments fa-5x"></i>
                    </div>
                    <div class="col-xs-9 text-right">
                        <div class="huge">{{$expStatistics->failedExperimentCount}}</div>
                        <div>Failed Experiments</div>
                    </div>
                </div>
            </div>
            <a id="getFailedExperiments" href="#experiment-container">
            <div class="panel-footer" style="height: 80px">
                    <span class="pull-left">FAILED</span>
<!--                    <span class="pull-right"><span class="glyphicon glyphicon-arrow-right"></span></span>-->

                    <div class="clearfix"></div>
                </div>
            </a>
        </div>
    </div>
</div>

<div id="experiment-container" style="margin: 20px" class="experiment-container"></div>

<script>
    var username = @if ($username) "{{ $username }}" @else null @endif;
    var appname = @if ($appname) "{{ $appname }}" @else null @endif;
    var hostname = @if ($hostname) "{{ $hostname }}" @else null @endif;

    $("#getAllExperiments").click(function () {
        getExperimentsOfStatus( 'ALL');
    });

    $("#getCreatedExperiments").click(function () {
        getExperimentsOfStatus( 'CREATED');
    });

    $("#getRunningExperiments").click(function () {
        getExperimentsOfStatus( 'RUNNING');
    });

    $("#getCompletedExperiments").click(function () {
        getExperimentsOfStatus( 'COMPLETED');
    });

    $("#getCancelledExperiments").click(function () {
        getExperimentsOfStatus( 'CANCELED');
    });

    $("#getFailedExperiments").click(function () {
        getExperimentsOfStatus( 'FAILED');
    });

    function getExperimentsOfStatus( status){
        //These are coming from manage-experiments.blade.php
        $(".experiment-container").html( $(".loading-img").html() );
        $fromTime = $("#datetimepicker9").find("input").val();
        $fromTime = moment($fromTime).utc().format('MM/DD/YYYY hh:mm a');
        $toTime = $("#datetimepicker10").find("input").val();
        $toTime = moment($toTime).utc().format('MM/DD/YYYY hh:mm a');
        if ($fromTime == '' || $toTime == '') {
            alert("Please Select Valid Date Inputs!");
        } else {
            $(".loading-img-statistics").removeClass("hide");
            $.ajax({
                type: 'GET',
                url: "{{URL::to('/')}}/admin/dashboard/experimentsOfTimeRange",
                data: {
                    'status-type': status,
                    'search-key': 'creation-time',
                    'from-date': $fromTime,
                    'to-date': $toTime,
                    'username': username,
                    'appname': appname,
                    'hostname': hostname
                },
                async: false,
                success: function (data) {
                    $(".experiment-container").html(data);
                    //from time-conversion.js
                    updateTime();
                }
            }).complete(function () {
                $(".loading-img-statistics").addClass("hide");
            });
        }
    }

    //element coming from experiment-info blade
    $(document).on("click", ".popover-taskinfo", function(){ 
        $(this).popover();
    });
</script>