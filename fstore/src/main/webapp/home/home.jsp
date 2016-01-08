<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

	<head>
	
		<title>Fstore Home</title>
	
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="description" content="">
	    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
	
		<jsp:include page="includes_header.jsp" />
		
	    <!-- app resources -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/common/assets/css/app.css"/>
	    
	    <!-- override common styles with login app specific styles -->
	    <link rel="stylesheet" href="<%=request.getContextPath()%>/home/assets/css/app.css"/>
	    
		<!-- for angular location provider -->
		<base href="/home">	 		
	
	</head>
	
	<body ng-app="fstoreHome" layout="row" ng-controller="homeController as main">

	<jsp:include page="includes_body_header.jsp" />
	
	<!--
	Main content area - show resources for current resource store / directory
	-->	
	<!-- the 'style' values make sure we have a sticky header (header doesn't scroll out of view)-->
	<md-content flex style="display: flex; flex-flow: column; height: 100%;">
	
		<md-toolbar class="md-tall md-hue-3">
			<span flex></span>
			<h3 class="md-toolbar-tools md-toolbar-tools-bottom">
				<!--
				<md-button ng-click="main.toggleLeftNav()" class="md-icon-button" aria-label="Menu" hide-gt-md>
					<md-icon md-svg-icon="<%=request.getContextPath()%>/common/assets/img/icons/ic_menu_18px.svg"></md-icon>
				</md-button>
				-->
				<span style="font-style: italic; whitespace:nowrap;">{{main.sectionTitle()}}</span>
				<div flex></div>
			</h3>
		</md-toolbar>
		
		<!-- ui.route view -->
		<!--
		<div ui-view="toolbarContent"></div>
		-->
	
		<!-- layout-fill layout-align="top center" -->
		<!-- flex style="display: flex; flex-flow: column; height: 100%;" -->
		<!--
		<md-content flex layout="column" class="md-hue-1" role="main" layout-padding>
		-->
		<md-content class="md-padding md-hue-1" layout="row" layout-wrap >
			
			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">Sections</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

					<p>
						<a href="<%=request.getContextPath() %>/file/index.jsp">File Manager</a>
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/cms/index.jsp">Content Management</a>
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/administration/index.jsp">Administration</a>
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/spring/core/login">Login</a>
					</p>				
				
				</md-card-content>
				
			</md-card>			
			
			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">General Info & Debug</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

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
				
				</md-card-content>
				
			</md-card>

			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">WebSocket Setup Check</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

					<p>
						<a href="<%=request.getContextPath() %>/spring/hello/info">/spring/hello/info</a>
					</p>				
				
				</md-card-content>
				
			</md-card>			

			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">Sample Tree Tests</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

					<p>
						<a href="<%=request.getContextPath() %>/spring/example/test">Fetch sample tree.</a>
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/spring/example/tree">Sample tree dispatcher test.</a>
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/cxf/example/tree/1">CXF JAX-RS fetch sample tree.</a>
					</p>				
				
				</md-card-content>
				
			</md-card>			

			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">File 2 Manager Tests</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

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
						
				</md-card-content>
				
			</md-card>
			
			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">File 2 Manager Dispatch Tests</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

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
				
				</md-card-content>
				
			</md-card>						

			<md-card>
			
				<md-card-title layout-padding>
					<md-card-title-text>
						<span class="md-headline">CMS Tests</span><br>
					</md-card-title-text>
				</md-card-title>
				
				<md-card-content>

					<p>
						<a href="<%=request.getContextPath() %>/spring/cms/test/test.jsp">Dispatcher dispatch /test/test.jsp (/WEB-INF/jsp)</a><br>
						Dispatcher dispatch spring/cms/test/test.jsp
					</p>
					
					<p>
						<a href="<%=request.getContextPath() %>/spring/cms/test">Dispatcher dispatch /test (/WEB-INF/jsp)</a><br>
						Dispatcher dispatch spring/cms/test (should thrown an error, this is a directory, not a file.)
					</p>		
				
				</md-card-content>
				
			</md-card>			

			
		</md-content>

	</md-content>

	<jsp:include page="includes_footer.jsp" />

  </body>
</html>
