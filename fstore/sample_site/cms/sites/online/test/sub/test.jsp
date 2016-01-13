<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalTime" %>

<%@ page import="org.lenzi.fstore.cms.api.*" %>
<%@ page import="org.lenzi.fstore.cms.constants.CmsConstants" %>

<%
CmsService cmsService = (CmsService)session.getAttribute(CmsConstants.SESSION_ATT_CMS_SERVICE);
CmsLink cmsLink = cmsService.getCmsLink();
CmsPath cmsPath = cmsService.getCmsPath();
%>

<html>

<head>

	<title>CMS Test Page 2</title>
	
	<jsp:include page="<%=cmsPath.createPath(\"/test/includes/header_include.jsp\") %>" />
	
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
<a href="<%=cmsLink.createLink("/test/test.jsp") %>">Link back to first page</a>
</p>


</body>

</html>

