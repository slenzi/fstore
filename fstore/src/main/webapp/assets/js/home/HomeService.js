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
		var directoryService = $resource(
				appConstants.contextPath + '/cxf/resource/directory/:dirId/depth/:maxDepth',
				{ dirId:'@dirId', maxDepth:'@maxDepth' }
			);
		
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
		
		function doLoadTestRegular(){
			return sampleData;
		};		
		
		// promise based API (asynchronous)
		function doLoadTestWithPromise(){
			
			$log.debug('loading test, path = ' + $location.path());
			
			return $q.when(sampleData);
		};
		
		// *********************************
		// Internal RESTful methods
		// *********************************

		// fetch all resource stores
		function fetchResourceStoreList(){
			
			$log.debug('fetching resource store list...');
			
			return storeService.query().$promise;
			
		};
		
		// fetch store by id
		function fetchStoreById(storeId){
			
			$log.debug('fetching resource store for store id ' + storeId + '...');
			
			return storeService.query({ storeId: storeId }).$promise;
			
		};
		
		// fetch directory listing
		function fetchDirectoryListing(dirId, maxDepth){
			
			$log.debug('fetching directory lsiting for dir id ' + dirId + ', max depth ' + madDepth);
			
			return directoryService.query({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
		};
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: doLoadTestRegular,
			loadPromise: doLoadTestWithPromise,
			getResourceStores: fetchResourceStoreList,
			getResourceStoreById: fetchStoreById,
			getDirectoryListing: fetchDirectoryListing
	    };
		
	}

})();