<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
partial which display path resources in a table, using the angular smart table module.
-->

<md-toolbar class="directoryTitle-Toolbar">
	<div class="md-toolbar-tools directoryTitle">
		<!--ng-model="home.directory.getName"-->
		<h2 class="md-flex" ng-repeat="crumb in home.breadcrumb() track by $index">
			> <md-button ng-click="home.handleEventClickBreadcrumb(crumb)">{{crumb.name}}</md-button>
		</h2>
	</div>
</md-toolbar>
	
<md-content layout-padding id="directoryTableContent" style="padding: 10px;">

	<p>
		<div fs-table-resource-view directory="home.directory()"></div>
	</p>

</md-content>