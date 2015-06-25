(function(){
	
	'use strict';
	
	angular.module('home').service('homeService', ['$q', HomeService]);
	
	function HomeService($q){
		
		var sampleData = [
	             {
	               name: 'Fubar',
	               avatar: 'weee',
	               content: 'this is a test'
	             }
             ];
		
		// Promise-based API
	    return {
	      doLoadTest : function() {
	    	  
	        // Simulate async nature of real remote calls
	        return $q.when(sampleData);
	        
	      }
	    };
		
	}

})();