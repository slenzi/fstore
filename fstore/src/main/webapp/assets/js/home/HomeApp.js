(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 */
	homeApp = angular
			.module('fstoreApp', ['ngMaterial', 'home', 'ui.grid', 'ui.grid.pagination'])
		    .config(function($mdThemingProvider){
		
		    	$mdThemingProvider.theme('default')
		    		.primaryPalette('grey')
		            .accentPalette('red');
					
					// append .dark() to make the theme dark
		
		    });
			
})();