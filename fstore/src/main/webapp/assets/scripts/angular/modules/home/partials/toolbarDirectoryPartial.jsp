<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "home_directory" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<md-button class="" ng-click="home.notImplemented()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
		New Folder
	</md-button>
	
	<md-button class="" ng-click="home.handleEventViewUploadForm()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_file_upload_24px.svg"></md-icon>
		Add Files
	</md-button>
	
	<div flex></div>
	
	<md-button class="" ng-if="home.haveSelectedPathResources()" ng-click="home.handleEventClickDeletePathResources()">
		[Delete]
	</md-button>

	<md-button class="" ng-if="home.haveSelectedPathResources()" ng-click="home.handleEventClickClearSelectedPathResources()">
		[Clear Selected]
	</md-button>	
	
	<md-button class="" ng-click="home.handleEventViewStoreSettings()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_settings_24px.svg"></md-icon>
		Store Settings
	</md-button>
	
</md-toolbar>