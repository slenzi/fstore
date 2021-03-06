<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Upload form partial that's loaded for the "main_upload" state
-->
	
<md-content layout-padding id="storeSettingsContent">
	
	<p>
		Drop Zone:<br>
		<div fs-upload-drop uploader="main.uploader()" style="border: 1px solid #777; border-style: dashed; height: 200px;"></div>
	</p>

	<p>
		<div fs-upload-progress uploader="main.uploader()"></div>
	</p>
	
	<p>
		<!-- button for file input -->
		<label class="md-raised md-button" md-ink-ripple for="file-input">
			<span>Select Files</span>
		</label>
		<label class="md-raised md-button" md-ink-ripple ng-click="main.handleEventClearUploadQueue()" ng-disabled="main.uploader().isQueueEmpty()">
			<span>Clear Files</span>
		</label>
		<label class="md-raised md-button" md-ink-ripple ng-click="main.handleEventDoUpload()" ng-disabled="main.uploader().isQueueEmpty()">
			<span>Start Upload</span>
		</label>
	</p>

	<p>
		<div fs-upload-queue-table uploader="main.uploader()"></div>
	</p>
	
	<!--
	<p>
		Debug:<br>
		<div fs-upload-debug uploader="main.uploader()"></div>
	</p>
	-->
	
	<!-- hide input field. users use 'Select Files' button above -->
	<input id="file-input" style="display: none;" type="file" fs-upload-file-select uploader="main.uploader()" multiple />

</md-content>