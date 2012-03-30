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
	left: <?php echo $x?>px;
	top: <?php echo $y?>px; 
	padding: 0em;
	border-style: none;
}
#<?php echo $id ?>_icon{
	height: 100%;
	text-align: center;
	float:none;
	vertical-align: middle;
}
</style>

<div id="<?php echo $id ?>" class="ui-widget-content" style="position: absolute; text-align: center; padding: 0em">
	<div style="height: 100%; width:100%; vertical-align: middle; text-align: center;">
		<img id="<?php echo $id ?>_icon" src="components/logic/xor_gate.png" />
		<!-- <p class="windowtitle"><?php echo $id ?></p>-->
	</div>
	
	<!--div style="height:100%:position:relative; text-align: right;vertical-align: bottom; padding:0em;">
		<a id="<?php echo $id ?>_configure" href="#" onclick="workspace.showDialog('Configure <?php echo $id ?>','Setting blah blah');" class="componentConfiguration"><img src="http://www.clker.com/cliparts/3/e/e/d/1194994517471096801configure.svg.med.png" style="width:12px;height:12px"></img>Configure</a>
		<a id="<?php echo $id ?>_delete" onclick="workspace.removeComponent('<?php echo $id ?>')" href="#" class="componentConfiguration"><img src="http://cdn1.iconfinder.com/data/icons/softwaredemo/PNG/128x128/DeleteRed.png" style="width:12px;height:12px"></img>Delete</a>
	</div-->
</div>
