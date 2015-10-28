<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!--
Resource store list partial that's loaded for the "main_storeList" state
-->

<%-- old store list. we no use an angular smart table
<md-content layout-padding layout="column" class="storeList" id="storeListContent" style="padding: 10px;">
	
    <md-list>
		<div ng-repeat="store in main.storeList() track by $index"> <!-- ui-sref="home_directory" -->
			<md-list-item ng-click="main.handleEventViewStore(store.id)">
				<md-icon md-svg-icon="<%=request.getContextPath()%>/file/assets/img/icons/ic_archive_24px.svg"></md-icon>
				{{store.name}}
				<span flex></span>
			</md-list-item>
		</div>
	</md-list>
    
</md-content>
--%>
    
<md-content layout-padding id="storeTableContent" style="padding: 10px;">

	<p>
		<div fs-table-store-list store-list="main.storeList()" store-click-handler="main.handleEventViewStore(storeId)"></div>
	</p>

</md-content>