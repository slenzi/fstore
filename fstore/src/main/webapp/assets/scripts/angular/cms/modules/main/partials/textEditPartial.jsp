<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Resource store settings partial that's loaded for the "main_storeSettings" state
-->
	
<md-content layout-padding id="storeSettingsContent" style="padding: 10px;">

    <!-- Ace editor - need to preload this partial so the editor properly loads when the page loads -->
    <div ui-ace="{
        onLoad: aceLoaded,
        onChange: aceChanged,
		theme:'eclipse',
		mode: 'java'
    }"></div>

    <div ta-bind ng-model="fileEditor.dataTextAngular"></div>

</md-content>