<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<md-content layout-padding id="storeSettingsContent">
	
	<p>
	Drop Zone:<br>
	<div fs-upload-drop uploader="home.uploader()" style="border: 1px solid #777; border-style: dashed; height: 200px;"></div>
	</p>	
	
	<p>
	<!-- button for file input -->
	<label class="md-raised md-button" md-ink-ripple for="file-input">
        <span>Select Files</span>
    </label>
	<label class="md-raised md-button" md-ink-ripple ng-click="home.handleEventClearUploadQueue()">
        <span>Clear Files</span>
    </label>
	<label class="md-raised md-button" md-ink-ripple ng-click="home.handleEventDoUpload()">
        <span>Start Upload</span>
    </label>	
	</p>
	
	<p>
	Debug:<br>
	<!--
	<fs-upload-debug uploader="home.uploader()"/>
	-->
	<div fs-upload-debug uploader="home.uploader()"></div>
	</p>
	
	<!-- hide input field. users use 'Select Files' button above -->
	<input id="file-input" style="display: none;" type="file" fs-upload-file-select uploader="home.uploader()" multiple />

</md-content>