<script type="text/javascript">
    function getUrlVars() {
        var vars = {};
        var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
            vars[key] = value;
        });
        return vars;
    }

    var gatewayName = getUrlVars()["gatewayName"];
    var portalUserName = getUrlVars()["portalUserName"];
    var lifetime = getUrlVars()["lifetime"];

    alert(gatewayName);
    alert(portalUserName);
    alert(lifetime);


</script>
<html>
<body>
<h1>Credential Store</h1>
<p>An error occurred while processing</p>
</body>
</html>
