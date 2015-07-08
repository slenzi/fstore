<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Directory partial. Loads with #/directory
-->

<!-- layout-padding -->
<md-content id="homeContent" class="iconGrid">
 
	<md-grid-list md-cols-sm="3" md-cols-md="5" md-cols-gt-md="6" md-cols-gt-lg="8" md-row-height-gt-md="1:1" md-row-height="2:2" md-gutter="0px" md-gutter-gt-sm="0px">
		
		<!-- loop through all child path resources -->
		<md-grid-tile class="white" md-rowspan="1" md-colspan="1" md-colspan-sm="1" ng-repeat="pathResource in home.directory().getChildren()" ng-dblclick="home.notImplemented()">
			
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