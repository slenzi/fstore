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
<md-content layout-padding id="homeContent" class="iconGrid noselect" style="padding: 20px;">
 
	<md-grid-list md-cols-sm="3" md-cols-md="4" md-cols-gt-md="5" md-cols-gt-lg="7" md-row-height-gt-md="1:1" md-row-height="2:2" md-gutter="1em" md-gutter-gt-sm="1em">
		
		<%--
		Loop through all child path resources.
		If pathResource.isSelected attribute is true then apply 'selectedTile' css style.
		single click toggles isSelected attribute
		double-click handle opening/loading path resource
		--%>
		<md-grid-tile
			md-rowspan="1" md-colspan="1" md-colspan-sm="1"
			ng-repeat="pathResource in home.directory().getChildren()"
			ng-class="{'selectedTile' : pathResource.isSelected}"
			sglclick="home.handleEventClickIconGridPathResource(pathResource)"
			ng-dblclick="home.handleEventDblClickIconGridPathResource(pathResource)"
			ng-mouseover="home.handlePathResourceMouseOver(pathResource)">
		
            <md-grid-tile-header ng-if="pathResource.isSelected">
                <md-checkbox ng-model="pathResource.isSelected" ng-init="pathResource.isSelected" aria-label="Check"/>
            </md-grid-tile-header>
            
			<md-icon ng-if="pathResource.type == 'DIRECTORY'" class="gray shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_folder_48px.svg"></md-icon>
			
			<md-icon ng-if="pathResource.type == 'FILE'" class="red shadow" style="width:70%; height:70%;" md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_insert_drive_file_24px.svg"></md-icon>
			
			<md-grid-tile-footer ng-class="{'selectedFooter' : pathResource.isSelected}">
				<h3 ng-if="pathResource.type == 'DIRECTORY'" ng-class="{'selectedFooterText' : pathResource.isSelected}">
				{{pathResource.name}}<br>{{pathResource.dateUpdated}}
				</h3>
				<h3 ng-if="pathResource.type == 'FILE'" ng-class="{'selectedFooterText' : pathResource.isSelected}">
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