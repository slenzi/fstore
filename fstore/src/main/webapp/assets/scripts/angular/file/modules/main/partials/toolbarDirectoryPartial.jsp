<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "main_directory" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<!--
	<md-button class="" ng-click="main.notImplemented()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
		New Folder
	</md-button>
	-->
	
	<md-button class="" ng-click="main.handleEventSwitchResourceView()" aria-label="Toggle Resource View">
		<md-icon ng-if="main.isUsingIconView()" md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_view_list_24px.svg"></md-icon>
		<md-icon ng-if="!main.isUsingIconView()" md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_view_module_24px.svg"></md-icon>
	</md-button>	
	
	<md-progress-circular class="md-hue-2" md-mode="indeterminate" md-diameter="24" ng-if="main.isLoadingPathResource()"></md-progress-circular>
	<span ng-if="main.isLoadingPathResource()" style="font-style: italic; font-size: .75em;">Loading...</span>
	
	<div flex></div>
	
	<md-button class="" ng-if="main.haveSelectedPathResources()" ng-click="main.handleEventClickClearSelectedPathResources()">
		[Unselect]
	</md-button>
	
	<md-menu md-position-mode="target-right target">
		<md-button aria-label="Open phone interactions menu" class="md-icon-button" ng-click="$mdOpenMenu()">
			<md-icon md-menu-origin md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_more_vert_24px.svg"></md-icon>
		</md-button>
		<md-menu-content width="4">
			<md-menu-item>
				<md-button ng-click="main.handleEventClickNewFolder()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
					New Folder
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="main.haveChildPathResources()">
				<md-button ng-click="main.handleEventClickSelectAllPathResources()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_select_all_24px.svg"></md-icon>
					Select All
				</md-button>
			</md-menu-item>			

			<md-menu-divider ng-if="main.haveSelectedPathResources()"></md-menu-divider>
				
			<md-menu-item ng-if="main.haveSelectedPathResources()">
				<md-button ng-click="main.handleEventClickDeletePathResources()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_delete_24px.svg"></md-icon>
					Delete
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="main.haveSelectedPathResources()">
				<md-button ng-click="main.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_content_copy_24px.svg"></md-icon>
					Copy
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="main.haveSelectedPathResources()">
				<md-button ng-click="main.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_content_cut_24px.svg"></md-icon>
					Cut / Move
				</md-button>
			</md-menu-item>
			<md-menu-item ng-if="true""> <%-- if have something copied or cut... --%>
				<md-button ng-click="main.notImplemented()">
					<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_content_paste_24px.svg"></md-icon>
					Paste
				</md-button>
			</md-menu-item>				
		</md-menu-content>
	</md-menu>
	
	<md-button class="" ng-click="main.handleEventViewUploadForm()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_file_upload_24px.svg"></md-icon>
		Add Files
	</md-button>	
	
	<md-button class="" ng-click="main.handleEventViewStoreSettings()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_settings_24px.svg"></md-icon>
		Store Settings
	</md-button>
	
</md-toolbar>