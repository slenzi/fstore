<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalTime" %>

<%@ page import="org.lenzi.fstore.cms.api.CmsLink" %>

<html>

<head>
	<title>CMS Test Page 1</title>
</head>

<body>

<h2>CMS Test Page 1</h2>

<p>
This is the CMS test page. It should load as a regular JSP.
</p>

<p>
The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
</p>

<p>
<a href="<%=CmsLink.getInstance(request).createLink("/test/sub/test.jsp") %>">Link to second page</a>
</p>

<p>
<%
for(int i=0; i<10; i++){
	out.println("i = " + (i + 1) + "<br>");
}
%>
</p>

</body>

</html>