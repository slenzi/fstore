<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "main_upload" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<h2>Upload {{main.isViewingOnline() ? 'Online' : 'Offline'}} Resources</h2>
	
	<div flex></div>
	
	<md-button class="" ng-click="main.handleEventClickCancelUpload()">
		Cancel Upload
	</md-button>	
	
</md-toolbar>