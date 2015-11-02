<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<html>
<body>

	<h1>FStore Test Ground</h1>
	
	<p>
	<b>App name:</b> <spring:eval expression="@MyAppProperties.getProperty('application.title')" />
	</p>

	<p>
	<b>Active spring profiles:</b> <spring:eval expression="@environment.getProperty('spring.profiles.active')" />
	</p>	
	
	<p>
	This application demos how to read/write trees to a database using a closure table.
	</p>
	
	<p>
	This application also builds upon the basic tree structure to store files and directories.
	</p>
	
	<h2>File Manager Application</h2>
	
	<p>
		<a href="file/index.jsp">File Manager</a>
	</p>
	
	<p>
		<a href="file/index_ecog_acrin.jsp">File Manager (ECOG-ACRIN Themed)</a>
	</p>
	
	<h2>CMS Application</h2>
	
	<p>
		<a href="cms/index.jsp">Content Management System</a>
	</p>
	
	<p>
		<a href="cms/index_ecog_acrin.jsp">Content Management System (ECOG-ACRIN Themed)</a>
	</p>	
	
	<h2>WebSocket Setup Check</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/hello/info">/spring/hello/info</a>
	</p>
	
	<h2>Sample Tree Tests</h2>

	<p>
		<a href="<%=request.getContextPath() %>/spring/example/test">Fetch sample tree.</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/example/tree">Sample tree dispatcher test.</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/example/tree/1">CXF JAX-RS fetch sample tree.</a>
	</p>
	
	<h2>File 2 Manager Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/test/makestore">Create sample file store.</a><br>
		Creates a sample file store with some directories and files
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/download/id/14">Dispatcher download file resource (fileId=14)</a><br>
		Download file #14 from spring controller, should prompt user with a save dialog window.
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/load/id/14">Dispatcher load file resource (fileId=14)</a><br>
		Load file #14 from spring controller, should load file directly in browser for known types (text, images, etc) and prompt user with save dialog for other types.
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/resource/file/download/id/14">CXF JAX-RS download file resource (fileId=14).</a><br>
		Download file #14 from Apache CXF JAX-RS service, should prompt user with a save dialog window.
	</p>
	
	<h2>File 2 Manager Dispatch Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/load/path/upload_holding/cms/test.jsp">Dispatcher load /upload_holding/cms/test.jsp</a><br>
		Dispatcher load /upload_holding/cms/test.jsp
	</p>

	<!--
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/dispatch/test/test.jsp">Dispatcher dispatch /test/test.jsp (/WEB-INF/jsp)</a><br>
		Dispatcher dispatch /upload_holding/cms/test.jsp
	</p>
	-->
	
	<h2>CMS Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/cms/test/test.jsp">Dispatcher dispatch /test/test.jsp (/WEB-INF/jsp)</a><br>
		Dispatcher dispatch spring/cms/test/test.jsp
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/cms/test">Dispatcher dispatch /test (/WEB-INF/jsp)</a><br>
		Dispatcher dispatch spring/cms/test (should thrown an error, this is a directory, not a file.)
	</p>	

</body>
</html>
