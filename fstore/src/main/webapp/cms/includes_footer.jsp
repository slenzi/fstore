<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
    
<jsp:include page="/common/common_includes_footer.jsp" />

<!-- -------------------------------------------

application scripts

--------------------------------------------- -->

<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainModels.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainController.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainService.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/app/CmsApp.js"></script>

<!--  from file manager app -->
<!--
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/upload/FsUploadModule.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/stomp/FsStomp.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainFilter.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/assets/scripts/angular/cms/modules/main/MainDirective.js"></script>
-->