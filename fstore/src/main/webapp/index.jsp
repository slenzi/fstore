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
		<a href="<%=request.getContextPath() %>/cxf/api/tree/1">CXF JAX-RS fetch sample tree.</a>
	</p>
	
	<h2>File Manager Tests</h2>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file/test/makestore">Create sample file store.</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/file/res">File resource dispatcher test.</a>
	</p>
	


</body>
</html>
