<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
</head>
<body>
<form method="post" id="login" action="${pageContext.request.contextPath}/login">
    <h1 class="table_dark">Login page</h1>
    <table border="1" class="table_dark">
        <tr>
            <th>Your login:</th>
            <th>Your password:</th>
            <th>Log in</th>
        </tr>
        <tr>
            <td>
                <input type="text" name="login" form="login" required>
            </td>
            <td>
                <input type="password" name="password" form="login" required>
            </td>
            <td>
                <input type="submit" name="log in" form="login">
            </td>
        </tr>
    </table>
</form>
<h4><a href="${pageContext.request.contextPath}/drivers/add">Register</a></h4>
<h4 style="color: red">${errorMessage}</h4>
</body>
</html>
