(function(){
	
	'use strict';
	
	angular
		.module('home')
		.service('homeService', [
			'appConstants', '$log', '$q', '$location', '$resource', HomeService
			]
		);
	
	function HomeService(appConstants, $log, $q, $location, $resource){
		
		var storeService = $resource(appConstants.contextPath + '/cxf/resource/store', {id:'@storeId'});
		
		var sampleData = [
			{
				name: 'Fubar',
				avatar: 'weee',
				content: 'this is a test'
			}
		];
		
		// *********************************
		// Internal methods
		// *********************************
		
		function doLoadTestRegular(){
			return sampleData;
		};		
		
		// promise based API (asynchronous)
		function doLoadTestWithPromise(){
			
			$log.debug('loading test, path = ' + $location.path());
			
			return $q.when(sampleData);
		};

		// RESTful call to fetch all resource stores. return promise of result.
		function fetchResourceStoreList(){
			
			$log.debug('fetching resource store list...');
			
			return storeService.query().$promise;
			
		};
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: doLoadTestRegular,
			loadPromise: doLoadTestWithPromise,
			getResourceStores: fetchResourceStoreList
	    };
		
	}

})();