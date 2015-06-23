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
	
	<h2>File 1 Manager Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file/test/makestore">Create sample file store.</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file/res">File resource dispatcher test.</a>
	</p>
	
	<h2>File 2 Manager Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/test/makestore">Create sample file store.</a><br>
		Creates a sample file store with some directories and files
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/download/14">Dispatcher download file resource (fileId=14)</a><br>
		Download file #14 from spring controller, should prompt user with a save dialog window.
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file2/res/load/14">Dispatcher load file resource (fileId=14)</a><br>
		Load file #14 from spring controller, should load file directly in browser for known types (text, images, etc) and prompt user with save dialog for other types.
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/resource/file/download/14">CXF JAX-RS download file resource (fileId=14).</a><br>
		Download file #14 from Apache CXF JAX-RS service, should prompt user with a save dialog window.
	</p>


</body>
</html>
