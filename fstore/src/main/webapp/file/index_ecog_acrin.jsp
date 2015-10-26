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
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/file/assets/css/ecog.css"/>
		
		<!-- for angular location provider -->
		<base href="/file">
	    
	</head>

	<body ng-app="fstoreFileManager" layout="row" ng-controller="mainController as main" class="">
	
	<md-sidenav class="md-hue-1" md-component-id="MyLeftNav" md-is-locked-open="$mdMedia('gt-md')">

		<!-- make sidenav header sticky-->
		<md-content layout="column" class="" style="display: flex; flex-flow: column; height: 100%;">
			
			<md-toolbar class="md-hue-3 md-whiteframe-z1" style="min-height: 120px;">
				<h3 class="md-toolbar-tools" style="min-height: 64px;">
					<a href="<%=request.getContextPath()%>">File Manager</a>
					<span flex></span>
					<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
				<span flex></span>				
				<md-content layout="column" style="background-color: rgba(255,255,255, 1); overflow: hidden; min-height: 64px;">
					<img src="<%=request.getContextPath()%>/file/assets/img/ecog/ecog-acrin_logobanner_sm_1.png" style="width: 95%; margin: 0 auto; margin-top: 2px; margin-bottom: 2px; border: 0px solid red; padding-right: 8px; padding-left: 5px; padding-top: 5px;">
				</md-content>
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="min-height: 100px; padding-top: 0px;">
			
				<md-toolbar class="md-toolbar-tools md-hue-1">
			
					<!--  ui-sref="settings" -->
					<md-button class="md-raised md-warn leftNavButton" ng-click="main.notImplemented()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_settings_24px.svg"></md-icon>
						System Settings
					</md-button>
				
				</md-toolbar>
				
				<md-button class="md-raised leftNavButton" ng-click="main.handleEventViewStoreList()">
					Resource Store List
				</md-button>

				<md-content layout-padding class="md-hue-1">
				<h5>Sections</h5>
				</md-content>				
				
				<md-button class="md-raised leftNavButton" ng-href="<%=request.getContextPath()%>/cms/index.jsp">
					CMS
				</md-button>			
				
				<md-content layout-padding class="md-hue-1">
				<h5>Miscellaneous / Test</h5>
				</md-content>
				
				<md-button class="md-raised leftNavButton" ng-click="main.handleEventSendSampleStomp()">
					Sample STOMP
				</md-button>

				<md-button class="md-raised leftNavButton" ng-click="main.handleEventClickToastTest()">
					Toast Test
				</md-button>

				<md-button class="md-raised leftNavButton" ng-click="main.toggleRightNavLock()">
					Toggle Right Nav
				</md-button>				
				
			</md-content>			
			
			<md-content layout="column" class="md-hue-1" layout-padding>
			
                <!--
				<md-content layout-padding layout="column">
				<span style="font-weight: bold;" class="storeListHeader">Resource Stores</span>
				</md-content>
                -->
				
				<!--
				<md-content layout="column" class="storeList">
                    <h3>Resource Stores</h3>
					<md-list>
						<div ng-repeat="store in main.storeList() track by $index" ui-sref="main_directory">
							<md-list-item ng-click="main.handleEventViewStore(store.id)">
								<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_archive_24px.svg"></md-icon>
								{{store.name}}
								<span flex></span>
							</md-list-item>
						</div>
					</md-list>			
				</md-content>
				-->
				
				<!--
				<div ng-repeat="n in [1,5] | makeRange">
				...
				</div>
				
				<input type="text" ng-model="book.tags[$index]">
				<br>
				<md-content layout-padding layout="column">
					<div ui-grid="main.sampleGrid()" class="storeGrid"></div>
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
				<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_menu_18px.svg"></md-icon>
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
	
	<!--
	Right side navigation bar - show properties for currently selected resource.
	-->
	<md-sidenav class="md-sidenav-right md-hue-1" md-component-id="MyRightNav" md-is-locked-open="main.isRightNavOpen()">

		<!-- make sidenav header sticky-->
		<md-content layout="column" style="display: flex; flex-flow: column; height: 100%;">
	
			<md-toolbar class="md-hue-3">
				<span flex></span>
				<!--
				<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
					<a href="<%=request.getContextPath()%>">Resource Properties</a>
					<span flex></span>
					<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_menu_18px.svg"></md-icon>
					</md-button>
				</h3>
				-->
			</md-toolbar>

			<md-content layout="column" class="md-hue-1" style="padding-top: 0px;">
			
				<md-toolbar class="md-toolbar-tools md-hue-1">
				
					<span style="font-style: italic; whitespace:nowrap;">Resource Properties</a>
					
					<span flex></span>
					<md-button ng-click="main.toggleRightNavLock()" class="md-icon-button" aria-label="Menu">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_close_18px.svg"></md-icon>
					</md-button>					
				
					<!-- ui-sref="settings" -->
					<!--
					<md-button class="md-raised md-warn leftNavButton" ng-click="main.notImplemented()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_settings_24px.svg"></md-icon>
						System Settings
					</md-button>
					-->
				
				</md-toolbar>
				
				<md-content layout-padding class="md-hue-1">
				
				Properties go here...
				
				</md-content>
				
			</md-content>
			
			<!-- felx background color to bottom of screen -->
			<md-content layout="column" class="md-hue-1" style="min-height: 3px;" flex>
			&nbsp;
			</md-content>
			
		</md-content>
		
	</md-sidenav>	

	<jsp:include page="includes_footer.jsp" />

  </body>
</html>