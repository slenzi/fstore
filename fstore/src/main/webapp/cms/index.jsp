<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>

<html lang="en" >
	
	<head>
	
    	<title>FStore: CMS</title>

	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="description" content="">
	    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />

		<jsp:include page="includes_header.jsp" />
	    
	    <!-- app resources -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/common/assets/css/app.css"/>
	    
	    <!-- override common styles with cms specific styles -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/cms/assets/css/app.css"/>	    
	    
		<!-- for angular location provider -->
		<base href="/cms">	    
	    
	</head>

	<body ng-app="fstoreCms" layout="row" ng-controller="mainController as main">
	
	<jsp:include page="includes_body_header.jsp" />
	
	<md-sidenav class="md-sidenav-left  md-hue-1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<!-- make sidenav header sticky-->
		<md-content layout="column" style="display: flex; flex-flow: column; height: 100%;">
	
			<md-toolbar class="md-tall md-hue-2">
				<span flex></span>
				<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
					<a href="<%=request.getContextPath()%>/spring/core/home">Content Management System</a>
					<span flex></span>
					<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/common/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="padding-top: 0px;">
			
				<md-toolbar class="md-hue-1" style="padding-top:7px;">
				
					<!-- ui-sref="settings" -->
					<md-button class="md-raised md-warn leftNavButton" ng-href="<%=request.getContextPath()%>/administration/index.jsp">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/common/assets/img/icons/ic_settings_24px.svg"></md-icon>
						Administration
					</md-button>				
				
				</md-toolbar>

				<md-button class="md-raised leftNavButton" ng-click="main.handleEventViewSiteList()">
					Site List
				</md-button>
				
				<md-content layout-padding class="md-hue-1">
				<h5>Sections</h5>
				</md-content>				
				
				<md-button class="md-raised leftNavButton" ng-href="<%=request.getContextPath()%>/file/index.jsp">
					File Manager
				</md-button>
				
				<md-content layout-padding class="md-hue-1">
					<h5>CMS Quick Settings</h5>
					<md-switch ng-model="session.isViewingOnline" aria-label="Is Online" ng-change="main.setCmsViewMode(session.isViewingOnline)"/>
					Online Mode: {{ session.isViewingOnline }}
					</md-switch>
				</md-content>								
				
				<br>								
				
			</md-content>
			
			<!-- felx background color to bottom of screen -->
			<md-content layout="column" class="md-hue-1" style="min-height: 3px;" flex>
			&nbsp;
			</md-content>
			
		</md-content>
		
	</md-sidenav>
	
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
		<div ui-view="toolbarContent"></div>	
	
		<!-- layout-fill layout-align="top center" -->
		<md-content layout="column" role="main">		
		
			<!-- ui.route view -->
			<div ui-view="mainContent"></div>
			
		</md-content>
		
		<!--
		<div flex>
			<md-content layout-padding>
			remainder
			</md-content>
		</div>
		-->
		
	</md-content>

	<jsp:include page="includes_footer.jsp" />

  </body>
</html>