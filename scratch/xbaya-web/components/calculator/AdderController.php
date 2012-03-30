<?php
	include '../../utils/urlutils.php';
	$id=$_GET["id"];
	$ui_url=attachToCurrentURLInPath("AdderUI.php");
	$ui_factory_url=attachToCurrentURLInPath("AdderFactoryUI.php");
?>
<script>
	
	function AdderComponent(id, type){
		var objSelf = this;
		this.id=id;
		this.uicomponent=null;
		this.type=type;
		this.parent=null;
		this.ui_url="<?php echo $ui_url?>";
		this.getInputs=function(){
			parameters={};
			parameters["number1"]={type:"int"};
			parameters["number2"]={type:"int"};
			return parameters;
			};
		this.getOutputs=function(){
			parameters={};
			parameters["sum"]={type:"int"};
			return parameters;
			};
		this.draw=function(parent, x, y){
			$.get(objSelf.ui_url,  { id: objSelf.id}, function(data) {
					objSelf.uicomponent=workspace.insertUIElementsToPage(parent,x,y,data);
					workspace.addPorts(objSelf.uicomponent,objSelf.getInputs(),0,false);
					workspace.addPorts(objSelf.uicomponent,objSelf.getOutputs(),1,true);
				});
			};
		this.serialize=function(){
			return null;
			};
	}
	function AdderComponentFactory(type){
		var objSelf = this;
		this.ui_factory_url="<?php echo $ui_factory_url?>";
		this.type=type;
		this.uicomponent=null;
		this.createComponent=function(id){
			component=new AdderComponent(id,objSelf.type);
			return component;
			};
		this.getLabel=function(){
			return "Add";
			};
		this.getDescription=function(){
			return "My Component description";
			};
		this.draw=function(parent,x,y){
			$.get(objSelf.ui_factory_url,  { type: objSelf.type}, function(data) {
				objSelf.uicomponent=workspace.insertUIElementsToPage(parent,x,y,data);
				});
			};
	}
	workspace.addComponentFactory(new AdderComponentFactory("<?php echo $id ?>"));
</script>
