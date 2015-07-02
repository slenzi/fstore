(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 *
	 * ngMaterial - material design UI components
	 * ngResource - provides interaction support with RESTful services via the $resource service
	 * home - our main home module
	 * ui.grid - ui data grid component
	 * ui.grid.pagination - pagination support for ui.grid
	 */
	homeApp = angular
		.module('fstoreApp', ['ngMaterial', 'ngResource', 'home', 'ui.grid', 'ui.grid.pagination'])
		.config(appConfig)
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('appConstants', {
			contextPath: '@application.context@'
		});
		
		//
		// main app config
		//
		function appConfig($locationProvider, $mdThemingProvider){
			
			locationConfig($locationProvider);
			
			materialConfig($mdThemingProvider);
			
		};
		
		//
		// configure location provider
		//
		function locationConfig($locationProvider){
			
			$locationProvider.html5Mode(true);
			
		};
		
		//
		// angular material UI config
		//
		function materialConfig($mdThemingProvider){
			
			// Extend the gray theme with a few different shades
			var lightGrey = $mdThemingProvider.extendPalette('grey', {
				'50':  'EFEFEF',
				'200': 'C6C6C6',
				'400': '8C8C8C',
				'800': '323232'
			});
			
			// Register the new color palette map with the name <code>light-gray</code>
			$mdThemingProvider.definePalette('light-gray', lightGrey);				
	
			// apply our light-gray theme
			$mdThemingProvider.theme('default')
				.primaryPalette('light-gray', {
					'default': '50', // by default use shade 50 from the light-gray palette for primary intentions
					'hue-1': '200', // use shade 100 for the <code>md-hue-1</code> class
					'hue-2': '400', // use shade 600 for the <code>md-hue-2</code> class
					'hue-3': '800' // use shade A100 for the <code>md-hue-3</code> class
				})
				.accentPalette('red');
				
				// append .dark() to make the theme dark			
			
		};
			
})();