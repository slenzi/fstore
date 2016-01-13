<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.LocalTime" %>

<%@ page import="org.lenzi.fstore.cms.api.*" %>
<%@ page import="org.lenzi.fstore.cms.constants.CmsConstants" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
CmsService cmsService = (CmsService)session.getAttribute(CmsConstants.SESSION_ATT_CMS_SERVICE);

CmsLink cmsLink = cmsService.getCmsLink();
%>

<html>

<head>

	<title>Sample Page</title>

	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	
</head>

<body>

	<h2>Sample JSP</h2>

	<p>
	This is the CMS test page. It should load as a regular JSP.
	</p>
	
	<p>
	The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
	</p>
	
	<p>
	The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
	</p>
	
	<p>
	The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
	</p>

	<p>
	The time is <b><%=LocalTime.now() %></b> on <b><%=LocalDate.now() %></b>
	</p>
	
	<a href="https://www.google.com">google</a>
	
	<p>
	<img src="http://mentalfloss.com/sites/default/legacy/wp-content/uploads/2007/04/trashcat_410.jpg"/>
	</p>
	
	<p>
	<img src="<%=cmsLink.createLink("/test/img/cat.jpg") %>"/>
	</p>	
	
</body>

</html>

