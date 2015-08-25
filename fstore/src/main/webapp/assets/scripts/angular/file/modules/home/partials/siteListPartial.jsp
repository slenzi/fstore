<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!--
Cms site list partial that's loaded for the "home_siteList" state
-->
	
<md-content layout-padding layout="column" class="storeList" id="storeListContent" style="padding: 10px;">
	<md-list>
		<div ng-repeat="cmsSite in home.cmsSiteList() track by $index"> <!-- ui-sref="home_directory" -->
			<md-list-item ng-click="home.notImplemented()">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_archive_24px.svg"></md-icon>
				{{cmsSite.name}}
				<span flex></span>
			</md-list-item>
		</div>
	</md-list>			
</md-content>