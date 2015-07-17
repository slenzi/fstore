<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<md-button class="md-raised leftNavButton" ng-click="home.notImplemented()">
	<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
	Create New Store
</md-button>
	
<md-content layout-padding layout="column" class="storeList" id="storeListContent" style="padding: 10px;">
	<md-list>
		<h3>Resource Stores</h3>
		<div ng-repeat="store in home.storeList() track by $index" ui-sref="home_directory">
			<md-list-item ng-click="home.handleEventViewStore(store.id)">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/assets/img/icons/ic_archive_24px.svg"></md-icon>
				{{store.name}}
				<span flex></span>
			</md-list-item>
		</div>
	</md-list>			
</md-content>