<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "main_storeList" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<h2>{{main.isViewingOnline() ? 'Online Resources' : 'Offline Resources'}}</h2>
	
	<div flex></div>
	
	<%--
	Show offline resources menu options
	--%>
	<span ng-if="!main.isViewingOnline()">
	
		<md-button class="" ng-if="main.haveSelectedOfflinePathResources()" ng-click="main.handleEventClickClearSelectedOfflinePathResources()">
			[Unselect Offline]
		</md-button>

		<md-menu md-position-mode="target-right target">
			<md-button aria-label="Open phone interactions menu" class="md-icon-button" ng-click="$mdOpenMenu()">
				<md-icon md-menu-origin md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_more_vert_24px.svg"></md-icon>
			</md-button>
			<md-menu-content width="4">
				<md-menu-item>
					<md-button ng-click="main.handleEventClickNewOfflineFolder()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
						New Folder
					</md-button>
				</md-menu-item>			
				<md-menu-item ng-if="main.haveOfflineChildPathResources()">
					<md-button ng-click="main.handleEventClickSelectAllOfflinePathResources()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_select_all_24px.svg"></md-icon>
						Select All
					</md-button>
				</md-menu-item>			
			</md-menu-content>
		</md-menu>		
	
	</span>
	
	<%--
	Show online resources menu options
	--%>	
	<span ng-if="main.isViewingOnline()">
	
		<md-button class="" ng-if="main.haveSelectedOnlinePathResources()" ng-click="main.handleEventClickClearSelectedOnlinePathResources()">
			[Unselect Online]
		</md-button>

		<md-menu md-position-mode="target-right target">
			<md-button aria-label="Open phone interactions menu" class="md-icon-button" ng-click="$mdOpenMenu()">
				<md-icon md-menu-origin md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_more_vert_24px.svg"></md-icon>
			</md-button>
			<md-menu-content width="4">
				<md-menu-item>
					<md-button ng-click="main.handleEventClickNewOnlineFolder()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
						New Folder
					</md-button>
				</md-menu-item>			
				<md-menu-item ng-if="main.haveOnlineChildPathResources()">
					<md-button ng-click="main.handleEventClickSelectAllOnlinePathResources()">
						<md-icon md-svg-icon="<%=request.getContextPath()%>/cms/assets/img/icons/ic_select_all_24px.svg"></md-icon>
						Select All
					</md-button>
				</md-menu-item>			
			</md-menu-content>
		</md-menu>		
	
	</span>	
		
	<md-button class="" ng-click="main.notImplemented()">
		Some future button...
	</md-button>		
	
</md-toolbar>