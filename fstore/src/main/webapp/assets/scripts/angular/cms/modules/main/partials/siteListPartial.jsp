<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!--
Cms site list partial that's loaded for the "main_siteList" state
-->
	
<!--	
<md-content layout-padding layout="column" class="storeList" id="storeListContent" style="padding: 10px;">
	<md-list>
		<div ng-repeat="cmsSite in main.cmsSiteList() track by $index">
			<md-list-item ng-click="main.notImplemented()">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_archive_24px.svg"></md-icon>
				{{cmsSite.name}}
				<span flex></span>
			</md-list-item>
		</div>
	</md-list>			
</md-content>
-->

<md-content layout-padding id="siteTableContent" style="padding: 10px;">

	<p>
		<div fs-cms-site-table cms-site-list="main.cmsSiteList()" site-click-handler="main.handleEventClickSiteTable(theResource)"></div>
	</p>

</md-content>