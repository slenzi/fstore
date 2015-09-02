(function(){
	
	'use strict';
	
	// fetch main module
	var mainModule = angular.module('fsFileManagerMain');
	
	/**
	 * Handle single click when also using ng-dblclick. Allows for both
	 * single and double click on the same element.
	 * 
	 * Thanks to Rob:
	 * http://stackoverflow.com/questions/20444409/handling-ng-click-and-ng-dblclick-on-the-same-element-with-angularjs
	 */
	mainModule.directive('sglclick', ['$parse', function($parse) {
		return {
			restrict: 'A',
			link: function(scope, element, attr) {
				var fn = $parse(attr['sglclick']);
				var delay = 300, clicks = 0, timer = null;
				element.on('click', function (event) {
					clicks++;  //count clicks
					if(clicks === 1) {
						timer = setTimeout(function() {
							scope.$apply(function () {
								fn(scope, { $event: event });
							}); 
							clicks = 0;             //after action performed, reset counter
						}, delay);
					} else {
						clearTimeout(timer);    //prevent single-click action
						clicks = 0;             //after action performed, reset counter
					}
				});
			}
		};
	}])
	.directive('fsTableResourceView', ['$log', function($log) {
		
		var controller = ['$scope', function ($scope) {

			function init() {
				
				//$scope.localDir = angular.copy($scope.directory);
				
				$scope.resourceList = $scope.directory.getChildren();
				// a separate list copy for display. this is needed for smart table
				$scope.resourceListView = [].concat($scope.directory.getChildren());
				
				// update resource list when directory changes
				$scope.$watch('directory', function(newDirectory, oldDirectory){
					$scope.resourceList = newDirectory.getChildren();
					$scope.resourceListView = [].concat(newDirectory.getChildren());
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
			'<table st-table="resourceListView" st-safe-src="resourceList" class="table fstoreSmartTable">' +
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
			'	<tr st-select-row="pathResource" st-select-mode="multiple" ng-repeat="pathResource in resourceListView">' +
			'        <td><a href ng-click="clickPathResource(pathResource);  $event.stopPropagation();">{{pathResource.name}}</a></td>' +
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
		
		/*
		return {
			
			restrict: 'AE',
			replace: true,
			scope: {
				directoryResource: '=directory'
			},
			template:
                '<table st-table="" class="table table-striped">' +
                '    <thead>' +
                '    <tr>' +
				'        <th></th>' +
                '        <th st-sort="tableGetters.fileName">Name</th>' +
                '        <th>Type</th>' +
				'        <th>Size</th>' +
                '        <th>Update Date</th>' +
                '    </tr>' +
                '    </thead>' +
                '    <tbody>' +
                '    <tr ng-repeat="pathResource in directoryResource.getChildren()">' +
				'        <td><a href ng-click="home.notImplemented()">[Remove]</a></td>' +
                '        <td>{{pathResource.name}}</td>' +
                '        <td>{{pathResource.type == "DIRECTORY" ? "Folder" : pathResource.mimeType}}</td>' +
                '        <td>{{pathResource.type == "FILE" ? pathResource.getHumanReadableSize() : ""}}</td>' +
                '        <td>{{pathResource.dateUpdated}}</td>' +
                '    </tr>' +
                '    </tbody>' +
                '</table>',
			link: function ($scope, element, attributes){
			
				$scope.tableGetters = function(){
					return {
						// sort by name
						fileName: function (pathResource) {
							return pathResource.name;
						}
					}
				};
			
			}
		};
		*/
		
	}]);


})();