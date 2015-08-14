<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "home_directory" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<!--
	<md-button class="" ng-click="home.notImplemented()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
		New Folder
	</md-button>
	-->
	
	<md-progress-circular class="md-hue-2" md-mode="indeterminate" md-diameter="24" ng-if="home.isLoadingPathResource()"></md-progress-circular>
	<span ng-if="home.isLoadingPathResource()" style="font-style: italic; font-size: .75em;">Loading...</span>
	
	<div flex></div>
	
	<md-button class="" ng-if="home.haveSelectedPathResources()" ng-click="home.handleEventClickClearSelectedPathResources()">
		[Unselect]
	</md-button>
	
	<md-menu md-position-mode="target-right target">
		<md-button aria-label="Open phone interactions menu" class="md-icon-button" ng-click="$mdOpenMenu()">
			<md-icon md-menu-origin md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_more_vert_24px.svg"></md-icon>
		</md-button>
		<md-menu-content width="4">
			<md-menu-item>
				<md-button ng-click="home.handleEventClickNewFolder()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
					New Folder
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="home.haveChildPathResources()">
				<md-button ng-click="home.handleEventClickSelectAllPathResources()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_select_all_24px.svg"></md-icon>
					Select All
				</md-button>
			</md-menu-item>			

			<md-menu-divider ng-if="home.haveSelectedPathResources()"></md-menu-divider>
				
			<md-menu-item ng-if="home.haveSelectedPathResources()">
				<md-button ng-click="home.handleEventClickDeletePathResources()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_delete_24px.svg"></md-icon>
					Delete
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="home.haveSelectedPathResources()">
				<md-button ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_content_copy_24px.svg"></md-icon>
					Copy
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="home.haveSelectedPathResources()">
				<md-button ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_content_cut_24px.svg"></md-icon>
					Cut / Move
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="true""> <%-- if have something copied or cut... --%>
				<md-button ng-click="home.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_content_paste_24px.svg"></md-icon>
					Paste
				</md-button>
			</md-menu-item>				
		</md-menu-content>
	</md-menu>
	
	<md-button class="" ng-click="home.handleEventViewUploadForm()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_file_upload_24px.svg"></md-icon>
		Add Files
	</md-button>	
	
	<md-button class="" ng-click="home.handleEventViewStoreSettings()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_settings_24px.svg"></md-icon>
		Store Settings
	</md-button>
	
</md-toolbar>