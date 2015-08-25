<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<jsp:include page="/common/common_includes_footer.jsp" />

<!-- -------------------------------------------

application scripts

--------------------------------------------- -->
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/upload/FsUploadModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/stomp/FsStomp.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeModels.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeController.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeService.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeFilter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/modules/home/HomeDirective.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/file/app/FileManager.js"></script>