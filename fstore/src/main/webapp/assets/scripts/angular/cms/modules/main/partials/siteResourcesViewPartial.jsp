<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Resource store settings partial that's loaded for the "main_storeSettings" state
-->
	
<md-content layout-padding id="siteResourcesContent" style="padding: 10px;">

	<p>
		<div fs-path-resource-table directory="main.offlineDirectory()" resource-click-handler="main.handleEventClickTablePathResource(theResource)"></div>
	</p>

</md-content>