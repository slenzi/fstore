<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<html>
<body>

<%
String message = (String)request.getAttribute("message");
if(message != null){
	out.println(message);
}else{
	out.println("No message found in request...Boo!");
}
%>

</body>
</html>