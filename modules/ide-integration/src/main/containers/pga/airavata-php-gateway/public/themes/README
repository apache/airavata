Theme Documentation ( Read before starting to work on your themes)
------------

Steps to create your own theme:

1) First thing first, PGA themes use Bootstrap 3.x and Blade as its templating engine as a base for all UI Development. The capability to let users use another UI framework is being worked upon and would be available in the future. If users still want to use another UI framework, they could add it in theme header/footer and it will act as the overwriting entity on anything which Bootstrap has a hold on.

2) Copy <pga-root-folder>/public/themes/base folder in the same 'themes' folder and rename it to your theme. Lets call this folder 'theme1' for the rest of the steps.

3) PGA Theme structure:

Below is how all UIs are layered in a theme. 

 ----------------------------------
| THEME HEADER                     |
 ----------------------------------
| PGA HEADER                       |
 ----------------------------------
| PGA BODY (main content)          |
 ----------------------------------
| PGA FOOTER                       |
 ----------------------------------
| THEME FOOTER                     |
 ----------------------------------

Folder 'Theme1' contains 4 folders:

- Assets:             All styling, scripts and image files can be kept here and accessed in theme pages using- 
{{ URL::to('/') }}/themes/{{Session::get('theme')}}/assets/<filename.css>
Users can create additional folders inside 'assets' for structuring purposes like css, js, img files.
- Partials:           All HTML blocks and pages which should be included in a theme can be added here. By default, this folder contains header.blade.php, footer.blade.php and template.blade.php and should NOT be deleted.
- Layouts and Views:  Additional folders to structure extra theme components and plugin layouts if any.



THEME HEADER:
------------
This is the space where Theme creaters can add their theme headers which would
generally contain styles or links to styles, global navigation for a Scientific gateway having logo/s, links etc which would show across all pages on PGA.

All content for Theme Header can be added in theme1/partials/header.blade.php.

Additional pages other than the landing page can be created and linked to THEME header. The way to do that is add the page content to 'theme1/partials/<page1.blade.php>' and can be referenced/linked from anywhere inside the theme pages with the link: '{{URL::to('/')}}/pages/page1' ( blade.php does not need to be written).

Sample Theme Header content: 
-----------------------------------
<title>Theme1</title>

<link href='http://fonts.googleapis.com/css?family=Lato:100,300,400,700,900,100italic,300italic,400italic,700italic,900italic' rel='stylesheet' type='text/css'>
<link href='http://fonts.googleapis.com/css?family=Arvo:400,700' rel='stylesheet' type='text/css'>

<link media="all" type="text/css" rel="stylesheet" href="{{ URL::to('/') }}/themes/{{Session::get('theme')}}/assets/css/style.css"/>

<div class="container-fluid">
    <nav class="nav navbar-default navbar-inverse">
        <!-- Brand and toggle get grouped for better mobile display -->
    <!--    <div class="navbar-header page-scroll" id="home">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand page-scroll" href="{{ URL::to('/') }}/home">
                THEME 1
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav navbar-right">
                <li>
                    <a href="{{URL::to('/')}}/pages/documentation">Documentation</a>
                </li>
                <li>
                    <a href="{{URL::to('/')}}/pages/publications">Publications</a>
                </li>
                <li>
                    <a href="{{URL::to('/')}}/pages/about">About</a>
                </li>
                <li>
                    <a href="{{URL::to('/')}}/pages/contact">Contact</a>
                </li>
            </ul>
        </div>
        <!-- /.navbar-collapse -->
<!--</nav>
</div>
-----------------------------------

PGA HEADER and PGA FOOTER:
------------
All layout files which help PGA dashboard to run smoothly exist in this layer and should NOT be changed. Any changes done to these files will get removed on updating PGA. 

PGA header contains a default navbar which helps to navigate throughout user dashboard. A simple capability to remove this navbar before users log in, exists in pga_config.php where they can set 'theme-based-login-links-configured' to true.
Before setting this property to true, please ensure that Login and Register links are configured somewhere in THEME HEADER.
Login Link:     {{URL::to('/')}}/login
Register Link:  {{URL::to('/')}}/register

PGA footer mainly includes scripts needed in PGA.

Although, Theme Users can add their Brand logo on the left of the PGA Navbar. 
Simply add an image as background of class 'brand-logo'.
Put the image in assets folder and reference it from your custom styles file.

Example css inside custom style file ( assuming it is in assets/css/style.css and image is at assets/img/mylogo.png):

.brand-logo{
    background: url("../img/mylogo.png");
}

PGA BODY:
------------
Theme landing page content should be added to  theme1/partials/template.blade.php.

Any other pages ( outside of PGA Dashboard) can also be created inside theme1/partials/<page1.blade.php> and can be referenced/linked from anywhere inside the theme pages with the link: '{{URL::to('/')}}/pages/page1' ( blade.php does not need to be written). This helps to add global links to THEME HEADER like About, Contact, Documentation etc.


THEME FOOTER:
------------
Global theme footer can be added to THEME FOOTER layer. It is recommended that all script files and/or their links are added in Theme footer instead of Header unless necessary.

Since THEME FOOTER is the last layer, it helps to overwrite any scripts included before by PGA HEADER/PGA FOOTER. 

All content for Theme Footer can be added in theme1/partials/footer.blade.php.
