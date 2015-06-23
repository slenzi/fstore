<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>

<html lang="en" >
	
	<head>
	
    	<title>Fstore Material</title>

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

	<body ng-app="fstoreApp" layout="row" ng-controller="HomeController as home">

  	<md-sidenav class="site-sidenav md-sidenav-left md-whiteframe-z2" md-component-id="left" md-is-locked-open="$mdMedia('gt-sm')">

		<md-toolbar class="md-whiteframe-z1">
			<h1>Resource Stores</h1>
		</md-toolbar>

		<!--
		<md-list>
			<md-list-item ng-repeat="it in ul.users">
				<md-button ng-click="ul.selectUser(it)" ng-class="{'selected' : it === ul.selected }">
					<md-icon md-svg-icon="{{it.avatar}}" class="avatar"></md-icon>
					{{it.name}}
				</md-button>
			</md-list-item>
		</md-list>
		-->

  	</md-sidenav>


	<div flex layout="column" tabIndex="-1" role="main" class="md-whiteframe-z2">

		<md-toolbar layout="row" class="md-whiteframe-z1">
			<md-button class="menu" hide-gt-sm ng-click="ul.toggleList()" aria-label="Show User List">
				<md-icon md-svg-icon="menu" ></md-icon>
			</md-button>
			<h1>Fstore Material App</h1>
		</md-toolbar>
			
		<md-content flex id="content">
			
			<p>
			Content here...
			</p>
		
			
			
			<div layout="row">
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
			</div>
			<div layout="row">
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
				<div flex>[icon]</div>
			</div>		
		    
		</md-content>

	</div>

    <script src="<%=request.getContextPath()%>/bower_components/angular/angular.js"></script>
    <script src="<%=request.getContextPath()%>/bower_components/angular-animate/angular-animate.js"></script>
    <script src="<%=request.getContextPath()%>/bower_components/angular-aria/angular-aria.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/bower_components/angular-material/angular-material.js"></script>

    <script src="<%=request.getContextPath()%>/js/home/Home.js"></script>
    <script src="<%=request.getContextPath()%>/js/home/HomeController.js"></script>
    <script src="<%=request.getContextPath()%>/js/home/HomeService.js"></script>

    <script type="text/javascript">

		angular
			.module('fstoreApp', ['ngMaterial'])
		    .config(function($mdThemingProvider){
		
		    	$mdThemingProvider.theme('default')
		    		.primaryPalette('blue')
		            .accentPalette('red');
		
		    });

    </script>

  </body>
</html>