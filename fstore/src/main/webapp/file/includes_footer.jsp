<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<jsp:include page="/common/common_includes_footer.jsp" />

<!-- -------------------------------------------

application scripts

--------------------------------------------- -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/upload/FsUploadModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/stomp/FsStomp.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainModels.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainController.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainService.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainFilter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/main/MainDirective.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/app/FileManager.js"></script>