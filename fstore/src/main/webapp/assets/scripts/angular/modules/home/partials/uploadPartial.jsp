<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<md-content layout-padding id="storeSettingsContent">

	<p>
	Upload directive testing.
	</p>

	<p>
	Files:<br>
	<input type="file" fs-upload-file-select="" uploader="home.uploader()" multiple  />
	</p>	
	
	<p>
	Debug:<br>
	<fs-upload-debug uploader="home.uploader()"/>
	</p>

</md-content>