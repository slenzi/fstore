<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Toolbar partial that's loaded for the "main_storeList" state
-->

<md-toolbar class="md-toolbar-tools">
	
	<!--
	<h2>Text Edit</h2>
	-->
	
	<div flex></div>

	<md-button class="" ng-click="main.saveTextEdit()">
		Save
	</md-button>

	<md-button class="" ng-click="main.saveAndCloseTextEdit()">
		Save & Close
	</md-button>
	
	<md-button class="" ng-click="main.cancelTextEdit()">
		Cancel
	</md-button>		
	
</md-toolbar>