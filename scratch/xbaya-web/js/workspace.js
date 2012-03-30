function initWorkspace(componentPanel, canvasObj){
	workspace=new Object;
	workspace.componentFactories={};
	workspace.components={};
	workspace.canvas=new Array();
	workspace.canvas.push(canvasObj);
	workspace.componentPanel=componentPanel;
	workspace.addComponentFactory=function(factory){
		return addComponentFactory(factory);
		};
	workspace.insertUIElementsToPage=function(parent, x, y,data){
		return insertUIElementsToPage(parent, x, y,data);
		};
	workspace.addPorts=function (component,ports,orientation, isOutput){
		return addPorts(component,ports,orientation, isOutput);
		};
	workspace.getNextComponentId=function(type){
		return getNextComponentId(type);
		};
	workspace.addToComponentPannel=function (url, component_id, componentPanelSection){
		return addToComponentPannel(url, component_id, componentPanelSection);
		};
	workspace.showDialog=function(title, content){
		return showDialog(title, content);
	};
	workspace.removeComponent=function(componentInstance){
		return removeComponent(componentInstance);
	};
	workspace.makeAccordian=function (divId){
		return makeAccordian(divId);
	};
}
function addComponentFactory(factory){
	workspace.componentFactories[factory.type]=factory;
}
function insertUIElementsToPage(parent, x, y,data){
	uiElement=null;
	div = document.createElement('div');
	div.innerHTML = data;
	var elements = div.childNodes;
	var arr = new Array();
	while(elements.length>0){
		ele=elements[0];
		nodeName=ele.nodeName.toLowerCase();
		if (nodeName=="style"){
			document.getElementsByTagName("head")[0].appendChild(ele);
		}else if (nodeName=="script"){
			e=document.createElement("script");
			e.innerHTML=ele.innerHTML;
			arr.push(e);
			div.removeChild(ele);
		}else{
			parent.appendChild(ele);
			if (nodeName=="div"){
				uiElement=ele;
				}
		}
	}
	for(i=0; i < arr.length;i++){
		document.getElementsByTagName("head")[0].appendChild(arr[i]);
	}
	return uiElement;
};
function addPorts(component,ports,orientation, isOutput){
	if (isOutput){
		color="#66FF00";
	}else{
		color="#FFEF00";
	}
    step=1/(Object.keys(ports).length+1);
    i=step;
	jsPlumb.bind("ready", function() {
        var r=5;
        var label_x_value=(isOutput)? -r:r;
        var x=(isOutput)? orientation+0.05:orientation-0.05;
		for(var portId in ports){
			endpointOption = {
	            isSource:isOutput, 
	            isTarget:!isOutput, 
	            endpoint:["Dot", {radius:r}], 
	            paintStyle:{fillStyle:color},
	            setDragAllowedWhenFull:true,
	            /*overlays:[
					[ "Label", {
						location:[label_x_value, 0.5],
						label:portId,
						cssClass:"endpointLabel"
						} ]
					]*/
            };
			
			endpointOption=(isOutput)? $.extend({anchor:[x,i,1,0],maxConnections:-1},endpointOption):$.extend({anchor:[x,i,0,1],maxConnections:1},endpointOption);
			ep=jsPlumb.addEndpoint(component, endpointOption);
			i=i+step;
			}
			jsPlumb.draggable(component);	            
	                
		});
	}
function getNextComponentId(type){
	a=new Array();
	for(id in workspace.components){
		if (workspace.components[id].type==type){
			a.push(id);
			}
		}
	i=1;
	name=type+i;
	while(a.indexOf(name) != -1){
		i=i+1;
		name=type+i;
		}
	return name;
	}

function addToComponentPannel(url, component_id, componentPanelSection){
	$.get(url,  { id: component_id}, function(data) {
		var div = document.createElement('div');
		  div.innerHTML = data;
		  var elements = div.childNodes;
		  //alert(elements.length);
		  var arr = new Array();
		  var leftPanel=componentPanelSection;
		  var divElement=document.createElement("div");
		  var divId=component_id+"_FactoryUI";
		  divElement.setAttribute("id", divId);
		  divElement.setAttribute("class", "componentFactoryIconStyle");
		  leftPanel.appendChild(divElement);			
		  while(elements.length>0){    
		  
			  nodeName=elements[0].nodeName.toLowerCase();
			  if (nodeName=="style"){
				  document.getElementsByTagName("head")[0].appendChild(elements[0]);
			  }else if (nodeName=="script"){
				  e=document.createElement("script");
				  e.innerHTML=elements[0].innerHTML;
				  arr.push(e);
				  div.removeChild(elements[0]);
			  }else{
				  divElement.appendChild(elements[0]);
			  }
        	}
		  for(i=0; i < arr.length;i++){

			  document.getElementsByTagName("head")[0].appendChild(arr[i]);

		  }
		  var factoryObj=workspace.componentFactories[component_id];	        	
		  if (factoryObj!=undefined){
			  factoryObj.draw(divElement,0,0);
			  resizeComponentPanel();
			  $("#"+divId).click(function(){
				  	uniqueId=workspace.getNextComponentId(component_id);//+"_"+(new Date().getTime());
				  	componentInstance=factoryObj.createComponent(uniqueId);
				  	componentInstance.draw(workspace.canvas[0],0,0);
				  	workspace.components[uniqueId]=componentInstance;
					return false;
				  });
			  }	
		});
	}

function resizeComponentPanel(){
	$( "#componentsPanel" ).accordion( "resize" );
	$( ".selector" ).accordion({ autoHeight: false });
}

function makeAccordian(divId){
	$( "#"+divId ).accordion();
}

function showDialog(dialogtitle, content){
	var d=document.createElement('div');
	d.innerHTML=content;
	var $dialog = $(d)
	.html('This dialog will show every time!')
	.dialog({
		autoOpen: false,
		title: dialogtitle,
		buttons: [{text: "Save",click: function() { $(this).dialog("close"); }},
		          {text: "Cancel",click: function() { $(this).dialog("close"); }}]
	});
	$dialog.dialog('open');
}

function removeComponent(componentInstanceId){
	componentInstance=workspace.components[componentInstanceId];
	var divId="#"+componentInstance.uicomponent.id;
	jsPlumb.removeAllEndpoints(componentInstance.uicomponent);
	$(divId).remove();
	delete workspace.components[componentInstanceId];
}