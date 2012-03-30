<html>

<!-- <script type="text/javascript" src="jquery.js"></script> -->
 <script type="text/javascript" src="jquery/jquery-1.7.1.js"></script>
                <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/jquery-ui.min.js"></script>
                <script type="text/javascript" src="http://jsplumb.org/js/1.3.5/jquery.jsPlumb-1.3.5-all-min.js"></script>
<link rel="stylesheet" href="../js/jquery/development-bundle/themes/base/jquery.ui.all.css">
<link rel="stylesheet" href="../js/jquery/development-bundle/demos/demos.css">
                
	<style>
	#window1, #window2 { width: 150px; height: 150px;  
		border-style: solid;
	}
	</style>
	<script>
	$(function() {
		//$( "#window1" ).draggable();
		//$( "#window2" ).draggable();
	});
	</script>

	
<body>





	<div class="demo">
	
	<div id="window1" class="ui-widget-content" style="position: relative;">
		<p>Window1 content</p>
	</div>
	<div id="window2" class="ui-widget-content" style="position: absolute;">
		<p>Window2 content</p>
	</div>
	</div>
	<script type="text/javascript">
		var targetOption = {
		            isSource:false, 
		            isTarget:true, 
		            endpoint:["Dot", {radius:5}]};
		                
		var sourceOption = {anchor:"BottomCenter",
		            isSource:true, 
		            isTarget:false, 
		            endpoint:["Dot", {radius:5}], 
		            paintStyle:{fillStyle:"#FFEF00"},
		                setDragAllowedWhenFull:true};
		
		jsPlumb.bind("ready", function() {
		
			jsPlumb.addEndpoint('window1', $.extend({container:"window1"},targetOption));
			jsPlumb.addEndpoint('window1', $.extend({container:"window1"},sourceOption));
			
			jsPlumb.addEndpoint('window2', $.extend({container:"window2"},targetOption));
			jsPlumb.addEndpoint('window2', $.extend({container:"window2"},sourceOption));
			
			jsPlumb.draggable('window1');
			jsPlumb.draggable('window2');
		});
	

	</script>
	
	<button type="button" onclick="t()">Display Date</button>
</body>
</html>
