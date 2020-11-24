@extends('layout.basic')

@section('page-header')
@parent
{{ HTML::style('css/datetimepicker.css')}}

@stop

@section('content')

<div class="container" style="max-width: 80%;">
    <form action="{{URL::to('/')}}/experiment/browse" method="post" class="form-inline" role="form">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3>Search for Experiments</h3>
            </div>
            <div class="panel-body">
                <div class="form-group">
                    <label for="search-key">Search by</label>
                    <select class="form-control" name="search-key" id="search-key">
                        <?php

                        // set up options for select input
                        $values = array('experiment-name', 'experiment-description', 'application', 'creation-time');
                        $labels = array('Experiment Name', 'Experiment Description', 'Application', 'Creation Time');
                        $disabled = array('', '', '', '');

                        ExperimentUtilities::create_options($values, $labels, $disabled);

                        ?>
                    </select>
                </div>

                <div class="form-group search-text-block">
                    <label for="search-value">for</label>
                    <input type="search" class="form-control" name="search-value" id="search-value" placeholder="value"
                           value="<?php if (isset($_POST['search-value'])) echo $_POST['search-value'] ?>">
                </div>

                <select name="status-type" class="form-control select-status">
                    <option value="ALL">Status</option>
                    <?php
                    foreach ($expStates as $index => $state) {
                        if (isset($input) && isset($input["status-type"]) && $state == $input["status-type"]) {
                            echo '<option value="' . $state . '" selected>' . $state . '</option>';
                        } else {
                            echo '<option value="' . $state . '">' . $state . '</option>';
                        }
                    }
                    ?>
                </select>

                <div class="container select-dates hide">
                    <div class="col-md-12">
                        Select dates between which you want to search for experiments.
                    </div>
                    <div class="col-sm-8" style="height:75px;">
                        <div class='col-md-6'>
                            <div class="form-group">
                                <div class='input-group date' id='datetimepicker9'>
                                    <input type='text' class="form-control" placeholder="From Date" name="from-date"
                                           value="<?php if (isset($_POST['from-date'])) echo $_POST['from-date'] ?>"/>
                        <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span>
                        </span>
                                </div>
                            </div>
                        </div>
                        <div class='col-md-6'>
                            <div class="form-group">
                                <div class='input-group date' id='datetimepicker10'>
                                    <input type='text' class="form-control" placeholder="To Date" name="to-date"
                                           value="<?php if (isset($_POST['to-date'])) echo $_POST['to-date'] ?>"/>
                        <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span>
                        </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <button name="search" type="submit" class="btn btn-primary pull-right" value="Search"><span
                        class="glyphicon glyphicon-search"></span> Search
                </button>
                <p class="help-block">You can use * as a wildcard character. Tip: search for * alone to retrieve all of your
                    experiments.</p>

            </div>
        </div>

        <!-- Pagination Handling -->
        <?php
        if (isset($expContainer)) {
            ?>
            <div class="pull-right btn-toolbar" style="padding-bottom: 5px">
                <?php
                if ($pageNo != 1) {
                    echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="prev" value="Previous"/>';
                }
                if (sizeof($expContainer) > 0) {
                    echo '<input class="btn btn-primary btn-xs" type="submit" style="cursor: pointer" name="next" value="Next"/>';
                }
                ?>
            </div>
            <div class="pull-left">
                <?php if (sizeof($expContainer) != 0) echo 'Showing experiments from ' . strval(($pageNo - 1) * $limit + 1)
                    . ' to ' . strval(min($pageNo * $limit, ($pageNo - 1) * $limit + sizeof($expContainer))); ?>
            </div>
            <input type="hidden" name="pageNo" value="<?php echo($pageNo) ?>"/>
            <div style="clear: both"></div>
        <?php
        }
        ?>
    </form>

    @include('partials/experiment-container')

</div>

@stop

@section('scripts')
@parent
{{ HTML::script('js/moment.js')}}
{{ HTML::script('js/datetimepicker-3.1.3.js')}}

<script type="text/javascript">

    $(document).ready(function () {

//------------------------Commenting Client Side filtering--------------------------------------
//            /* script to make status select work on the UI side itself. */
//
//            $(".select-status").on("change", function(){
//                selectedStatus = this.value;
//
//                if( selectedStatus == "ALL")
//                {
//                    $("table tr").slideDown();
//                }
//                else
//                {
//                    $("table tr").each(function(index) {
//                        if (index != 0) {
//
//                            $row = $(this);
//
//                            var status = $.trim( $row.find("td:last").text() );
//                            if (status == selectedStatus )
//                            {
//                                $(this).slideDown();
//                            }
//                            else {
//                                $(this).slideUp();
//                            }
//                        }
//                    });
//                }
//            });

        /* making datetimepicker work for exp search */

        $('#datetimepicker9').datetimepicker({
            pick12HourFormat: false
        });
        $('#datetimepicker10').datetimepicker({
            pick12HourFormat: false
        });
        $("#datetimepicker9").on("dp.change", function (e) {
            $('#datetimepicker10').data("DateTimePicker").setMinDate(e.date);

            //hack to close calendar on selecting date
            $(this).find(".glyphicon-calendar").click();
        });
        $("#datetimepicker10").on("dp.change", function (e) {
            $('#datetimepicker9').data("DateTimePicker").setMaxDate(e.date);

            //hack to close calendar on selecting date
            $(this).find(".glyphicon-calendar").click();
        });

        /* selecting creation time */
        $("#search-key").on("change", function () {
            if (this.value == "creation-time") {
                $(".search-text-block").addClass("hide");
                $(".select-dates").removeClass("hide");
                $("#search-value").removeAttr("required");

            }
            else {
                $(".search-text-block").removeClass("hide");
                $(".select-dates").addClass("hide");
                $("#search-value").attr("required");
            }
        });

        changeInputVisibility($("#search-key").val());

    });

    function changeInputVisibility(selectedStatus) {
        if (selectedStatus == "creation-time") {
            $(".search-text-block").addClass("hide");
            $(".select-dates").removeClass("hide");
            $("#search-value").removeAttr("required");

        }
        else {
            $(".search-text-block").removeClass("hide");
            $(".select-dates").addClass("hide");
            $("#search-value").attr("required");
        }
    }
</script>
@stop