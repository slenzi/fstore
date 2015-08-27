(function(){
	
	'use strict';
	
	// fetch main module
	var mainModule = angular.module('fsCmsMain');
	
	mainModule
		.directive('fsCmsSiteTable', ['$log', function($log) {
			
			var controller = ['$scope', function ($scope) {
	
				function init() {
					
					$scope.tableSiteList = $scope.cmsSiteList;
					$scope.tableSiteListView = [].concat($scope.cmsSiteList);
					
					// update resource list when directory changes
					$scope.$watch('cmsSiteList', function(newSiteList, oldSiteList){
						
						$scope.tableSiteList = newSiteList;
						$scope.tableSiteListView = [].concat(newSiteList);
						
					}, true);				
					
				}
	
				init();
	
				$scope.tableGetters = function(){
					return {
						siteName: function (site) {
							return site.name;
						}					
					}
				};
				
				$scope.clickCmsSite = function(siteData){
					//alert('test click resource = ' + JSON.stringify(pathResource));
					$scope.siteClickHandler( {theResource: siteData} );
				};
				
			}];
	
			var template = 
				'<table st-table="tableSiteListView" st-safe-src="tableSiteList" class="table table-striped">' +
				'	<thead>' +
				'	<tr>' +
				'        <th st-sort="tableGetters().siteName">Name</th>' +
				'        <th>Desc</th>' +
				'        <th>Update Date</th>' +
				'	</tr>' +
				'	<tr>' +
				'		<th>' +	
				'			<input st-search="name" placeholder="search for file name" class="input-sm form-control" type="search"/>' +
				'		</th>' +				
				'	</tr>' +			
				'	</thead>' +
				'	<tbody>' +
				'	<tr ng-repeat="site in tableSiteListView">' +
				'        <td><a href ng-click="clickCmsSite(site)">{{site.name}}</a></td>' +
				'        <td>{{site.description}}</td>' +
				'        <td>{{site.dateUpdated}}</td>' +
				'	</tr>' +
				'	</tbody>' +
				'	<tfoot>' +
				'		<tr>' +		
				'			<td colspan="4" class="text-center">' +
				'				<div st-pagination="" st-items-by-page="20" st-displayed-pages="7"></div>' +
				'			</td>' +
				'		</tr>' +
				'	</tfoot>' +		
				'</table>';
	
			return {
				restrict: 'AE',
				scope: {
					cmsSiteList: '=',
					siteClickHandler: '&'
				},
				controller: controller,
				template: template
			};
			
		}])
		.directive('fsPathResourceTable', ['$log', function($log) {
		
		var controller = ['$scope', function ($scope) {

			function init() {
				
				var theDirectory = $scope.directory;
				
				if(theDirectory && theDirectory.getChildren()){
					$scope.resourceList = theDirectory.getChildren();
					$scope.resourceListView = [].concat(theDirectory.getChildren());					
				}
			
				// update resource list when directory changes
				$scope.$watch('directory', function(newDirectory, oldDirectory){
					if(newDirectory && newDirectory.getChildren()){
						$scope.resourceList = newDirectory.getChildren();
						$scope.resourceListView = [].concat(newDirectory.getChildren());
					}
				}, true);				
				
			}

			init();

			$scope.tableGetters = function(){
				return {
					resourceName: function (pathResource) {
						return pathResource.name;
					},
					resourceSize: function (pathResource) {
						return pathResource.size;
					},
					resourceType: function (pathResource) {
						if(pathResource.type == 'DIRECTORY'){
							return "";
						}else{
							return pathResource.getHumanReadableSize();
						}
					}					
				}
			};
			
			$scope.clickPathResource = function(pathResource){
				//alert('test click resource = ' + JSON.stringify(pathResource));
				$scope.resourceClickHandler( {theResource: pathResource} );
			};
			
		}];

		var template = 
			'<table st-table="resourceListView" st-safe-src="resourceList" class="table table-striped">' +
			'	<thead>' +
			'	<tr>' +
			'        <th st-sort="tableGetters().resourceName">Name</th>' +
			'        <th st-sort="tableGetters().resourceType">Type</th>' +
			'        <th st-sort="tableGetters().resourceSize" st-skip-natural="true">Size</th>' +
			'        <th>Update Date</th>' +
			'	</tr>' +
			'	<tr>' +
			'		<th>' +	
			'			<input st-search="name" placeholder="search for file name" class="input-sm form-control" type="search"/>' +
			'		</th>' +				
			'	</tr>' +			
			'	</thead>' +
			'	<tbody>' +
			'	<tr ng-repeat="pathResource in resourceListView">' +
			'        <td><a href ng-click="clickPathResource(pathResource)">{{pathResource.name}}</a></td>' +
			'        <td>{{pathResource.type == "DIRECTORY" ? "Folder" : pathResource.mimeType}}</td>' +
			'        <td>{{pathResource.type == "FILE" ? pathResource.getHumanReadableSize() : ""}}</td>' +
			'        <td>{{pathResource.dateUpdated}}</td>' +
			'	</tr>' +
			'	</tbody>' +
			'	<tfoot>' +
			'		<tr>' +		
			'			<td colspan="4" class="text-center">' +
			'				<div st-pagination="" st-items-by-page="20" st-displayed-pages="7"></div>' +
			'			</td>' +
			'		</tr>' +
			'	</tfoot>' +		
			'</table>';

		return {
			restrict: 'AE',
			scope: {
				directory: '=',
				resourceClickHandler: '&'
			},
			controller: controller,
			template: template
		};
		
	}]);


})();