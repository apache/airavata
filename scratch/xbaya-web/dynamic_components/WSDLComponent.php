<?php
	include '../utils/urlutils.php';
	include '../utils/wsdlparser.php';
	$url=urldecode($_GET["wsdlurl"]);
// 	$url="http://localhost/poc/test/Calculator.wsdl";
	$opid=$_GET["opid"];
?>
<script>
	<?php
		$parser=new WSDLParser($url);
		foreach ($parser->getOperations() as $ops){
			$id=$parser->getServiceName()."_".$ops->getFunctionName();
			if ($id==$opid){
				$ui_url=attachToCurrentURLInPath("WSDLComponentUI.php");
				$ui_factory_url=attachToCurrentURLInPath("WSDLComponentFactoryUI.php");
				?>
				function <?php echo $id?>Component(id, type){
					var objSelf = this;
					this.id=id;
					this.uicomponent=null;
					this.type=type;
					this.parent=null;
					this.ui_url="<?php echo $ui_url?>";
					this.getInputs=function(){
						parameters={};
						<?php 
							$paras=$parser->getPrimitiveTypes($ops->getParameters());
							foreach($paras as $key=>$value){
								echo "parameters['".$key."']={type:'".$value."'};\n";		
							}
						?>
						return parameters;
						};
					this.getOutputs=function(){
						parameters={};
						<?php 
							$paras=$parser->getPrimitiveTypes($ops->getReturnType());
							foreach($paras as $key=>$value){
								echo "parameters['"."result"."']={type:'".$ops->getReturnType()."'};\n";
							}
						?>
						return parameters;
						};
					this.draw=function(parent, x, y){
						$.get(objSelf.ui_url,  { id: objSelf.id, caption:"<?php echo $ops->getFunctionName()?>"}, function(data) {
								objSelf.uicomponent=workspace.insertUIElementsToPage(parent,x,y,data);
								workspace.addPorts(objSelf.uicomponent,objSelf.getInputs(),0,false);
								workspace.addPorts(objSelf.uicomponent,objSelf.getOutputs(),1,true);
							});
						};
					this.serialize=function(){
						return null;
						};
				}
				function <?php echo $id?>ComponentFactory(type){
					var objSelf = this;
					this.ui_factory_url="<?php echo $ui_factory_url?>";
					this.type=type;
					this.uicomponent=null;
					this.createComponent=function(id){
						component=new <?php echo $id?>Component(id,objSelf.type);
						return component;
						};
					this.getLabel=function(){
						return "<?php echo $id?>";
						};
					this.getDescription=function(){
						return "My WSDL Component description";
						};
					this.draw=function(parent,x,y){
						$.get(objSelf.ui_factory_url,  { type: objSelf.type, caption:"<?php echo $ops->getFunctionName()?>"}, function(data) {
							objSelf.uicomponent=workspace.insertUIElementsToPage(parent,x,y,data);
							});
						};
				}
				workspace.addComponentFactory(new <?php echo $id?>ComponentFactory("<?php echo $id ?>"));
				<?php 
			}
		}
	?>
	
</script>
