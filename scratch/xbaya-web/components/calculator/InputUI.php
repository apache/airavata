<?php 
	$id=$_GET["id"];
	$x=0;
	$y=0;
// 	$x=$_GET["x"];
// 	$y=$_GET["y"];
	$x=($x==null)? 0:$x;
	$y=($y==null)? 0:$y;
?>
<style>
#<?php echo $id ?> {
	width: 100px;
	height: 30px;
	left: <?php echo $x?>px;
	top: <?php echo $y?>px; 
	padding: 0.5em;
}
#<?php echo $id ?>_icon{
	height: 80%;
	float:right;
}
</style>

<div id="<?php echo $id ?>" class="ui-widget-content" style="position: absolute;">
	<img id="<?php echo $id ?>_icon" src="http://www.clker.com/cliparts/2/6/9/f/12422392611935626017Circle-n.svg.med.png">
	<div style="height:100%:position:relative; float: left;text-align: left;vertical-align: bottom; padding:0em;">
		<a id="<?php echo $id ?>_configure" href="#" onclick="workspace.showDialog('Configure <?php echo $id ?>','Setting blah blah');" class="componentConfiguration"><img src="http://www.clker.com/cliparts/3/e/e/d/1194994517471096801configure.svg.med.png" style="width:12px;height:12px"></img>Configure</a><br/>
		<a id="<?php echo $id ?>_delete" onclick="workspace.removeComponent('<?php echo $id ?>')" href="#" class="componentConfiguration"><img src="http://cdn1.iconfinder.com/data/icons/softwaredemo/PNG/128x128/DeleteRed.png" style="width:12px;height:12px"></img>Delete</a>
	</div>
</div>
