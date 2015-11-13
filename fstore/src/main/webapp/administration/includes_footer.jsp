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
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/administration/modules/main/MainModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/administration/modules/main/MainController.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/administration/modules/main/MainFilter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/administration/modules/main/MainDirective.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/administration/app/AdministrationApp.js"></script>