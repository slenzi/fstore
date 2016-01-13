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
	
	<title>CMS Test Page 1</title>
	
	<jsp:include page="<%=cmsPath.createPath(\"/test/includes/header_include.jsp\") %>" />
	
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
<a href="<%=cmsLink.createLink("/test/sub/test.jsp") %>">Link to second page</a>
</p>

<p>
<%
for(int i=0; i<10; i++){
	out.println("i = " + (i + 1) + "<br>");
}
%>
</p>

<p>
<img src="<%=cmsLink.createLink("/test/img/cat.jpg") %>"/>
</p>

<p>
<img src="<%=cmsLink.createLink("/test/img/honey_badger.JPG") %>"/>
</p>

<p>
<a href="https://www.google.com/search?q=lolcats&biw=1920&bih=911&source=lnms&tbm=isch">More Lolcats!</a>
</p>

</body>

</html>

