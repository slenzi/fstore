<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<title>Fstore Login</title>
	
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="description" content="">
	    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
	
		<jsp:include page="defaultLogin_includes_header.jsp" />
		
	    <!-- app resources -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/common/assets/css/app.css"/>
	    
	    <!-- override common styles with file manager specific styles -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/login/assets/css/app.css"/>
	    
		<!-- for angular location provider -->
		<base href="/login">	 		
	
	</head>
	
	<body ng-app="fstoreLogin" layout="row" ng-controller="loginController as main">

	<jsp:include page="defaultLogin_includes_body_header.jsp" />
	
	
	<!--
	Main content area - show resources for current resource store / directory
	-->	
	<!-- the 'style' values make sure we have a sticky header (header doesn't scroll out of view)-->
	<md-content flex style="display: flex; flex-flow: column; height: 100%;">
	
		<md-toolbar class="md-tall md-hue-3">
			<span flex></span>
			<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
				<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/common/assets/img/icons/ic_menu_18px.svg"></md-icon>
				</md-button>				
				<span style="font-style: italic; whitespace:nowrap;">{{main.sectionTitle()}}</span>
				<div flex></div>
			</h3>
		</md-toolbar>
		
		<!-- ui.route view -->
		<!--
		<div ui-view="toolbarContent"></div>
		-->
	
		<!-- layout-fill layout-align="top center" -->
		<!-- flex style="display: flex; flex-flow: column; height: 100%;" -->
		<md-content flex layout="column" class="md-hue-1" role="main" layout-padding>		
		
			<!-- ui.route view -->
			<!--
			<div ui-view="mainContent"></div>
			-->
			
			<c:url value="/login" var="loginUrl"/>

			<form action="<spring:eval expression="@MyAppProperties.getProperty('application.context')" />/spring/core/login" method="POST">

				<c:if test="${param.error != null}">
					<p>
						Invalid username and password.
					</p>
				</c:if>
				<c:if test="${param.logout != null}">
					<p>
						You have been logged out.
					</p>
				</c:if>
				
				<!--
				<p>
					<label for="username">Username</label>
					<input type="text" id="username" name="username"/>
				</p>
				<p>
					<label for="password">Password</label>
					<input type="password" id="password" name="password"/>
				</p>
				-->
				
				<md-input-container class="md-block">
					<label>Username</label>
					<input ng-model="user.username" id="username" name="username">
					</div>
				</md-input-container>				
				
				<md-input-container class="md-block">
					<label>Password</label>
					<input ng-model="user.password" type="password" id="password" name="password">
					</div>
				</md-input-container>				
				
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
				<input type="hidden" name="jSpringSecurityCheck" value = "<c:url value='j_spring_security_check' />" />
				<input type="hidden" name="springLoginUrl" value = "${loginUrl}" />
				
				<button type="submit" class="btn">Log in</button>
				
			</form>			
			
			
		</md-content>
		
		
	</md-content>

	<jsp:include page="defaultLogin_includes_footer.jsp" />

	</body>

</html>