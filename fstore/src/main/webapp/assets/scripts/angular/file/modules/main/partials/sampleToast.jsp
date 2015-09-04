<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
template for our sample md-toast
-->
	
<md-toast style="min-width: 50%; width: 600px; min-height: 250px; height: 250px; background-color: #bbb; text-align: left; align-items: initial; padding: 0px; z-index: 1000;">

	<md-content class="md-hue-1" layout="column" style="display: flex; flex-flow: column; height: 100%; width: 100%;">

		<div fs-upload-queue-table uploader="getUploader()"></div>

		<span flex>&nbsp;</span>
		<md-button flex ng-click="closeToast()" style="color: #000;">
		Close
		</md-button>

	</md-content>

</md-toast>