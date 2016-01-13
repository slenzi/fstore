<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ page import="org.lenzi.fstore.cms.api.*" %>
<%@ page import="org.lenzi.fstore.cms.constants.CmsConstants" %>

<%
CmsService cmsService = (CmsService)session.getAttribute(CmsConstants.SESSION_ATT_CMS_SERVICE);
CmsLink cmsLink = cmsService.getCmsLink();
%>

<link rel="stylesheet" type="text/css" href="<%=cmsLink.createLink("/test/css/test.css") %>">
