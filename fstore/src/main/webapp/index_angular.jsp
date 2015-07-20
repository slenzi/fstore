<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>

<html lang="en" >
	
	<head>
	
    	<title>Fstore</title>

	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="description" content="">
	    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />

		<!-- third party resources -->
	    <link rel='stylesheet' href='http://fonts.googleapis.com/css?family=Roboto:400,500,700,400italic'>
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/angular-material/angular-material.css"/>
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower/angular-ui-grid/ui-grid.css"/>
	    
	    <!-- app resources -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/app.css"/>
	    
		<!-- for angular location provider -->
		<base href="/">	    
	    
	</head>

	<body ng-app="fstoreApp" layout="row" ng-controller="homeController as home">
	
	<md-sidenav class="md-sidenav-left  md-hue-1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<!-- make sidenav header sticky-->
		<md-content layout="column" style="display: flex; flex-flow: column; height: 100%;">
	
			<md-toolbar class="md-tall md-hue-2">
				<span flex></span>
				<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
					<a href="<%=request.getContextPath()%>">Fstore</a>
					<span flex></span>
					<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="padding-top: 5px;">
			
				<!-- ui-sref="settings" -->
				<md-button class="md-raised md-warn leftNavButton" ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_settings_24px.svg"></md-icon>
					System Settings
				</md-button>
				
				<md-button class="md-raised leftNavButton" ng-click="home.handleEventViewStoreList()">
					Store List
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
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
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

	<!-- third party scripts -->
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular/angular.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-resource/angular-resource.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-touch/angular-touch.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-animate/angular-animate.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-aria/angular-aria.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-ui-router/release/angular-ui-router.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-material/angular-material.js"></script>  
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-ui-grid/ui-grid.js"></script>

	<!-- app scripts -->
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/upload/FsUploadModule.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/home/HomeModule.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/home/HomeModels.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/home/HomeController.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/home/HomeService.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/modules/home/HomeFilter.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/app/FStoreApp.js"></script>

  </body>
</html>