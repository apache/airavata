<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script type="text/javascript">
        <!--
        function redirect(){
            window.location = "${redirectUrl}"
        }
        //-->
    </script>
</head>
<body onLoad="setTimeout('redirect()', 1000)">
<h2>You will be now redirect to MyProxy portal !</h2>
<p>
    If your browser didn't redirect to MyProxy Portal within 1 minute click following link,
    <br><br> <a href="${redirectUrl}">${redirectUrl}</a>
</p>

</body>
</html>