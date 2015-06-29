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
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/app.css"/>
	    
	</head>

	<body ng-app="fstoreApp" layout="row" ng-controller="homeController as home">
	
	<md-sidenav class="md-sidenav-left md-whiteframe-z1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<md-toolbar class="md-tall md-hue-2">
			<span flex></span>
			<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
				Fstore
				<span flex></span>
				<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
				</md-button>
			</h3>
		</md-toolbar>

		<md-content layout="column" style="min-height: 110px; ">
		
			<md-button class="md-raised md-warn leftNavButton" ng-click="home.notImplemented()">System Settings</md-button>
			
			<md-button class="md-raised leftNavButton" ng-click="home.notImplemented()">Create New Store</md-button>
			
		</md-content>
		
		<md-content layout="column" layout-padding>
		
			<md-whiteframe class="md-whiteframe-z2" >
				<md-content layout-padding layout="column">
				<span style="font-weight: bold;">Resource Stores</span>
				</md-content>
				<md-content layout-padding layout="column">
					<md-list>
						<div ng-repeat="n in [1,15] | makeRange">
							<md-list-item ng-click="home.notImplemented()">
								Store {{n}}
							</md-list-item>
						</div>
					</md-list>			
				</md-content>
			</md-whiteframe>
			
			<!--
			<br>
			<md-content layout-padding layout="column">
				<div ui-grid="home.sampleGrid()" class="storeGrid"></div>
			</md-content>
			-->
			
		</md-content>
		
		
		<div flex="2"></div>
		
		
	</md-sidenav>
	
	<md-content flex>
	
		<div layout="column" role="main" layout-fill layout-align="top center">
			
			<md-toolbar class="md-tall">
				<span flex></span>
				<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
					<md-button ng-click="home.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>				
					<span style="whitespace:nowrap;">[Name of resource store]</span>
					<div flex></div>
				</h3>
			</md-toolbar>
			
			<md-toolbar class="md-toolbar-tools md-hue-1">
				
				<md-button ng-click="home.notImplemented()" class="md-raised">
				New Folder
				</md-button>
				
				<md-button ng-click="home.notImplemented()" class="md-raised">
				Add Files
				</md-button>
				
				<div flex></div>
				
				<md-button ng-click="home.notImplemented()" class="md-raised md-primary">
				Store Settings
				</md-button>
				
			</md-toolbar>

			<md-content layout-padding id="homeContent" class="iconGrid">
             
				
					<md-grid-list md-cols-sm="4" md-cols-md="5" md-cols-gt-md="7" md-row-height-gt-md="1:1" md-row-height="2:2" md-gutter="12px" md-gutter-gt-sm="8px">
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
						</md-grid-tile>
						
						<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1">
							<md-icon style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
							<md-grid-tile-footer><h3>file/folder name [size]</h3></md-grid-tile-footer>
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
			
		</div>
		
		<!--
		<div flex>
			<md-content layout-padding>
			remainder
			</md-content>
		</div>
		-->
		
	</md-content>

    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular/angular.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-touch/angular-touch.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-animate/angular-animate.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-aria/angular-aria.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-material/angular-material.js"></script>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower_components/angular-ui-grid/ui-grid.js"></script>

    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeModule.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeController.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeService.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeFilter.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/assets/js/home/HomeApp.js"></script>

  </body>
</html>