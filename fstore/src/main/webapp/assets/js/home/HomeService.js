(function(){
	
	'use strict';
	
	angular
		.module('home')
		.service('homeService', [
			'$log', '$q', '$location', HomeService
			]
		);
	
	function HomeService($log, $q, $location){
		
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
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: doLoadTestRegular,
			loadPromise: doLoadTestWithPromise
	    };
		
	}

})();