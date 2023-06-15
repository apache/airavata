<div class="collapse navbar-collapse navbar-ex1-collapse" >
    <ul class="nav navbar-nav side-nav" data-spy="affix" >
        <li
            @if( Session::has("admin-nav") && Session::get("admin-nav") == "exp-statistics") class="active" @endif>
            <a class="dashboard-link"  href="{{ URL::to('/')}}/admin/dashboard/experiments">
                <span class="glyphicon glyphicon-off"></span>&nbsp; Experiment Statistics
            </a>
        </li>

        <li
        @if( Session::has("admin-nav") && Session::get("admin-nav") == "manage-users") class="active" @endif>
            <a class="dashboard-link" href="{{ URL::to('/')}}/admin/dashboard/users"><span class="glyphicon glyphicon-user"></span>&nbsp; Users</a>
        </li>
        <li>
            <a><span class="glyphicon glyphicon-briefcase"></span>&nbsp; Compute Resources</a>
            <ul>
                @if(Session::has("super-admin"))
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "cr-create") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/cr/create"><i class="fa fa-fw fa-table"></i>Register</a>
                </li>
                @endif
                @if(Session::has("admin") || Session::has("admin-read-only"))
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "cr-browse") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/cr/browse"><i class="fa fa-fw fa-table"></i>Browse</a>
                </li>
                @endif
            </ul>

        </li>

        <li>
            <a><span class="glyphicon glyphicon-folder-open"></span>&nbsp; Storage Resources</a>
            <ul>
                @if(Session::has("super-admin"))
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "sr-create") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/sr/create"><i class="fa fa-fw fa-table"></i>Register</a>
                </li>
                @endif
                @if(Session::has("admin") || Session::has("admin-read-only"))
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "sr-browse") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/sr/browse"><i class="fa fa-fw fa-table"></i>Browse</a>
                </li>
                @endif
            </ul>

        </li>

        <li>
            <a><span class="glyphicon glyphicon-tasks"></span>&nbsp; App Catalog</a>
            <ul>
                @if(Session::has("admin") || Session::has("admin-read-only"))
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "app-module") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/app/module"><i class="fa fa-fw fa-table"></i>Module</a>
                </li>
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "app-interface") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/app/interface"><i class="fa fa-fw fa-table"></i>Interface</a>
                </li>
                <li
                @if( Session::has("admin-nav") && Session::get("admin-nav") == "app-deployment") class="active" @endif>
                    <a class="dashboard-link" href="{{ URL::to('/')}}/app/deployment"><i class="fa fa-fw fa-table"></i>Deployment</a>
                </li>
                @endif
            </ul>

        </li>

        <li
            @if( Session::has("admin-nav") && Session::get("admin-nav") == "gateway-prefs") class="active" @endif>
            <a class="dashboard-link" href="{{ URL::to('/')}}/admin/dashboard/gateway"><span class="glyphicon glyphicon-sort"></span>&nbsp;
                Gateway Management</a>
        </li>

        <li
            @if( Session::has("admin-nav") && Session::get("admin-nav") == "credential-store") class="active" @endif>
            <a class="dashboard-link" href="{{ URL::to('/')}}/admin/dashboard/credential-store"><span class="glyphicon glyphicon-lock"></span>&nbsp; Credential
                Store</a>
        </li>
        <li
            @if( Session::has("admin-nav") && Session::get("admin-nav") == "notices") class="active" @endif>
            <a class="dashboard-link" href="{{ URL::to('/')}}/admin/dashboard/notices">
                <span class="glyphicon glyphicon-bell"></span> Notices</a>
        </li>
<!--        <li>-->
<!--            <a href="forms.html"><i class="fa fa-fw fa-edit"></i> Settings</a>-->
<!--        </li>-->
        <!--
        <li>
            <a href="bootstrap-elements.html"><i class="fa fa-fw fa-desktop"></i> Bootstrap Elements</a>
        </li>
        <li>
            <a href="bootstrap-grid.html"><i class="fa fa-fw fa-wrench"></i> Bootstrap Grid</a>
        </li>
        <li>
            <a href="javascript:;" data-toggle="collapse" data-target="#demo"><i class="fa fa-fw fa-arrows-v"></i> Dropdown <i class="fa fa-fw fa-caret-down"></i></a>
            <ul id="demo" class="collapse">
                <li>
                    <a href="#">Dropdown Item</a>
                </li>
                <li>
                    <a href="#">Dropdown Item</a>
                </li>
            </ul>
        </li>
        <li>
            <a href="blank-page.html"><i class="fa fa-fw fa-file"></i> Blank Page</a>
        </li>
        <li>
            <a href="index-rtl.html"><i class="fa fa-fw fa-dashboard"></i> RTL Dashboard</a>
        </li>
        -->
    </ul>
</div>
