(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 *
	 * ui.router - Routing frameworks, essentially more powerful version of built in ngRoute.
	 * ngMaterial - Material design UI components
	 * ngResource - Provides interaction support with RESTful services via the $resource service
	 * home - Our main home module
	 * fsUpload - multi-part HTTP uploader
	 * ui.grid - ui data grid component
	 * ui.grid.pagination - pagination support for ui.grid
	 */
	homeApp = angular
		.module('fstoreApp', ['ui.router', 'ngMaterial', 'ngResource', 'home', 'fsUpload', 'ui.grid', 'ui.grid.pagination'])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('appConstants', {
			contextPath: '/fstore',
			httpUploadHandler: '/fstore/spring/file2/upload'
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
			
			// For any unmatched url, redirect to /state1
			$urlRouterProvider.otherwise("/directory");

			// Now set up the states
			$stateProvider
				.state('home_directory', {
					url: '/directory',
					templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/directoryPartial.jsp'
				})
				.state('home_storeList', {
					url: '/stores',
					templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/storeListPartial.jsp'
				})				
				.state('home_storeSettings', {
					url: '/storeSettings',
					templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/storeSettingsPartial.jsp'
				})
				.state('home_upload', {
					url: '/upload',
					templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/uploadPartial.jsp'
				});			
				
				/*
				.state('state1.list', {
					url: "/list",
					templateUrl: "partials/state1.list.html",
					controller: function($scope) {
						$scope.items = ["A", "List", "Of", "Items"];
					}
				})
				.state('state2', {
					url: "/state2",
					templateUrl: "partials/state2.html"
				})
				.state('state2.list', {
					url: "/list",
					templateUrl: "partials/state2.list.html",
					controller: function($scope) {
						$scope.things = ["A", "Set", "Of", "Things"];
					}
				});
				*/
			
		};
			
})();