<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<%@ page import="org.lenzi.fstore.web.constants.WebConstants" %>

<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<html>
<body>

<%
String errorMessage = (String)request.getAttribute(WebConstants.APP_REQUEST_ERROR_MESSAGE);
if(errorMessage != null){
	out.println(errorMessage);
}
%>

<%
String testData = (String)request.getAttribute("test-data");
if(testData != null){
	out.println(testData);
}else{
	out.println("No message found in request...Boo!");
}
%>

</body>
</html>