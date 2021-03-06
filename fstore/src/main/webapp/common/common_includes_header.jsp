<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
 
<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!-- -------------------------------------------

common meta

--------------------------------------------- -->
<!-- for spring security  -->
<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>
    
<!-- -------------------------------------------

common third party resources

--------------------------------------------- -->
<link rel='stylesheet' href='<spring:eval expression="@MyAppProperties.getProperty('web.prot')" />://fonts.googleapis.com/css?family=Roboto:400,500,700,400italic'>
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/angular-material/angular-material.min.css"/>

<!-- for textAngular -->
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/textAngular/dist/textAngular.css"/>
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/font-awesome/css/font-awesome.min.css"/>

<!-- no longer used
<link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/angular-ui-grid/ui-grid.css"/>
-->
<!-- used by angular "smart-table" plugin -->    
<link data-require="bootstrap-css@3.2.0" data-semver="3.2.0" rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css" />