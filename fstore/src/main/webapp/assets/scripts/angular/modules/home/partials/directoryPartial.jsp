<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Directory partial that's loaded for the "home_directory" state
-->

<md-toolbar class="directoryTitle-Toolbar">
	<div class="md-toolbar-tools directoryTitle">
		<!--ng-model="home.directory.getName"-->
		<h2 class="md-flex" ng-repeat="crumb in home.breadcrumb() track by $index">
			> <md-button ng-click="home.handleEventClickBreadcrumb(crumb)">{{crumb.name}}</md-button>
		</h2>
	</div>
</md-toolbar>

<!-- layout-padding -->
<md-content layout-padding id="homeContent" class="iconGrid" style="padding: 10px;">
 
	<md-grid-list md-cols-sm="3" md-cols-md="5" md-cols-gt-md="6" md-cols-gt-lg="8" md-row-height-gt-md="1:1" md-row-height="2:2" md-gutter="0px" md-gutter-gt-sm="0px">
		
		<%--
		Loop through all child path resources.
		If pathResource.isSelected attribute is true then apply 'selected' css style.
		single click toggles isSelected attribute
		double-click handle opening/loading path resource
		--%>
		<md-grid-tile ng-repeat="pathResource in home.directory().getChildren()" ng-class="{'selected' : pathResource.isSelected}" md-rowspan="1" md-colspan="1" md-colspan-sm="1" sglclick="home.handleEventClickPathResource(pathResource)" ng-dblclick="home.handleEventDblClickPathResource(pathResource)">
		
			<md-icon ng-if="pathResource.type == 'DIRECTORY'" class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
			
			<md-icon ng-if="pathResource.type == 'FILE'" class="red shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_insert_drive_file_24px.svg"></md-icon>
			
			<md-grid-tile-footer>
				<h3 ng-if="pathResource.type == 'DIRECTORY'">
				{{pathResource.name}}<br>{{pathResource.dateUpdated}}
				</h3>
				<h3 ng-if="pathResource.type == 'FILE'">
				{{pathResource.name}}<br>{{pathResource.dateUpdated}}<br>{{pathResource.mimeType}} [{{pathResource.size}}]
				</h3>							
			</md-grid-tile-footer>			

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