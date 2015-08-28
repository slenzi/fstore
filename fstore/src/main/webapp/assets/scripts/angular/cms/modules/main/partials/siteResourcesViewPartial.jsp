<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Resource store settings partial that's loaded for the "main_storeSettings" state
-->
    
<md-tabs md-dynamic-height md-border-bottom md-selected="selectedResourceTabIndex">
    
    <md-tab label="Offline Resources" md-on-select="main.setIsViewingOnline(false)">
        
		<md-content layout-padding id="siteResourcesOfflineContent" style="padding: 5px;">
		
            <div class="md-toolbar-tools directoryTitle">
                <h2 class="md-flex" ng-repeat="crumb in main.offlineBreadcrumb() track by $index">
                    > <md-button ng-click="main.handleEventClickOfflineBreadcrumb(crumb)">{{crumb.name}}</md-button>
                </h2>
            </div>

			<div fs-path-resource-table directory="main.offlineDirectory()" resource-click-handler="main.handleEventClickTableOfflinePathResource(theResource)"></div>

        </md-content>
        
    </md-tab>
    
    <md-tab label="Online Resources" md-on-select="main.setIsViewingOnline(true)">
        
		<md-content layout-padding id="siteResourcesOnlineContent" style="padding: 5px;">

            <div class="md-toolbar-tools directoryTitle">
                <h2 class="md-flex" ng-repeat="crumb in main.onlineBreadcrumb() track by $index">
                    > <md-button ng-click="main.handleEventClickOnlineBreadcrumb(crumb)">{{crumb.name}}</md-button>
                </h2>
            </div>

            <div fs-path-resource-table directory="main.onlineDirectory()" resource-click-handler="main.handleEventClickTableOnlinePathResource(theResource)"></div>

        </md-content>
        
    </md-tab>
    
</md-tabs>