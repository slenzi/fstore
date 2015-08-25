<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "home_siteList" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<div flex></div>
	
	<md-button class="" ng-click="home.handleEventClickNewCmsSite()">
		<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_add_circle_outline_24px.svg"></md-icon>
		Create New Site
	</md-button>
	
</md-toolbar>