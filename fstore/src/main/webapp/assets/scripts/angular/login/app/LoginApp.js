(function(){
	
	'use strict';
	
	var loginApp;
	
	/**
	 * Initialize app with 'angular material' and our 'fsLoginMain' module
	 *
	 * ui.router - Routing frameworks, essentially more powerful version of built in ngRoute.
	 * ngMaterial - Material design UI components
	 * ngResource - Provides interaction support with RESTful services via the $resource service
	 * fsLoginMain - Our main login module
	 * 
	 */
	loginApp = angular
		.module('fstoreLogin',
				[
				 'ui.router', 'ngMaterial', 'ngResource', 'fsLoginMain'
				 ])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('appConstants', {
			contextPath: '@application.context@'
		})
		// inject our own constants into our config
		.config(['appConstants', '$locationProvider', '$mdThemingProvider', '$stateProvider', '$urlRouterProvider', appConfig]);
	
		/**
		 * Main app config
		 *
		 * appConstants - our own application constants
		 * $locationProvider - default angular location provider
		 * $mdThemingProvider - Angular material theme setup
		 * $stateProvider - angular ui.router state provider
		 * $urlRouterProvider - angualr ui.router url provider
		 */
		function appConfig(appConstants, $locationProvider, $mdThemingProvider, $stateProvider, $urlRouterProvider){
			
			locationConfig($locationProvider);
			
			uiRouteConfig(appConstants, $stateProvider, $urlRouterProvider);
			
			materialConfig($mdThemingProvider);
			
		};
		
		/**
		 * Configure location provider
		 */
		function locationConfig($locationProvider){
			
			//$locationProvider.html5Mode(true);
			
		};
		
		/**
		 * Angular material UI config
		 */
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
		
		/**
		 * Angular ui.router config - configure states and partials.
		 */
		function uiRouteConfig(appConstants, $stateProvider, $urlRouterProvider){
			
			/* login app currently doesn't have any routes. This might change later...
			
			// For any unmatched url, redirect to /stores state. This partial shows a list of all resource stores.
			$urlRouterProvider.otherwise("/stores");

			// Now set up the states
			$stateProvider
				.state('main_directory_icon', {
					url: '/directory-icon/:dirId',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/file/modules/main/partials/directoryGridPartial.jsp'					
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/file/modules/main/partials/toolbarDirectoryPartial.jsp'							
						}
					}
				});
				
			*/
			
		};
			
})();