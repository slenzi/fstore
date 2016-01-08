<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%-- used for accessing our spring managed properties --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
    
<!-- -------------------------------------------

third party scripts

--------------------------------------------- -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular/angular.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-resource/angular-resource.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-touch/angular-touch.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-animate/angular-animate.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-aria/angular-aria.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-messages/angular-messages.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-ui-router/release/angular-ui-router.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-material/angular-material.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-smart-table/dist/smart-table.js"></script>

<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/textAngular/dist/textAngular.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/textAngular/dist/textAngular-rangy.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/textAngular/dist/textAngular-sanitize.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/textAngular/dist/textAngularSetup.js"></script>

<!--  
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/angular-ui-grid/ui-grid.js"></script>
-->

<!-- upgrade to version 1.0.x -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/sockjs-client/dist/sockjs-1.0.3.js"></script>
<!--
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/sockjs-client/dist/sockjs-0.3.4.js"></script>
-->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/bower/stomp-websocket/lib/stomp.js"></script>