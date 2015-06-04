<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<html>
<body>

	<h2>Spring-config sample application.</h2>
	
	<p>
	This application demos how to read/write trees to a database using a closure table.
	</p>
	
	<p>
	This application also builds upon the basic tree structure for store files and directories.
	</p>

	<p>
		<a href="<%=request.getContextPath() %>/spring/test">Fetch sample tree with ID = 1</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/res">CMS Resource Dispatcher...</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/spring/tree/1">Test Tree Dispatcher...</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/api/tree/1">CXF JAX-RS fetch tree with ID 1</a>
	</p>

</body>
</html>
