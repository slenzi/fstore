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
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/bower_components/angular-ui-grid/ui-grid.css"/>
	    <!--
	    <link rel="stylesheet" href="assets/app.css"/>
	    -->
		
		<style>
		.collapsable {
			display: inline-block;
			overflow: hidden;
			height: 0;
			transition: height 1s;
			-webkit-transition: height 1s;        
			-moz-transition: height 1s;        
			-o-transition: height 1s;         
		}
		.collapsable.showMe {
			height: 100px;
		}
		.storeGrid {
			width: 275px;
			height: 250px;
		}
		</style>
	    
	</head>

	<body ng-app="fstoreApp" layout="row" ng-controller="homeController as home">
	
	<md-sidenav class="md-sidenav-left md-whiteframe-z1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<md-toolbar class="md-primary md-hue-2">
		<h1 class="md-toolbar-tools">Fstore</h1>
		</md-toolbar>

		<md-content layout-padding layout="column">
		
			<md-button ng-click="home.leftNavClose()" class="md-raised" hide-gt-md>Close Left Nav</md-button>

			<p show-gt-md>
			This sidenav is locked open on your device. To go back to the default behavior,
			narrow your display.
			</p>
			
			<md-button class="md-raised" ng-click="home.doHello()">Button 1</md-button>
			
			<button class="md-button-toggle md-button md-default-theme" ng-click="open = !open">
				<div flex layout="row" class="ng-binding ng-scope">
					Button 2
					<span flex></span>
					<span aria-hidden="true" ng-class="{'toggled' : false}">
						<md-icon md-svg-src="md-toggle-arrow" class="ng-isolate-scope md-default-theme" aria-hidden="true">
							<!--
							<svg version="1.1" x="0px" y="0px" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg" fit="" height="100%" width="100%" preserveAspectRatio="xMidYMid meet" style="pointer-events: none; display: block;">
								<path d="M24 16l-12 12 2.83 2.83 9.17-9.17 9.17 9.17 2.83-2.83z"></path>
								<path d="M0 0h48v48h-48z" fill="none"></path>
							</svg>
							-->
						</md-icon>
					</span>
				</div>
			</button>
			
			<div ng-class="{ showMe: open }" class="collapsable">
				This should collapse
			</div>
			
			<p>
			<!-- ui-grid-pagination -->
			<div ui-grid="home.sampleGrid()" class="storeGrid"></div>
			</p>

		</md-content>
		
	</md-sidenav>
	
	<md-content flex>
	
		<div layout="column" role="main" layout-fill layout-align="top center">
			
			<md-toolbar layout="row" class="md-primary">
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

    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular/angular.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-touch/angular-touch.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-animate/angular-animate.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-aria/angular-aria.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-material/angular-material.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-ui-grid/ui-grid.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/js/home/HomeModule.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/home/HomeController.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/home/HomeService.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/home/HomeFilter.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/home/HomeApp.js"></script>

  </body>
</html>