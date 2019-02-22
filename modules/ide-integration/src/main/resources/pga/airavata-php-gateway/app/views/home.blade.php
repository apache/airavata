@extends('layout.basic')

@section('content')

<?php

if (CommonUtilities::id_in_session()) {

    if (Session::has("admin"))
        $admin = " Admin";
    else
        $admin = "";

    echo '<div class="well"><div class="container"><h4>Welcome' . $admin . ', ' . Session::get("username") . '!</h4></div></div>';
}

?>

<?php 
if( Session::get("theme") == "base"){?>
<div class="well">
    <div class="container">
        <h1>PHP Gateway with Airavata</h1>
        <p>
            PGA is a science gateway built with the Airavata API. You can reference PGA as you integrate Airavata
            into your own gateway, or you can create your gateway on top of PGA by cloning it at the link below.
            PGA is known to work well in the Chrome, Firefox, and Internet Explorer browsers.
        </p>
        <p><a href="https://github.com/apache/airavata-php-gateway"
                target="_blank">See the code <span class="glyphicon glyphicon-new-window"></span></a></p>
        <p><a href="https://cwiki.apache.org/confluence/display/AIRAVATA/PEARC+2017+Tutorials"
            target="_blank">PEARC 2017 tutorial documentation <span class="glyphicon glyphicon-new-window"></span></a>
        </p>
    </div>
</div>


<div class="container">

    <div class="row">

        <div class="col-md-6">
            <div class="thumbnail" style="border:none">
                <img src="assets/scigap-header-logo.png" alt="SciGaP">

                <div class="caption">
                    <p>
                        SciGaP is a hosted service with a public API that science gateways can use to manage
                        applications and workflows running on remote supercomputers, as well as other services. Gateway
                        developers can thus concentrate their efforts on building their scientific communities and not
                        worry about operations.
                    </p>

                    <p>
                        Science Gateway Platform as a Service (SciGaP) provides application programmer interfaces (APIs)
                        to hosted generic infrastructure services that can be used by domain science communities to
                        create Science Gateways.
                    </p>

                    <p><a href="http://scigap.org/"
                          target="_blank">Learn more <span class="glyphicon glyphicon-new-window"></span></a></p>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="thumbnail" style="border:none">
                <img src="assets/powered-by-airavata-transparent.png" width="260px" alt="Apache Airavata">

                <div class="caption">
                    <p>
                        Apache Airavata is a software framework which is dominantly used to build Web-based science
                        gateways and assist to compose, manage, execute and monitor large scale applications and
                        workflows on distributed computing resources such as local clusters, supercomputers, national
                        grids, academic and commercial clouds. Airavata mainly supports long running applications and
                        workflows on distributed computational resources.
                    </p>

                    <p><a href="http://airavata.apache.org/" target="_blank">Learn more <span
                                class="glyphicon glyphicon-new-window"></span></a></p>
                </div>
            </div>
        </div>

        <div class="col-md-12">
    <div class="row">
        <div id="contact-scigap" class="col-md-10 col-md-offset-1 text-center breathing-spaces">
            <h3>Contact Us</h3>
            <div class="col-md-6">
                <span class="glyphicon glyphicon-envelope" style="font-size:6em;"></span><br/>
            You can contact Gateway Admin by sending a mail to <a href="mailto:psd@scigap.atlassian.net">Contact E-mail</a>
            </div>
            <div class="col-md-6">
                <span class="glyphicon glyphicon-edit" style="font-size:6em;"></span><br/>
                You can also create a <span id="serviceDeskHelp">JIRA ticket</span> by signing in <a href="https://scigap.atlassian.net/servicedesk/customer/portal/8" target="_blank">here</a>.
            </div>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="serviceDeskHelp" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Modal title</h4>
      </div>
      <div class="modal-body">
        Hello
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary">Save changes</button>
      </div>
    </div>
  </div>
</div>


    </div>



</div>

<?php 
}
else
{
    $theme = Theme::uses( Session::get("theme"));
    echo $theme->partial("template");
}
?>
@stop

