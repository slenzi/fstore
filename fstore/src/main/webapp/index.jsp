<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<html>
<body>

	<h2>Spring-config sample application.</h2>
	
	<p>
	This application demonstrates how to use class-based annotation configuration to bootstrap
	Spring, Spring MVC, JPA + Hibernate, and a Apache CXF JAX-RS service.  No beans.xml file! 
	</p>

	<p>
		<a href="<%=request.getContextPath() %>/spring/test">Fetch sample tree with ID = 1</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/api/tree/1">CXF JAX-RS fetch tree with ID 1</a>
	</p>	
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/api/person">CXF JAX-RS Test</a>
	</p>
	
	<p>
		<a href="<%=request.getContextPath() %>/cxf/api/person/1">CXF JAX-RS + JPA Test. Fetch Person 1 from database.</a><br>
		<a href="<%=request.getContextPath() %>/cxf/api/person/2">CXF JAX-RS + JPA Test. Fetch Person 2 from database.</a>
	</p>

</body>
</html>
