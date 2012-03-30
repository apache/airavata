<?php 
	include "utils/fileutils.php"
?>
<html>
<head>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/workspace.js"></script>
<link rel="stylesheet" href="css/main.css">
</head>
<body>
	<div id="componentsPanel" style="width: 20%; height: 100%; border: 2; border-color: black; border-style: solid;float: left;position:relative;">
		<?php 
			$panels=getSettings("components/componentPanels.txt");
			foreach($panels as &$panel){
				echo "<h3><a href='#'>".$panel['label'].'<input type="submit" title="test" onclick="alert(234)" />'."</a></h3>";
				echo "<div id='".$panel['id']."' style='padding:0.5em;'>";
				echo "</div>";
			}
		?>
	</div>
	<div id="canvas" style="position:relative; overflow:auto;width: 75%; height: 100%; border: 2; border-color: black; border-style: solid;float: right;"></div>
	<script type="text/javascript">
		initWorkspace(document.getElementById("componentsPanel"),document.getElementById("canvas"));
		<?php 
			foreach($panels as &$panel){
				$components=getSettings($panel['component_list']);
// 				echo $panel['component_list'];
				foreach ($components as &$component) {
						$ui_id=$component['id']."_Factory_UIContainer";
						echo "\t\tworkspace.addToComponentPannel('".$component['controller']."','".$component['id']."',$('#".$panel['id']."')[0]);\n";
					}
				}
			?>
			url="http://localhost/poc/dynamic_components/WSDLComponent.php?wsdlurl="+encodeURIComponent("http://localhost/poc/test/Calculator.wsdl")+"&opid=Calculator_add";
			workspace.addToComponentPannel(url,"Calculator_add",$('#<?php echo $panels[0]['id']?>')[0]);
		$(function() {
			workspace.makeAccordian("componentsPanel");
		});
		window.onresize = function(event) {
			resizeComponentPanel();
		};
				
	</script>
</body>
</html>
