<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<jsp:include page="/common/common_includes_footer.jsp" />

<!-- -------------------------------------------

application scripts

--------------------------------------------- -->

<!-- common -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/common/modules/models/fstore-models.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/common/modules/services/fstore-services.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/common/modules/upload/fstore-upload.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/common/modules/websocket/fstore-stomp.js"></script>

<!-- specific to file app -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/home/modules/main/HomeModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/home/modules/main/HomeController.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/home/modules/main/HomeFilter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/home/modules/main/HomeDirective.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/home/app/HomeApp.js"></script>