<title>PGA for Science Gateways</title>

<link media="all" type="text/css" rel="stylesheet" href="{{ URL::to('/') }}/themes/{{Session::get('theme')}}/assets/css/style.css"/>

    <nav class="nav navbar-default">

        <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header page-scroll" id="home">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand page-scroll" href="{{ URL::to('/') }}/home">
                PHP Gateway
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav navbar-right">
                <li class="hidden">
                    <a href="#page-top"></a>
                </li>
                <li>
                    <a class="page-scroll" href="{{URL::to('/')}}/pages/documentation">Documentation</a>
                </li>
                <li>
                    <a class="page-scroll" href="{{URL::to('/')}}/pages/about">About</a>
                </li>
                <li>
                    <a class="page-scroll" href="{{URL::to('/')}}/#contact-scigap">Contact</a>
                </li>
                <!--
                <li>
                    <a class="page-scroll btn btn-primary" href="#collaborators">Get Access</a>
                </li>
                -->
            </ul>
        </div>
        <!-- /.navbar-collapse -->
    </div>
</nav>