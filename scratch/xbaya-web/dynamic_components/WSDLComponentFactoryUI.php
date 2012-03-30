<?php
	$type=$_GET["type"];
	$caption=$_GET["caption"]
?>
<style>
.component_factory_style_<?php echo $type?>{
	height:80%;
	width:80%;
	text-align:center;
	font-family: "Trebuchet MS","Arial","Helvetica","Verdana","sans-serif";
}
</style>
<div class="component_factory_style_<?php echo $type?>">
<?php echo $caption ?>
</div>