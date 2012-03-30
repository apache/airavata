<!doctype html>
<html>
        <head>
                <title>Example</title>
                <meta http-equiv="content-type" content="text/html;charset=utf-8" />    
            <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
                <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.13/jquery-ui.min.js"></script>
                <script type="text/javascript" src="http://jsplumb.org/js/1.3.1/jquery.jsPlumb-1.3.1-all-min.js"></script>
        </head>
        <body >
                <div id="block1" style="position: absolute;">Block1</div>
                <div id="block2" style="position: absolute;">Block2</div>
                        
                        
                <script type="text/javascript">
                
                var targetOption = {anchor:"TopCenter",
                                                    maxConnections:-1, 
                                                    isSource:false, 
                                                    isTarget:true, 
                                                    endpoint:["Dot", {radius:5}], 
                                                    paintStyle:{fillStyle:"#66FF00"},
                                                        setDragAllowedWhenFull:true}
                                                        
                var sourceOption = {anchor:"BottomCenter",
                                                        maxConnections:-1, 
                                                    isSource:true, 
                                                    isTarget:false, 
                                                    endpoint:["Dot", {radius:5}], 
                                                    paintStyle:{fillStyle:"#FFEF00"},
                                                        setDragAllowedWhenFull:true}
                
                jsPlumb.bind("ready", function() {
                        
                        jsPlumb.addEndpoint('block1', targetOption);
                        jsPlumb.addEndpoint('block1', sourceOption);
                        
                        jsPlumb.addEndpoint('block2', targetOption);
                        jsPlumb.addEndpoint('block2', sourceOption);
                        
                        jsPlumb.draggable('block1');
                        jsPlumb.draggable('block2');
                });
                
                </script>

        </body>
</html>
