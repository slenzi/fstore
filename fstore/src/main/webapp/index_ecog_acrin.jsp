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
	    
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower_components/angular-material/angular-material.css"/>
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/bower_components/angular-ui-grid/ui-grid.css"/>
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/ecog.css"/>
		
		<!-- for angular location provider -->
		<base href="/">
	    
	</head>

	<body ng-app="fstoreApp" layout="row" ng-controller="homeController as home" class="">
	
	<md-sidenav class="md-hue-1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<!-- make sidenav header sticky-->
		<md-content layout="column" class="" style="display: flex; flex-flow: column; height: 100%;">
			
			<md-toolbar class="md-hue-3 md-whiteframe-z1" style="min-height: 120px;">
				<h3 class="md-toolbar-tools" style="min-height: 64px;">
					File Store Manager
					<span flex></span>
					<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
				<span flex></span>				
				<md-content layout="column" style="background-color: rgba(255,255,255, 1); overflow: hidden; min-height: 64px;">
					<img src="<%=request.getContextPath()%>/assets/img/ecog/ecog-acrin_logobanner_sm_1.png" style="width: 95%; margin: 0 auto; margin-top: 2px; margin-bottom: 2px; border: 0px solid red; padding-right: 8px; padding-left: 5px; padding-top: 5px;">
				</md-content>
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="min-height: 100px; padding-top: 5px;">
			
				<md-button class="md-raised md-warn leftNavButton" ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_settings_24px.svg"></md-icon>
					System Settings
				</md-button>
				
				<md-button class="md-raised leftNavButton" ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
					Create New Store
				</md-button>
				
			</md-content>			
			
			<md-content layout="column" class="md-hue-1" layout-padding>
			
                <!--
				<md-content layout-padding layout="column">
				<span style="font-weight: bold;" class="storeListHeader">Resource Stores</span>
				</md-content>
                -->
				
				<md-content layout="column" class="storeList">
                    <h3>Resource Stores</h3>
					<md-list>
						<div ng-repeat="store in home.storeList() track by $index">
							<md-list-item ng-click="home.handleEventViewStore(store.id)">
								<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_archive_24px.svg"></md-icon>
								{{store.name}}
								<span flex></span>
							</md-list-item>
						</div>
					</md-list>			
				</md-content>
				
				<!--
				<div ng-repeat="n in [1,5] | makeRange">
				...
				</div>
				
				<input type="text" ng-model="book.tags[$index]">
				<br>
				<md-content layout-padding layout="column">
					<div ui-grid="home.sampleGrid()" class="storeGrid"></div>
				</md-content>
				-->
				
			</md-content>
			
			<!-- felx background color to bottom of screen -->
			<md-content layout="column" class="md-hue-1" style="min-height: 3px;" flex>
			&nbsp;
			</md-content>
			
		</md-content>
		
	</md-sidenav>
	
	<!-- the 'style' values make sure we have a sticky header (header doesn't scroll out of view)-->
	<md-content flex style="display: flex; flex-flow: column; height: 100%;">
	
		<!-- class="md-tall" style="min-height: 140px;" -->
		<md-toolbar class="md-hue-3">
			<span flex></span>
			<h3 class="md-toolbar-tools">
				<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
				</md-button>				
				<span style="font-style: italic; whitespace:nowrap;">[Name of resource store]</span>
				<div flex></div>
			</h3>
		</md-toolbar>
		
		<md-toolbar class="md-toolbar-tools md-whiteframe-z1">
			
			<md-button ng-click="home.notImplemented()" class="">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
				New Folder
			</md-button>
			
			<md-button ng-click="home.notImplemented()" class="">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_file_upload_24px.svg"></md-icon>
				Add Files
			</md-button>
			
			<div flex></div>
			
			<md-button ng-click="home.notImplemented()" class="">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_settings_24px.svg"></md-icon>
				Store Settings
			</md-button>
			
		</md-toolbar>		
	
		<!-- layout-fill layout-align="top center" -->
		<md-content layout="column" role="main">
			
			<md-toolbar>
				<div class="md-toolbar-tools md-whiteframe-z1 directoryTitle">
				  <h2 class="md-flex">[Directory Title]</h2>
				</div>
			</md-toolbar>
			
			<!-- layout-padding -->
			<md-content id="homeContent" class="iconGrid">
             
				<md-grid-list md-cols-sm="3" md-cols-md="5" md-cols-gt-md="6" md-cols-gt-lg="8" md-row-height-gt-md="1:1" md-row-height="2:2" md-gutter="0px" md-gutter-gt-sm="0px">
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="blue shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer>
							<h3>
							Sample Folder Name<br>[size]<br>other
							</h3>
						</md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="blue shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer>
							<h3>
							Sample File Name<br>[256 Kb]<br>6/30/2015 2:34 PM
							</h3>
						</md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
					<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-dblclick="home.notImplemented()">
						<md-icon class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
						<md-grid-tile-footer><h3>name</h3></md-grid-tile-footer>
					</md-grid-tile>
					
				</md-grid-list>
				
                <!--
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
                -->

				<!--
				<div flex></div>
				-->
			
			</md-content>
			
			<!--
			<md-content layout="column" style="min-height: 110px;" flex>
			hey
			</md-content>
			-->
			
		</md-content>
		
		<!--
		<div flex>
			<md-content layout-padding>
			remainder
			</md-content>
		</div>
		-->
		
	</md-content>

    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular/angular.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-resource/angular-resource.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-touch/angular-touch.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-animate/angular-animate.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-aria/angular-aria.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-material/angular-material.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-ui-grid/ui-grid.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeModule.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeModels.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeController.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeService.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeFilter.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeApp.js"></script>

  </body>
</html>