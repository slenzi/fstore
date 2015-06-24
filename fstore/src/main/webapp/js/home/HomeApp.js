(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 */
	homeApp = angular
			.module('fstoreApp', ['ngMaterial', 'home'])
		    .config(function($mdThemingProvider){
		
		    	$mdThemingProvider.theme('default')
		    		.primaryPalette('blue')
		            .accentPalette('red');
		
		    });
			
})();