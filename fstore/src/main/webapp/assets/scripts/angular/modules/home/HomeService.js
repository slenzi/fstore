(function(){
	
	'use strict';
	
	angular
		.module('home')
		.service('homeService', [
			'appConstants', '$log', '$q', '$location', '$resource', HomeService
			]
		);
	
	function HomeService(appConstants, $log, $q, $location, $resource){
		
		// resource store service
		var storeService = $resource(
				appConstants.contextPath + '/cxf/resource/store/:storeId',
				{ storeId:'@storeId' }
			);
		
		// directory service
		/*
		var directoryService = $resource(
				appConstants.contextPath + '/cxf/resource/directory/:dirId/depth/:maxDepth',
				{ dirId:'@dirId', maxDepth:'@maxDepth' }
			);
		*/
		
		var directoryService = $resource(
				appConstants.contextPath + '/cxf/resource/directory', {},{
					depthGet: {
						url: appConstants.contextPath + '/cxf/resource/directory/:dirId/depth/:maxDepth',
						method: 'GET',
						params: {
							dirId: '@dirId', maxDepth: '@maxDepth'
						}
					},
					breadcrumbGet: {
						url: appConstants.contextPath + '/cxf/resource/directory/breadcrumb/:dirId',
						method: 'GET',
						params: {
							dirId: '@dirId'
						}
					}
				});		
		
		var sampleData = [
			{
				name: 'Fubar',
				avatar: 'weee',
				content: 'this is a test'
			}
		];
		
		// *********************************
		// Internal test methods
		// *********************************
		
		function _doLoadTestRegular(){
			return sampleData;
		};		
		
		// promise based API (asynchronous)
		function _doLoadTestWithPromise(){
			
			$log.debug('loading test, path = ' + $location.path());
			
			return $q.when(sampleData);
		};
		
		// *********************************
		// Internal RESTful methods
		// *********************************

		// fetch all resource stores
		function _fetchResourceStoreList(){
			
			//$log.debug('fetching resource store list...');
			
			return storeService.query().$promise;
			
		};
		
		// fetch store by id
		function _fetchStoreById(storeId){
			
			//$log.debug('fetching resource store for store id ' + storeId + '...');
			
			return storeService.get({ storeId: storeId }).$promise;
			
		};
		
		// fetch directory listing
		function _fetchDirectoryListing(dirId, maxDepth){
			
			//$log.debug('fetching directory lsiting for dir id ' + dirId + ', max depth ' + maxDepth);
			
			//return directoryService.get({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
			return directoryService.depthGet({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
		};
		
		// fetch parent tree / breadcrumb listing for some child directory
		function _fetchBreadcrumb(dirId){
			
			return directoryService.breadcrumbGet({ dirId: dirId }).$promise;
			
		}
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: _doLoadTestRegular,
			loadPromise: _doLoadTestWithPromise,
			getResourceStores: _fetchResourceStoreList,
			getResourceStoreById: _fetchStoreById,
			getDirectoryListing: _fetchDirectoryListing,
			getBreadcrumb: _fetchBreadcrumb
	    };
		
	}

})();