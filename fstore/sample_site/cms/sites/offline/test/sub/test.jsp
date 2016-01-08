<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalTime" %>

<%@ page import="org.lenzi.fstore.cms.api.CmsLink" %>

<%
CmsLink linker = CmsLink.getInstance(request);
%>

<html>

<head>
	<title>CMS Test Page 2</title>
</head>

<body>

<h2>CMS Test Page 2</h2>

<p>
This is the second CMS test page..
</p>

<p>
The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
</p>

<p>
<a href="<%=linker.createLink("/test/test.jsp") %>">Link back to first page</a>
</p>


</body>

</html>