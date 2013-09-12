<%@ page import="org.apache.airavata.sample.gateway.SampleGateway" %><%
    session.removeAttribute("userName");
    session.removeAttribute(SampleGateway.GATEWAY_SESSION);
    session.invalidate();
%>

<html>
<head>
    <script language=javascript>
        function redirect(){
            window.location = "../index.jsp";
        }
    </script>
</head>
<body onload="redirect()">
</body>
</html>