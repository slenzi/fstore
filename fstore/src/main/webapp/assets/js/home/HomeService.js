(function(){
	
	'use strict';
	
	angular
		.module('home')
		.service('homeService', [
		      '$log', '$q',
		      HomeService
		]);
	
	function HomeService($log, $q){
		
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
			
			$log.debug('loading test');
			
			return $q.when(sampleData);
		};		
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: doLoadTestRegular,
			loadPromise: doLoadTestWithPromise
	    };
		
	}

})();