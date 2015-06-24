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

	    <link rel='stylesheet' href='http://fonts.googleapis.com/css?family=Roboto:400,500,700,400italic'>
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/bower_components/angular-material/angular-material.css"/>
	    <!--
	    <link rel="stylesheet" href="assets/app.css"/>
	    -->
	    
	</head>

	<body ng-app="fstoreApp" layout="row" ng-controller="homeController as home">
	
	<md-sidenav class="md-sidenav-left md-whiteframe-z2" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<md-toolbar class="md-theme-indigo">
		<h1 class="md-toolbar-tools">Fstore</h1>
		</md-toolbar>

		<md-content layout-padding layout="column">
		
			<md-button ng-click="home.leftNavClose()" class="md-raised" hide-gt-md>Close Left Nav</md-button>

			<p show-gt-md>
			This sidenav is locked open on your device. To go back to the default behavior,
			narrow your display.
			</p>
			
			<md-button class="md-raised" ng-click="home.doHello()">Button</md-button>

		</md-content>
		
	</md-sidenav>
	
	<md-content flex>
	
		<div layout="column" role="main" layout-fill layout-align="top center">
			
			<md-toolbar layout="row" class="md-whiteframe-z1">
				<h1 class="md-toolbar-tools">[Name of resource store]</h1>
			</md-toolbar>

			<md-content flex layout-padding id="homeContent">
			
				<md-button ng-click="home.toggleLeftNav()" class="md-raised" hide-gt-md>
				Toggle left
				</md-button>
			
				<p>
				The left sidenav will 'lock open' on a medium (>=960px wide) device.
				</p>
				
				<div ng-repeat="n in [10] | makeRange">
					<div layout="row">
						<div flex>[icon]</div>
						<div flex>[icon]</div>
						<div flex>[icon]</div>
						<div flex>[icon]</div>
						<div flex>[icon]</div>
						<div flex>[icon]</div>
						<div flex>[icon]</div>
					</div>			
				</div>		
			
			</md-content>
			
		</div>
		
		<div flex>
			<md-content layout-padding>
			remainder
			</md-content>
		</div>
		
	</md-content>

    <script src="<%=request.getContextPath()%>/bower_components/angular/angular.js"></script>
    <script src="<%=request.getContextPath()%>/bower_components/angular-animate/angular-animate.js"></script>
    <script src="<%=request.getContextPath()%>/bower_components/angular-aria/angular-aria.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-material/angular-material.js"></script>

    <script src="<%=request.getContextPath()%>/js/home/HomeModule.js"></script>
    <script src="<%=request.getContextPath()%>/js/home/HomeController.js"></script>
    <script src="<%=request.getContextPath()%>/js/home/HomeService.js"></script>
	<script src="<%=request.getContextPath()%>/js/home/HomeFilter.js"></script>
	<script src="<%=request.getContextPath()%>/js/home/HomeApp.js"></script>

  </body>
</html>