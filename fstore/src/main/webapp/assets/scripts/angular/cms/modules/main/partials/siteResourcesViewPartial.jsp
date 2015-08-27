<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!--
Resource store settings partial that's loaded for the "main_storeSettings" state
-->
    
<md-tabs md-dynamic-height md-border-bottom>
    
    <!-- 
    display offline resources
    -->
    <md-tab label="Offline Resources">
        
        <md-toolbar class="directoryTitle-Toolbar">
            <div class="md-toolbar-tools directoryTitle">
                <h2 class="md-flex" ng-repeat="crumb in main.offlineBreadcrumb() track by $index">
                    > <md-button ng-click="main.notImplemented()">{{crumb.name}}</md-button>
                </h2>
            </div>
        </md-toolbar>

        <md-content layout-padding id="siteResourcesOfflineContent" style="padding: 10px;">

            <p>
                <div fs-path-resource-table directory="main.offlineDirectory()" resource-click-handler="main.handleEventClickTablePathResource(theResource)"></div>
            </p>

        </md-content>
        
    </md-tab>
    
    <!-- 
    display online resources
    -->
    <md-tab label="Online Resources">
        
        <md-toolbar class="directoryTitle-Toolbar">
            <div class="md-toolbar-tools directoryTitle">
                <h2 class="md-flex" ng-repeat="crumb in main.onlineBreadcrumb() track by $index">
                    > <md-button ng-click="main.notImplemented()">{{crumb.name}}</md-button>
                </h2>
            </div>
        </md-toolbar>

        <md-content layout-padding id="siteResourcesOnlineContent" style="padding: 10px;">

            <p>
                <div fs-path-resource-table directory="main.onlineDirectory()" resource-click-handler="main.handleEventClickTablePathResource(theResource)"></div>
            </p>

        </md-content>
        
    </md-tab>
    
</md-tabs>