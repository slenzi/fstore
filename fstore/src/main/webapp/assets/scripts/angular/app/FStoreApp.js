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
	 * fsStomp - Stomp websocket module
	 * smart-table - lightweight table module
	 * ui.grid - ui data grid component
	 * ui.grid.pagination - pagination support for ui.grid
	 */
	homeApp = angular
		.module('fstoreApp', ['ui.router', 'ngMaterial', 'ngResource', 'home', 'fsUpload', 'fsStomp', 'smart-table', 'ui.grid', 'ui.grid.pagination'])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('appConstants', {
			contextPath: '@application.context@',
			httpUploadHandler: '@http.upload.handler@',
			restServiceStore: '@services.rest.store@',
			restServiceFile: '@services.rest.file@',
			restServiceDirectory: '@services.rest.directory@',
			restServiceCmsSite: '@services.cms.rest.site@'
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
			
			// For any unmatched url, redirect to /stores state. This partial shows a list of all resource stores.
			$urlRouterProvider.otherwise("/stores");

			// Now set up the states
			$stateProvider
				.state('home_directory_icon', {
					url: '/directory-icon/:dirId',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/directoryGridPartial.jsp'					
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarDirectoryPartial.jsp'							
						}
					}
				})
				.state('home_directory_table', {
					url: '/directory-table/:dirId',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/directoryTablePartial.jsp'				
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarDirectoryPartial.jsp'							
						}
					}
				})				
				.state('home_storeList', {
					url: '/stores',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/storeListPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarStoreListPartial.jsp'							
						}
					}
				})
				.state('home_siteList', {
					url: '/sites',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/siteListPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarSiteListPartial.jsp'							
						}
					}
				})				
				.state('home_storeSettings', {
					url: '/storeSettings',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/storeSettingsPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarStoreSettingsPartial.jsp'							
						}
					}
				})
				.state('home_upload', {
					url: '/upload',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/uploadPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/toolbarUploadPartial.jsp'							
						}
					}
				});
			
				/*
				.state('home_directory_icon', {
					url: '/directory-grid',
					templateUrl: appConstants.contextPath + '/assets/scripts/angular/modules/home/partials/directoryGridPartial.jsp'
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
				*/
			
		};
			
})();