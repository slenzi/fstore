<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<html>
<body>

	<h1>FStore Test Ground</h1>
	
	<p>
	This application demos how to read/write trees to a database using a closure table.
	</p>
	
	<p>
	This application also builds upon the basic tree structure to store files and directories.
	</p>
	
	<p>
	<a href="index_angular.jsp">Angular test</a>
	</p>
	
	<p>
	<a href="index_angular_ecog_acrin.jsp">ECOG-ACRIN Angular test</a>
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
		Dispatcher dispatch /upload_holding/cms/test.jsp
	</p>	

</body>
</html>
