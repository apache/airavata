<?php 
$theme = Theme::uses(Session::get("theme"));
$title = Session::get("portal-title");
?>

@section ('page-header')
@include("layout/fixed-header", array(
                            "title" => $title
                        ))
@show
<style>
/*z index of sidebar is 100.*/
.theme-header{
    position: relative;
    z-index:101;
}
.content-area{
    margin: 20px 0 !important;
}
</style>

<body>

<!--  PGA UI lies here. Do not touch. -->
<style>
.content-area{
    margin:0;
}
</style>
<div class="row content-area">
    @yield('content')
</div>

@show

@section('scripts')
@include('layout/fixed-scripts')
{{ HTML::script('js/time-conversion.js')}}
<script type="text/javascript">
	/* keeping a check that footer stays atleast at the bottom of the window.*/
	var bh = $("html").height();
	if( bh < $(window).height()){
		$(".theme-footer").css("position", "relative").css("top", $(window).height()/4).css("z-index", "-1");
    }
    var bw = $("body").width();
    if( bw > 767){
        $(".hero-unit").height( bw*0.36);
    }

    //put sidebar below all headers in admin dashboards
    if( $(".side-nav").length > 0){
        var headerHeight = $(".pga-header").height() + $(".theme-header").height();
        $(".side-nav").css("top", headerHeight);
        $(".side-nav").affix();
        var selectedDashboardHeight = $(window).height() - headerHeight;
        
        if( selectedDashboardHeight < $(".side-nav").height())
        {
            $(".side-nav").height( selectedDashboardHeight);
        }
    }

    $(".floating").click( function(){
        $('html,body').animate({
            scrollTop: $(".seagrid-info").offset().top},
        'slow');
        $(".seagrid-info").scrollTop( $(window).scrollTop() + 150);
    })

    $(".notif-link").click( function(){
        $.ajax({
            type:"post",
            url:"{{URL::to('/')}}/notice-seen-ack",
            data:{"notice-count": $(".notif-num").data("total-notices")}
        });
        $(".notif-num").addClass("fade");
    })
</script>

<!-- Google Analytics for portal-->

@if( isset( Config::get('pga_config.portal')['google-analytics-id']))

    @if( Config::get("pga_config.portal")["google-analytics-id"] != '')
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

      ga('create', '{{ Config::get("pga_config.portal")["google-analytics-id"] }}', 'auto');
      ga('send', 'pageview');

    </script>
    @endif

@endif
<!-- end google analytics --> 
@show

</body>

</html>