<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>

<html lang="en" >
	
	<head>
	
    	<title>FStore: File Manager</title>

	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="description" content="">
	    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />

		<jsp:include page="includes_header.jsp" />
	    
	    <!-- app resources -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/file/assets/css/app.css"/>
	    
		<!-- for angular location provider -->
		<base href="/file">	    
	    
	</head>

	<body ng-app="fstoreFileManager" layout="row" ng-controller="homeController as home">
	
	<md-sidenav class="md-sidenav-left  md-hue-1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<!-- make sidenav header sticky-->
		<md-content layout="column" style="display: flex; flex-flow: column; height: 100%;">
	
			<md-toolbar class="md-tall md-hue-2">
				<span flex></span>
				<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
					<a href="<%=request.getContextPath()%>">File Manager</a>
					<span flex></span>
					<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="padding-top: 0px;">
			
				<md-toolbar class="md-toolbar-tools md-hue-1">
				
					<!-- ui-sref="settings" -->
					<md-button class="md-raised md-warn leftNavButton" ng-click="home.notImplemented()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_settings_24px.svg"></md-icon>
						System Settings
					</md-button>				
				
				</md-toolbar>
		
				<md-button class="md-raised leftNavButton" ng-click="home.handleEventViewStoreList()">
					Resource Store List
				</md-button>

				<md-button class="md-raised leftNavButton" ng-click="home.handleEventViewSiteList()">
					CMS Site List
				</md-button>				
				
				<md-button class="md-raised leftNavButton" ng-click="home.handleEventSendSampleStomp()">
					Sample STOMP
				</md-button>				
				
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
				<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_menu_18px.svg"></md-icon>
				</md-button>				
				<span style="font-style: italic; whitespace:nowrap;">{{home.sectionTitle()}}</span>
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